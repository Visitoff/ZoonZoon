package com.seashore.zoonzoon.gamepad.model

/**
 * Represents a vibration pattern that can be executed on a gamepad controller.
 * 
 * Each pattern defines how motor values should be calculated over time.
 * Motor values are in the range [0.0, 1.0] where 0.0 is no vibration and 1.0 is maximum vibration.
 */
sealed class VibrationPattern {
    /**
     * Calculates the motor values for the current frame.
     * 
     * @param time The current time in milliseconds since pattern started
     * @param intensity The intensity multiplier in range [0.0, 1.0]
     * @return A pair of (leftMotor, rightMotor) values, each in range [0.0, 1.0]
     */
    abstract fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float>
    
    /**
     * Constant vibration pattern - both motors vibrate at constant intensity.
     */
    data object Constant : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            return Pair(intensity, intensity)
        }
    }
    
    /**
     * Pulse vibration pattern - motors pulse on and off at a regular frequency.
     * 
     * @param frequencyHz The pulse frequency in Hertz (pulses per second)
     */
    data class Pulse(val frequencyHz: Float = 2.0f) : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val period = 1000.0f / frequencyHz // Period in milliseconds
            val phase = (time % period) / period // Phase in [0.0, 1.0]
            val value = if (phase < 0.5f) intensity else 0.0f
            return Pair(value, value)
        }
    }
    
    /**
     * Wave vibration pattern - motors vibrate with smooth sine wave modulation.
     * 
     * @param frequencyHz The wave frequency in Hertz
     */
    data class Wave(val frequencyHz: Float = 1.0f) : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val period = 1000.0f / frequencyHz // Period in milliseconds
            val phase = (time % period) / period // Phase in [0.0, 1.0]
            val angle = phase * 2.0f * kotlin.math.PI.toFloat()
            val sineValue = kotlin.math.sin(angle)
            // Map sine wave from [-1, 1] to [0, 1]
            val normalizedValue = (sineValue + 1.0f) / 2.0f
            val value = normalizedValue * intensity
            return Pair(value, value)
        }
    }
    
    /**
     * Custom vibration pattern - user-defined sequence of motor values.
     *
     * @param frames List of frame definitions, each containing (leftMotor, rightMotor, durationMs)
     * @param loop Whether to loop the pattern when it reaches the end
     */
    data class Custom(
        val frames: List<Frame>,
        val loop: Boolean = true
    ) : VibrationPattern() {
        data class Frame(
            val leftMotor: Float,
            val rightMotor: Float,
            val durationMs: Long
        )

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            if (frames.isEmpty()) {
                return Pair(0.0f, 0.0f)
            }

            var accumulatedTime = 0L
            var currentTime = time

            if (loop) {
                val totalDuration = frames.sumOf { it.durationMs }
                if (totalDuration > 0) {
                    currentTime = time % totalDuration
                }
            }

            for (frame in frames) {
                if (currentTime < accumulatedTime + frame.durationMs) {
                    return Pair(
                        frame.leftMotor * intensity,
                        frame.rightMotor * intensity
                    )
                }
                accumulatedTime += frame.durationMs
            }

            return Pair(0.0f, 0.0f)
        }
    }

    /** Plays multiple patterns sequentially (used by playlist mode). */
    data class Sequence(
        val segments: List<VibrationPattern>,
        val segmentDurationsMs: List<Long> = emptyList(),
        val segmentDurationMs: Long = 4_000L
    ) : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            if (segments.isEmpty()) return Pair(0f, 0f)
            val durations = if (segmentDurationsMs.size == segments.size) {
                segmentDurationsMs
            } else {
                List(segments.size) { segmentDurationMs }
            }
            val cycle = durations.sum()
            if (cycle <= 0) return Pair(0f, 0f)
            val t = time % cycle
            var elapsed = 0L
            for (i in segments.indices) {
                val dur = durations[i]
                if (t < elapsed + dur) {
                    return segments[i].calculateFrame(t - elapsed, intensity)
                }
                elapsed += dur
            }
            return segments.last().calculateFrame(0, intensity)
        }
    }

    /** Gesture-recorded pattern: each sample holds intensity + pulse frequency. */
    data class Recorded(
        val samples: List<Sample>,
        val loop: Boolean = true
    ) : VibrationPattern() {
        data class Sample(
            val intensity: Float,
            val frequencyHz: Float,
            val durationMs: Long = 32L
        )

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            if (samples.isEmpty()) return Pair(0f, 0f)
            val total = samples.sumOf { it.durationMs }
            if (total <= 0) return Pair(0f, 0f)

            var localTime = if (loop) time % total else time
            if (!loop && localTime >= total) return Pair(0f, 0f)

            var elapsed = 0L
            for (sample in samples) {
                if (localTime < elapsed + sample.durationMs) {
                    val inner = localTime - elapsed
                    val period = 1000f / sample.frequencyHz.coerceAtLeast(0.5f)
                    val phase = (inner % period.toLong()) / period
                    val pulse = if (phase < 0.5f) 1f else 0.2f
                    val level = (sample.intensity * pulse * intensity).coerceIn(0f, 1f)
                    return Pair(level, level)
                }
                elapsed += sample.durationMs
            }
            return Pair(0f, 0f)
        }
    }

    /** Volcano — ramp up, peak, decay. */
    data object Volcano : VibrationPattern() {
        private const val CYCLE_MS = 4_000L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val t = (time % CYCLE_MS) / CYCLE_MS.toFloat()
            val envelope = when {
                t < 0.45f -> t / 0.45f
                t < 0.6f -> 1f
                else -> 1f - (t - 0.6f) / 0.4f
            }.coerceIn(0f, 1f)
            val v = envelope * intensity
            return Pair(v, v)
        }
    }

    /** Heartbeat — dub-dub pause. */
    data object Heartbeat : VibrationPattern() {
        private const val CYCLE_MS = 900L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val phase = time % CYCLE_MS
            val beat = when {
                phase < 110 -> 1f
                phase < 190 -> 0.85f
                else -> 0f
            }
            val v = beat * intensity
            return Pair(v, v)
        }
    }

    /** Earthquake — chaotic pseudo-random bursts. */
    data object Earthquake : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val bucket = time / 70L
            val hash = (bucket * 1_103_515_245L).toInt()
            val noise = (hash ushr 16 and 0xFF) / 255f
            val left = if (noise > 0.25f) noise * intensity else 0f
            val right = if (noise > 0.35f) (noise * 0.9f) * intensity else 0f
            return Pair(left.coerceIn(0f, 1f), right.coerceIn(0f, 1f))
        }
    }

    /** Ripple — decaying ring waves. */
    data object Ripple : VibrationPattern() {
        private const val RING_MS = 750L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val ring = (time / RING_MS).toInt()
            val local = (time % RING_MS) / RING_MS.toFloat()
            val decay = 1f / (1f + ring * 0.45f)
            val wave = kotlin.math.sin(local * kotlin.math.PI).toFloat()
            val v = (wave * decay * intensity).coerceIn(0f, 1f)
            return Pair(v, v)
        }
    }

    /** Storm — fast bursts with gaps. */
    data object Storm : VibrationPattern() {
        private const val CYCLE_MS = 600L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val phase = time % CYCLE_MS
            val burst = phase < 180 || (phase in 250..340)
            val v = if (burst) intensity else 0f
            return Pair(v, v * 0.85f)
        }
    }

    /** Breeze — light frequent flutter. */
    data object Breeze : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val period = 120f
            val phase = (time % period.toLong()) / period
            val flutter = 0.25f + 0.75f * kotlin.math.sin(phase * 2f * kotlin.math.PI.toFloat()).let { (it + 1f) / 2f }
            val v = (flutter * intensity * 0.55f).coerceIn(0f, 1f)
            return Pair(v, v)
        }
    }

    /** Hammer — rare strong strikes. */
    data object Hammer : VibrationPattern() {
        private const val CYCLE_MS = 850L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val phase = time % CYCLE_MS
            val strike = phase < 60
            val v = if (strike) intensity else 0f
            return Pair(v, v)
        }
    }

    /** Cascade — stepped intensity falling. */
    data object Cascade : VibrationPattern() {
        private const val STEP_MS = 500L
        private val steps = listOf(1f, 0.75f, 0.5f, 0.35f, 0.2f, 0.1f)

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val index = ((time / STEP_MS) % steps.size).toInt()
            val v = steps[index] * intensity
            return Pair(v, v)
        }
    }

    /** Spark — short bright flashes. */
    data object Spark : VibrationPattern() {
        private const val CYCLE_MS = 350L

        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val phase = time % CYCLE_MS
            val flash = phase < 40 || (phase in 120..150)
            val v = if (flash) intensity else 0f
            return Pair(v, v)
        }
    }
}
