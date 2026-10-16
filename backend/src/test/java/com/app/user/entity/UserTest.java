package com.app.user.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getAuthoritiesReturnsUserRole() {
        User user = User.builder()
            .email("test@example.com")
            .role(Role.ROLE_USER)
            .build();

        Collection<? extends GrantedAuthority> authorities =
            user.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER",
            authorities.iterator().next().getAuthority());
    }

    @Test
    void getUsernameReturnsEmail() {
        User user = User.builder()
            .email("test@example.com")
            .build();

        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    void accountStatusMethodsReturnExpectedValues() {
        User user = User.builder()
            .role(Role.ROLE_USER)
            .enabled(true)
            .build();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void disabledUserIsNotEnabled() {
        User user = User.builder()
            .enabled(false)
            .build();

        assertFalse(user.isEnabled());
    }

    @Test
    void onCreateSetsTimestampsDefaultRoleAndEnabledStatus() {
        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void onCreatePreservesExistingRole() {
        User user = new User();
        user.setRole(Role.ROLE_USER);

        user.onCreate();

        assertEquals(Role.ROLE_USER, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void onUpdateRefreshesUpdatedAt() {
        User user = new User();
        LocalDateTime previous = LocalDateTime.now().minusMinutes(1);
        user.setUpdatedAt(previous);

        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(previous));
    }
}
