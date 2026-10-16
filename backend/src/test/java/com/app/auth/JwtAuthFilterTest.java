package com.app.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthFilter jwtAuthFilter;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        userDetails = User.withUsername("test@example.com")
            .password("test-password")
            .authorities("ROLE_USER")
            .build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void executeFilter() throws ServletException, IOException {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void missingAuthorizationHeaderContinuesChain()
            throws ServletException, IOException {
        executeFilter();

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void nonBearerAuthorizationHeaderContinuesChain()
            throws ServletException, IOException {
        request.addHeader("Authorization", "Basic abc123");

        executeFilter();

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void validTokenSetsAuthentication()
            throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.extractUsername("valid-token"))
            .thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
            .thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails))
            .thenReturn(true);

        executeFilter();

        assertNotNull(
            SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
            "test@example.com",
            SecurityContextHolder.getContext()
                .getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidTokenDoesNotSetAuthentication()
            throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.extractUsername("invalid-token"))
            .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

        executeFilter();

        assertNull(
            SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void existingAuthenticationIsNotReplaced()
            throws ServletException, IOException {
        UsernamePasswordAuthenticationToken existing =
            new UsernamePasswordAuthenticationToken(
                "existing-user", null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existing);

        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.extractUsername("valid-token"))
            .thenReturn("test@example.com");

        executeFilter();

        assertSame(
            existing,
            SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never())
            .loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenWithInvalidValidationDoesNotAuthenticate()
            throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer expired-token");
        when(jwtService.extractUsername("expired-token"))
            .thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
            .thenReturn(userDetails);
        when(jwtService.isTokenValid("expired-token", userDetails))
            .thenReturn(false);

        executeFilter();

        assertNull(
            SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
