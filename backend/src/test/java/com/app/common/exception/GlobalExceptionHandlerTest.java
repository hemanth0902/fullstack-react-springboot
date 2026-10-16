package com.app.common.exception;

import com.app.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentialsReturnsUnauthorized() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleBadCredentials(
                new BadCredentialsException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void handleIllegalStateReturnsConflict() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleIllegalState(new IllegalStateException("Conflict"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Conflict", response.getBody().getMessage());
    }

    @Test
    void handleRuntimeReturnsInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleRuntime(new RuntimeException("Unexpected error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void handleValidationReturnsBadRequestWithFieldErrors()
            throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(
            new FieldError("request", "email", "Invalid email"));
        bindingResult.addError(
            new FieldError("request", "password", "Password is required"));

        Method method = GlobalExceptionHandlerTest.class
            .getDeclaredMethod("validationMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException exception =
            new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response =
            handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("Invalid email", response.getBody().getData().get("email"));
        assertEquals("Password is required",
            response.getBody().getData().get("password"));
    }

    private void validationMethod(String value) {
        // Used only to construct MethodParameter for the validation test.
    }
}
