package com.app.config;

import com.app.auth.JwtAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        JwtAuthFilter jwtAuthFilter = mock(JwtAuthFilter.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
    }

    @Test
    void corsConfigurationHasExpectedOriginsMethodsAndHeaders() {
        CorsConfigurationSource source =
            securityConfig.corsConfigurationSource();

        CorsConfiguration cors = source.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest(
                "GET", "/api/products"));

        assertNotNull(cors);
        assertEquals(
    List.of("http://localhost:3000", "http://localhost:3001"),
    cors.getAllowedOrigins()
);
        assertEquals(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
            cors.getAllowedMethods());
        assertEquals(List.of("*"), cors.getAllowedHeaders());
        assertEquals(Boolean.FALSE, cors.getAllowCredentials());
    }

    @Test
    void passwordEncoderEncodesAndMatchesPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "TestPassword123!";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("WrongPassword", encodedPassword));
    }

    @Test
    void authenticationProviderUsesConfiguredUserDetailsService() {
        assertInstanceOf(
            DaoAuthenticationProvider.class,
            securityConfig.authenticationProvider());
    }

    @Test
    void authenticationManagerDelegatesToAuthenticationConfiguration() throws Exception {
        AuthenticationConfiguration configuration =
            org.mockito.Mockito.mock(AuthenticationConfiguration.class);
        org.springframework.security.authentication.AuthenticationManager manager =
            org.mockito.Mockito.mock(
                org.springframework.security.authentication.AuthenticationManager.class);

        org.mockito.Mockito.when(configuration.getAuthenticationManager())
            .thenReturn(manager);

        assertSame(manager, securityConfig.authenticationManager(configuration));
    }
}
