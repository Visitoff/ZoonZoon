package com.seashore.zoonzoon.gamepad.model

/**
 * Manages vibration pattern instances and provides access to available patterns.
 * 
 * This class serves as a central registry for all vibration patterns supported by the system.
 * It provides methods to retrieve pattern instances and manage custom patterns.
 * 
 * **Validates: Requirements 3.2, 3.3, 3.4, 3.5**
 */
class PatternManager {
    private val customPatterns = mutableMapOf<String, VibrationPattern.Custom>()
    
    /**
     * Gets the constant vibration pattern.
     * 
     * @return The constant pattern instance
     */
    fun getConstantPattern(): VibrationPattern.Constant {
        return VibrationPattern.Constant
    }
    
    /**
     * Gets a pulse vibration pattern with the specified frequency.
     * 
     * @param frequencyHz The pulse frequency in Hertz (default: 2.0 Hz)
     * @return A pulse pattern instance
     */
    fun getPulsePattern(frequencyHz: Float = 2.0f): VibrationPattern.Pulse {
        return VibrationPattern.Pulse(frequencyHz)
    }
    
    /**
     * Gets a wave vibration pattern with the specified frequency.
     * 
     * @param frequencyHz The wave frequency in Hertz (default: 1.0 Hz)
     * @return A wave pattern instance
     */
    fun getWavePattern(frequencyHz: Float = 1.0f): VibrationPattern.Wave {
        return VibrationPattern.Wave(frequencyHz)
    }
    
    /**
     * Registers a custom vibration pattern with a unique name.
     * 
     * @param name The unique identifier for this custom pattern
     * @param pattern The custom pattern to register
     * @throws IllegalArgumentException if a pattern with the same name already exists
     */
    fun registerCustomPattern(name: String, pattern: VibrationPattern.Custom) {
        require(!customPatterns.containsKey(name)) {
            "A custom pattern with name '$name' already exists"
        }
        customPatterns[name] = pattern
    }
    
    /**
     * Gets a registered custom pattern by name.
     * 
     * @param name The name of the custom pattern
     * @return The custom pattern, or null if not found
     */
    fun getCustomPattern(name: String): VibrationPattern.Custom? {
        return customPatterns[name]
    }
    
    /**
     * Gets all registered custom pattern names.
     * 
     * @return A set of all custom pattern names
     */
    fun getCustomPatternNames(): Set<String> {
        return customPatterns.keys.toSet()
    }
    
    /**
     * Removes a custom pattern by name.
     * 
     * @param name The name of the custom pattern to remove
     * @return true if the pattern was removed, false if it didn't exist
     */
    fun removeCustomPattern(name: String): Boolean {
        return customPatterns.remove(name) != null
    }
    
    /**
     * Clears all registered custom patterns.
     */
    fun clearCustomPatterns() {
        customPatterns.clear()
    }
    
    /**
     * Gets all available pattern types as a list.
     * This includes built-in patterns and registered custom patterns.
     * 
     * @return A list of all available patterns
     */
    fun getAllPatterns(): List<VibrationPattern> {
        return buildList {
            add(getConstantPattern())
            add(getPulsePattern())
            add(getWavePattern())
            addAll(customPatterns.values)
        }
    }
}
