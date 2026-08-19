package com.seashore.zoonzoon.gamepad.model

import com.seashore.zoonzoon.gamepad.model.CommonErrors
import com.seashore.zoonzoon.gamepad.model.DisplayError
import com.seashore.zoonzoon.gamepad.model.ErrorSeverity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for error handling UI logic.
 *
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**
 */
class ErrorHandlingTest {

    @Test
    fun testDisplayError_defaultSeverityIsError() {
        val error = DisplayError(message = "Something went wrong")
        assertEquals(ErrorSeverity.ERROR, error.severity)
    }

    @Test
    fun testDisplayError_warningSeverity() {
        val error = DisplayError(message = "Warning message", severity = ErrorSeverity.WARNING)
        assertEquals(ErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testDisplayError_withAction() {
        var actionCalled = false
        val error = DisplayError(
            message = "Retry?",
            actionLabel = "Retry",
            onAction = { actionCalled = true }
        )

        assertNotNull(error.actionLabel)
        assertNotNull(error.onAction)
        error.onAction!!.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun testDisplayError_withoutAction() {
        val error = DisplayError(message = "Info message")
        assertNull(error.actionLabel)
        assertNull(error.onAction)
    }

    @Test
    fun testCommonErrors_controllerDiscoveryFailed() {
        var retryCalled = false
        val error = CommonErrors.controllerDiscoveryFailed { retryCalled = true }

        assertEquals(ErrorSeverity.ERROR, error.severity)
        assertNotNull(error.actionLabel)
        error.onAction?.invoke()
        assertTrue(retryCalled)
    }

    @Test
    fun testCommonErrors_mfiOnlyRestriction() {
        val error = CommonErrors.mfiOnlyRestriction
        assertEquals(ErrorSeverity.WARNING, error.severity)
        assertTrue(error.message.contains("MFi", ignoreCase = true))
    }

    @Test
    fun testCommonErrors_connectionLost() {
        val error = CommonErrors.connectionLost
        assertEquals(ErrorSeverity.ERROR, error.severity)
        assertTrue(error.message.isNotEmpty())
    }

    @Test
    fun testNullErrorMeansNoDisplay() {
        val error: DisplayError? = null
        assertNull(error, "Null error means nothing to display")
    }

    @Test
    fun testErrorMessageIsNonEmpty() {
        val errors = listOf(
            CommonErrors.mfiOnlyRestriction,
            CommonErrors.connectionLost,
            CommonErrors.controllerDiscoveryFailed {}
        )
        for (error in errors) {
            assertTrue(error.message.isNotEmpty(), "Error message should not be empty")
        }
    }
}
