package com.app.config;

import com.app.user.entity.Role;
import com.app.user.entity.User;
import com.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationConfigTest {

    @Test
    void userDetailsService_WhenUserExists_ReturnsUser() {
        UserRepository repository = mock(UserRepository.class);

        User user = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        when(repository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        ApplicationConfig config = new ApplicationConfig(repository);
        UserDetailsService service = config.userDetailsService();

        UserDetails result = service.loadUserByUsername("test@example.com");

        assertSame(user, result);
        assertEquals("test@example.com", result.getUsername());
        verify(repository).findByEmail("test@example.com");
    }

    @Test
    void userDetailsService_WhenUserDoesNotExist_ThrowsException() {
        UserRepository repository = mock(UserRepository.class);

        when(repository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        ApplicationConfig config = new ApplicationConfig(repository);
        UserDetailsService service = config.userDetailsService();

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));

        assertEquals(
                "User not found: missing@example.com",
                exception.getMessage());

        verify(repository).findByEmail("missing@example.com");
    }
}
