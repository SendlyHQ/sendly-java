package com.sendly.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all exception classes.
 */
class ExceptionsTest {

    // ==================== SendlyException Tests ====================

    @Test
    void testSendlyException_withMessage() {
        SendlyException exception = new SendlyException("Test error");

        assertEquals("Test error", exception.getMessage());
        assertEquals(0, exception.getStatusCode());
        assertNull(exception.getErrorCode());
    }

    @Test
    void testSendlyException_withMessageAndStatusCode() {
        SendlyException exception = new SendlyException("Test error", 500);

        assertEquals("Test error", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
        assertNull(exception.getErrorCode());
    }

    @Test
    void testSendlyException_withMessageStatusCodeAndErrorCode() {
        SendlyException exception = new SendlyException("Test error", 500, "TEST_ERROR");

        assertEquals("Test error", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
        assertEquals("TEST_ERROR", exception.getErrorCode());
    }

    @Test
    void testSendlyException_isRuntimeException() {
        SendlyException exception = new SendlyException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    // ==================== AuthenticationException Tests ====================

    @Test
    void testAuthenticationException_withMessage() {
        AuthenticationException exception = new AuthenticationException("Invalid API key");

        assertEquals("Invalid API key", exception.getMessage());
        assertEquals(401, exception.getStatusCode());
        assertEquals("AUTHENTICATION_ERROR", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_extendsSendlyException() {
        AuthenticationException exception = new AuthenticationException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testAuthenticationException_hasCorrectStatusCode() {
        AuthenticationException exception = new AuthenticationException("Test");
        assertEquals(401, exception.getStatusCode());
    }

    // ==================== ValidationException Tests ====================

    @Test
    void testValidationException_withMessage() {
        ValidationException exception = new ValidationException("Invalid phone number");

        assertEquals("Invalid phone number", exception.getMessage());
        assertEquals(400, exception.getStatusCode());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
    }

    @Test
    void testValidationException_extendsSendlyException() {
        ValidationException exception = new ValidationException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testValidationException_hasCorrectStatusCode() {
        ValidationException exception = new ValidationException("Test");
        assertEquals(400, exception.getStatusCode());
    }

    // ==================== InsufficientCreditsException Tests ====================

    @Test
    void testInsufficientCreditsException_withMessage() {
        InsufficientCreditsException exception = new InsufficientCreditsException("Not enough credits");

        assertEquals("Not enough credits", exception.getMessage());
        assertEquals(402, exception.getStatusCode());
        assertEquals("INSUFFICIENT_CREDITS", exception.getErrorCode());
    }

    @Test
    void testInsufficientCreditsException_extendsSendlyException() {
        InsufficientCreditsException exception = new InsufficientCreditsException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testInsufficientCreditsException_hasCorrectStatusCode() {
        InsufficientCreditsException exception = new InsufficientCreditsException("Test");
        assertEquals(402, exception.getStatusCode());
    }

    // ==================== NotFoundException Tests ====================

    @Test
    void testNotFoundException_withMessage() {
        NotFoundException exception = new NotFoundException("Resource not found");

        assertEquals("Resource not found", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
        assertEquals("NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testNotFoundException_extendsSendlyException() {
        NotFoundException exception = new NotFoundException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testNotFoundException_hasCorrectStatusCode() {
        NotFoundException exception = new NotFoundException("Test");
        assertEquals(404, exception.getStatusCode());
    }

    // ==================== RateLimitException Tests ====================

    @Test
    void testRateLimitException_withMessage() {
        RateLimitException exception = new RateLimitException("Rate limit exceeded");

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertEquals(429, exception.getStatusCode());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(0, exception.getRetryAfter());
    }

    @Test
    void testRateLimitException_withMessageAndRetryAfter() {
        RateLimitException exception = new RateLimitException("Rate limit exceeded", 60);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertEquals(429, exception.getStatusCode());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(60, exception.getRetryAfter());
    }

    @Test
    void testRateLimitException_extendsSendlyException() {
        RateLimitException exception = new RateLimitException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testRateLimitException_hasCorrectStatusCode() {
        RateLimitException exception = new RateLimitException("Test", 30);
        assertEquals(429, exception.getStatusCode());
    }

    @Test
    void testRateLimitException_retryAfterZeroByDefault() {
        RateLimitException exception = new RateLimitException("Test");
        assertEquals(0, exception.getRetryAfter());
    }

    @Test
    void testRateLimitException_retryAfterPreserved() {
        RateLimitException exception = new RateLimitException("Test", 120);
        assertEquals(120, exception.getRetryAfter());
    }

    // ==================== NetworkException Tests ====================

    @Test
    void testNetworkException_withMessage() {
        NetworkException exception = new NetworkException("Connection failed");

        assertEquals("Connection failed", exception.getMessage());
        assertEquals(0, exception.getStatusCode());
        assertEquals("NETWORK_ERROR", exception.getErrorCode());
    }

    @Test
    void testNetworkException_extendsSendlyException() {
        NetworkException exception = new NetworkException("Test");
        assertInstanceOf(SendlyException.class, exception);
    }

    @Test
    void testNetworkException_hasZeroStatusCode() {
        NetworkException exception = new NetworkException("Test");
        assertEquals(0, exception.getStatusCode());
    }

    // ==================== Exception Hierarchy Tests ====================

    @Test
    void testAllExceptionsExtendSendlyException() {
        assertInstanceOf(SendlyException.class, new AuthenticationException("Test"));
        assertInstanceOf(SendlyException.class, new ValidationException("Test"));
        assertInstanceOf(SendlyException.class, new InsufficientCreditsException("Test"));
        assertInstanceOf(SendlyException.class, new NotFoundException("Test"));
        assertInstanceOf(SendlyException.class, new RateLimitException("Test"));
        assertInstanceOf(SendlyException.class, new NetworkException("Test"));
    }

    @Test
    void testAllExceptionsAreRuntimeExceptions() {
        assertInstanceOf(RuntimeException.class, new SendlyException("Test"));
        assertInstanceOf(RuntimeException.class, new AuthenticationException("Test"));
        assertInstanceOf(RuntimeException.class, new ValidationException("Test"));
        assertInstanceOf(RuntimeException.class, new InsufficientCreditsException("Test"));
        assertInstanceOf(RuntimeException.class, new NotFoundException("Test"));
        assertInstanceOf(RuntimeException.class, new RateLimitException("Test"));
        assertInstanceOf(RuntimeException.class, new NetworkException("Test"));
    }

    @Test
    void testExceptionStatusCodes() {
        assertEquals(401, new AuthenticationException("Test").getStatusCode());
        assertEquals(400, new ValidationException("Test").getStatusCode());
        assertEquals(402, new InsufficientCreditsException("Test").getStatusCode());
        assertEquals(404, new NotFoundException("Test").getStatusCode());
        assertEquals(429, new RateLimitException("Test").getStatusCode());
        assertEquals(0, new NetworkException("Test").getStatusCode());
    }

    @Test
    void testExceptionErrorCodes() {
        assertEquals("AUTHENTICATION_ERROR", new AuthenticationException("Test").getErrorCode());
        assertEquals("VALIDATION_ERROR", new ValidationException("Test").getErrorCode());
        assertEquals("INSUFFICIENT_CREDITS", new InsufficientCreditsException("Test").getErrorCode());
        assertEquals("NOT_FOUND", new NotFoundException("Test").getErrorCode());
        assertEquals("RATE_LIMIT_EXCEEDED", new RateLimitException("Test").getErrorCode());
        assertEquals("NETWORK_ERROR", new NetworkException("Test").getErrorCode());
    }

    // ==================== Exception Throwing Tests ====================

    @Test
    void testExceptionsCanBeThrown() {
        assertThrows(SendlyException.class, () -> {
            throw new SendlyException("Test");
        });

        assertThrows(AuthenticationException.class, () -> {
            throw new AuthenticationException("Test");
        });

        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Test");
        });

        assertThrows(InsufficientCreditsException.class, () -> {
            throw new InsufficientCreditsException("Test");
        });

        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("Test");
        });

        assertThrows(RateLimitException.class, () -> {
            throw new RateLimitException("Test");
        });

        assertThrows(NetworkException.class, () -> {
            throw new NetworkException("Test");
        });
    }

    // ==================== Exception Catching Tests ====================

    @Test
    void testAuthenticationExceptionCatchesSendlyException() {
        try {
            throw new AuthenticationException("Test");
        } catch (SendlyException e) {
            assertTrue(e instanceof AuthenticationException);
            assertEquals("Test", e.getMessage());
        }
    }

    @Test
    void testValidationExceptionCatchesSendlyException() {
        try {
            throw new ValidationException("Test");
        } catch (SendlyException e) {
            assertTrue(e instanceof ValidationException);
            assertEquals("Test", e.getMessage());
        }
    }

    @Test
    void testRateLimitExceptionCatchesSendlyException() {
        try {
            throw new RateLimitException("Test", 60);
        } catch (SendlyException e) {
            assertTrue(e instanceof RateLimitException);
            assertEquals(60, ((RateLimitException) e).getRetryAfter());
        }
    }

    @Test
    void testNetworkExceptionCatchesSendlyException() {
        try {
            throw new NetworkException("Test");
        } catch (SendlyException e) {
            assertTrue(e instanceof NetworkException);
            assertEquals("Test", e.getMessage());
        }
    }

    // ==================== Exception Message Preservation Tests ====================

    @Test
    void testExceptionMessagesArePreserved() {
        String testMessage = "This is a test error message";

        assertEquals(testMessage, new SendlyException(testMessage).getMessage());
        assertEquals(testMessage, new AuthenticationException(testMessage).getMessage());
        assertEquals(testMessage, new ValidationException(testMessage).getMessage());
        assertEquals(testMessage, new InsufficientCreditsException(testMessage).getMessage());
        assertEquals(testMessage, new NotFoundException(testMessage).getMessage());
        assertEquals(testMessage, new RateLimitException(testMessage).getMessage());
        assertEquals(testMessage, new NetworkException(testMessage).getMessage());
    }

    @Test
    void testEmptyMessageHandling() {
        SendlyException exception = new SendlyException("");
        assertEquals("", exception.getMessage());
    }

    @Test
    void testLongMessageHandling() {
        String longMessage = "a".repeat(1000);
        SendlyException exception = new SendlyException(longMessage);
        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testSpecialCharactersInMessage() {
        String specialMessage = "Error: Invalid input! @#$%^&*()";
        SendlyException exception = new SendlyException(specialMessage);
        assertEquals(specialMessage, exception.getMessage());
    }
}
