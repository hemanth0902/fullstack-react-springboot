package com.app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET =
        "01234567890123456789012345678901";

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        Field secretField = JwtService.class.getDeclaredField("secretKey");
        secretField.setAccessible(true);
        secretField.set(jwtService, SECRET);

        Field expirationField =
            JwtService.class.getDeclaredField("jwtExpiration");
        expirationField.setAccessible(true);
        expirationField.setLong(jwtService, 3600000L);

        userDetails = User.withUsername("test@example.com")
            .password("test-password")
            .authorities("ROLE_USER")
            .build();
    }

    @Test
    void generateTokenAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void generatedTokenContainsRoleClaim() {
        String token = jwtService.generateToken(userDetails);

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(token)
            .getBody();

        assertEquals("ROLE_USER", claims.get("role"));
    }

    @Test
    void validTokenIsAccepted() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void mismatchedUsernameIsRejected() {
        String token = jwtService.generateToken(userDetails);
        UserDetails anotherUser = User.withUsername("other@example.com")
            .password("test-password")
            .authorities("ROLE_USER")
            .build();

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }

    @Test
    void expiredTokenIsRejected() {
        Date now = new Date();
        String token = Jwts.builder()
            .setSubject("test@example.com")
            .setIssuedAt(new Date(now.getTime() - 10000))
            .setExpiration(new Date(now.getTime() - 1000))
            .signWith(
                Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256)
            .compact();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void invalidTokenSignatureIsRejected() {
        String token = jwtService.generateToken(userDetails);
        String wrongSecret = "abcdefghijklmnopqrstuvwxyz123456";

        String invalidToken = Jwts.builder()
            .setSubject("test@example.com")
            .setExpiration(new Date(System.currentTimeMillis() + 60000))
            .signWith(
                Keys.hmacShaKeyFor(
                    wrongSecret.getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256)
            .compact();

        assertThrows(
            io.jsonwebtoken.JwtException.class,
            () -> jwtService.extractUsername(invalidToken));
    }
}
