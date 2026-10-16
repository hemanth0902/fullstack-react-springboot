package com.app.auth;

import com.app.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void registerReturnsCreated() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Mahesh");
        request.setLastName("Patluri");
        request.setEmail("mahesh@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("test-token");
        authResponse.setEmail("mahesh@example.com");
        authResponse.setRole("ROLE_USER");

        when(authService.register(request)).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
            authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-token", response.getBody().getData().getToken());

        verify(authService).register(request);
    }

    @Test
    void loginReturnsOk() {
        LoginRequest request = new LoginRequest();
        request.setEmail("mahesh@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("test-token");
        authResponse.setEmail("mahesh@example.com");
        authResponse.setRole("ROLE_USER");

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
            authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-token", response.getBody().getData().getToken());

        verify(authService).login(request);
    }
}
