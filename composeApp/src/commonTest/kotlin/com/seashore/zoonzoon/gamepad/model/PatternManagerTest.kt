package com.seashore.zoonzoon.gamepad.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for PatternManager.
 * 
 * Tests pattern retrieval, custom pattern management, and edge cases.
 */
class PatternManagerTest {
    
    @Test
    fun testGetConstantPattern() {
        val manager = PatternManager()
        val pattern = manager.getConstantPattern()
        
        assertNotNull(pattern)
        assertTrue(pattern is VibrationPattern.Constant)
    }
    
    @Test
    fun testGetPulsePatternWithDefaultFrequency() {
        val manager = PatternManager()
        val pattern = manager.getPulsePattern()
        
        assertNotNull(pattern)
        assertTrue(pattern is VibrationPattern.Pulse)
        assertEquals(2.0f, pattern.frequencyHz)
    }
    
    @Test
    fun testGetPulsePatternWithCustomFrequency() {
        val manager = PatternManager()
        val pattern = manager.getPulsePattern(frequencyHz = 5.0f)
        
        assertNotNull(pattern)
        assertTrue(pattern is VibrationPattern.Pulse)
        assertEquals(5.0f, pattern.frequencyHz)
    }
    
    @Test
    fun testGetWavePatternWithDefaultFrequency() {
        val manager = PatternManager()
        val pattern = manager.getWavePattern()
        
        assertNotNull(pattern)
        assertTrue(pattern is VibrationPattern.Wave)
        assertEquals(1.0f, pattern.frequencyHz)
    }
    
    @Test
    fun testGetWavePatternWithCustomFrequency() {
        val manager = PatternManager()
        val pattern = manager.getWavePattern(frequencyHz = 3.0f)
        
        assertNotNull(pattern)
        assertTrue(pattern is VibrationPattern.Wave)
        assertEquals(3.0f, pattern.frequencyHz)
    }
    
    @Test
    fun testRegisterCustomPattern() {
        val manager = PatternManager()
        val customPattern = VibrationPattern.Custom(
            frames = listOf(
                VibrationPattern.Custom.Frame(1.0f, 0.5f, 100),
                VibrationPattern.Custom.Frame(0.5f, 1.0f, 100)
            ),
            loop = true
        )
        
        manager.registerCustomPattern("test-pattern", customPattern)
        
        val retrieved = manager.getCustomPattern("test-pattern")
        assertNotNull(retrieved)
        assertEquals(customPattern, retrieved)
    }
    
    @Test
    fun testRegisterCustomPatternWithDuplicateName() {
        val manager = PatternManager()
        val customPattern1 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100)),
            loop = true
        )
        val customPattern2 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100)),
            loop = true
        )
        
        manager.registerCustomPattern("duplicate", customPattern1)
        
        assertFailsWith<IllegalArgumentException> {
            manager.registerCustomPattern("duplicate", customPattern2)
        }
    }
    
    @Test
    fun testGetCustomPatternNotFound() {
        val manager = PatternManager()
        
        val retrieved = manager.getCustomPattern("non-existent")
        assertNull(retrieved)
    }
    
    @Test
    fun testGetCustomPatternNames() {
        val manager = PatternManager()
        val pattern1 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100)),
            loop = true
        )
        val pattern2 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100)),
            loop = true
        )
        
        manager.registerCustomPattern("pattern1", pattern1)
        manager.registerCustomPattern("pattern2", pattern2)
        
        val names = manager.getCustomPatternNames()
        assertEquals(2, names.size)
        assertTrue(names.contains("pattern1"))
        assertTrue(names.contains("pattern2"))
    }
    
    @Test
    fun testGetCustomPatternNamesEmpty() {
        val manager = PatternManager()
        
        val names = manager.getCustomPatternNames()
        assertTrue(names.isEmpty())
    }
    
    @Test
    fun testRemoveCustomPattern() {
        val manager = PatternManager()
        val customPattern = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100)),
            loop = true
        )
        
        manager.registerCustomPattern("to-remove", customPattern)
        assertTrue(manager.removeCustomPattern("to-remove"))
        
        val retrieved = manager.getCustomPattern("to-remove")
        assertNull(retrieved)
    }
    
    @Test
    fun testRemoveCustomPatternNotFound() {
        val manager = PatternManager()
        
        assertFalse(manager.removeCustomPattern("non-existent"))
    }
    
    @Test
    fun testClearCustomPatterns() {
        val manager = PatternManager()
        val pattern1 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100)),
            loop = true
        )
        val pattern2 = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100)),
            loop = true
        )
        
        manager.registerCustomPattern("pattern1", pattern1)
        manager.registerCustomPattern("pattern2", pattern2)
        
        manager.clearCustomPatterns()
        
        assertTrue(manager.getCustomPatternNames().isEmpty())
        assertNull(manager.getCustomPattern("pattern1"))
        assertNull(manager.getCustomPattern("pattern2"))
    }
    
    @Test
    fun testGetAllPatternsWithoutCustom() {
        val manager = PatternManager()
        
        val patterns = manager.getAllPatterns()
        
        assertEquals(3, patterns.size)
        assertTrue(patterns.any { it is VibrationPattern.Constant })
        assertTrue(patterns.any { it is VibrationPattern.Pulse })
        assertTrue(patterns.any { it is VibrationPattern.Wave })
    }
    
    @Test
    fun testGetAllPatternsWithCustom() {
        val manager = PatternManager()
        val customPattern = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100)),
            loop = true
        )
        
        manager.registerCustomPattern("custom", customPattern)
        
        val patterns = manager.getAllPatterns()
        
        assertEquals(4, patterns.size)
        assertTrue(patterns.any { it is VibrationPattern.Constant })
        assertTrue(patterns.any { it is VibrationPattern.Pulse })
        assertTrue(patterns.any { it is VibrationPattern.Wave })
        assertTrue(patterns.any { it is VibrationPattern.Custom })
    }
    
    @Test
    fun testPatternCalculateFrameIntegration() {
        // Test that patterns retrieved from manager work correctly
        val manager = PatternManager()
        
        // Test constant pattern
        val constant = manager.getConstantPattern()
        val (left1, right1) = constant.calculateFrame(0, 0.5f)
        assertEquals(0.5f, left1)
        assertEquals(0.5f, right1)
        
        // Test pulse pattern
        val pulse = manager.getPulsePattern(2.0f)
        val (left2, right2) = pulse.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left2)
        assertEquals(1.0f, right2)
        
        // Test wave pattern
        val wave = manager.getWavePattern(1.0f)
        val (left3, right3) = wave.calculateFrame(0, 1.0f)
        assertTrue(left3 >= 0.0f && left3 <= 1.0f)
        assertTrue(right3 >= 0.0f && right3 <= 1.0f)
    }
}
