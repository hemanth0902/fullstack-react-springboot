package com.app.user.controller;

import com.app.user.entity.Role;
import com.app.user.entity.User;
import com.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserRepository userRepository;
    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        user = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        HandlerMethodArgumentResolver principalResolver =
                new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(
                                AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer container,
                            NativeWebRequest request,
                            WebDataBinderFactory binderFactory) {
                        return user;
                    }
                };

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userRepository))
                .setCustomArgumentResolvers(principalResolver)
                .build();
    }

    @Test
    void getCurrentUser_WhenFound_ReturnsUser() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_WhenMissing_ThrowsException() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        UserController controller = new UserController(userRepository);

        assertThrows(
                RuntimeException.class,
                () -> controller.getCurrentUser(
                        org.springframework.security.core.userdetails.User
                                .withUsername("missing@example.com")
                                .password("password")
                                .authorities("ROLE_USER")
                                .build()));
    }

    @Test
    void getAllUsers_WhenUsersExist_ReturnsUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ReturnsEmptyList() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
