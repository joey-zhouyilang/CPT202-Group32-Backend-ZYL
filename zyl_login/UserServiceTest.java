package com.group32.cpt202.zyl_project.zyl_login;

import com.group32.cpt202.LY_contributor.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService} (zyl login / profile module).
 * Covers registration, authentication, profile retrieval and update flows.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User savedEntity;

    @BeforeEach
    void setUp() {
        savedEntity = new User();
        savedEntity.setId(1L);
        savedEntity.setUsername("alice");
        savedEntity.setPassword("secret");
        savedEntity.setRole(User.Role.USER);
        savedEntity.setEmail("alice@example.com");
        savedEntity.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("TC-REG-01: registers a new user with trimmed fields")
        void register_success() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("  alice  ");
            request.setPassword("secret");
            request.setEmail(" alice@example.com ");
            request.setPhone(" 123 ");
            request.setRole(UserRole.USER);

            when(userRepository.existsByUsername("alice")).thenReturn(false);
            when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedEntity);

            com.group32.cpt202.zyl_project.zyl_login.User result = userService.register(request);

            assertEquals(1L, result.getId());
            assertEquals("alice", result.getUsername());
            assertEquals(UserRole.USER, result.getRole());
            assertEquals("alice@example.com", result.getEmail());
            assertNotNull(result.getCreatedAt());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("alice", captor.getValue().getUsername());
            assertEquals(User.Role.USER, captor.getValue().getRole());
        }

        @Test
        @DisplayName("TC-REG-02: maps ADMIN role on registration")
        void register_adminRole() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("admin");
            request.setPassword("secret");
            request.setRole(UserRole.ADMIN);

            savedEntity.setUsername("admin");
            savedEntity.setRole(User.Role.ADMIN);

            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedEntity);

            com.group32.cpt202.zyl_project.zyl_login.User result = userService.register(request);

            assertEquals(UserRole.ADMIN, result.getRole());
        }

        @Test
        @DisplayName("TC-REG-03: rejects null request")
        void register_nullRequest() {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(null));
            assertEquals("request is required", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-REG-04: rejects blank username")
        void register_missingUsername() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setPassword("secret");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));
            assertEquals("username is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-REG-05: rejects blank password")
        void register_missingPassword() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("alice");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));
            assertEquals("password is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-REG-06: rejects duplicate username")
        void register_duplicateUsername() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("alice");
            request.setPassword("secret");

            when(userRepository.existsByUsername("alice")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));
            assertEquals("username already exists", ex.getMessage());
        }

        @Test
        @DisplayName("TC-REG-07: rejects duplicate email")
        void register_duplicateEmail() {
            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("alice");
            request.setPassword("secret");
            request.setEmail("alice@example.com");

            when(userRepository.existsByUsername("alice")).thenReturn(false);
            when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));
            assertEquals("email already exists", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("TC-LOG-01: logs in with username")
        void login_byUsername() {
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(savedEntity));

            com.group32.cpt202.zyl_project.zyl_login.User result = userService.login("alice", "secret");

            assertEquals("alice", result.getUsername());
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("TC-LOG-02: logs in with email when username not found")
        void login_byEmail() {
            when(userRepository.findByUsername("alice@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(savedEntity));

            com.group32.cpt202.zyl_project.zyl_login.User result =
                    userService.login("alice@example.com", "secret");

            assertEquals("alice", result.getUsername());
        }

        @Test
        @DisplayName("TC-LOG-03: rejects blank identifier")
        void login_missingIdentifier() {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("  ", "secret"));
            assertEquals("username or email is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-LOG-04: rejects blank password")
        void login_missingPassword() {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("alice", ""));
            assertEquals("password is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-LOG-05: rejects unknown user")
        void login_userNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("ghost", "secret"));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("TC-LOG-06: rejects wrong password")
        void login_invalidPassword() {
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(savedEntity));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("alice", "wrong"));
            assertEquals("invalid password", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("profile")
    class ProfileTests {

        @Test
        @DisplayName("TC-PRO-01: returns profile by user id")
        void getProfile_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(savedEntity));

            com.group32.cpt202.zyl_project.zyl_login.User result = userService.getProfile(1L);

            assertEquals(1L, result.getId());
            assertEquals("alice", result.getUsername());
        }

        @Test
        @DisplayName("TC-PRO-02: rejects missing profile")
        void getProfile_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getProfile(99L));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("TC-PRO-03: updates profile fields")
        void updateProfile_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
            when(userRepository.findByUsername("alice2")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("alice2");
            request.setEmail("new@example.com");
            request.setPassword("new-secret");
            request.setBio("Updated bio");

            com.group32.cpt202.zyl_project.zyl_login.User result = userService.updateProfile(1L, request);

            assertEquals("alice2", result.getUsername());
            assertEquals("new@example.com", result.getEmail());
            assertEquals("Updated bio", result.getBio());
        }

        @Test
        @DisplayName("TC-PRO-04: rejects username taken by another user")
        void updateProfile_duplicateUsername() {
            User other = new User();
            other.setId(2L);
            other.setUsername("bob");

            when(userRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
            when(userRepository.findByUsername("bob")).thenReturn(Optional.of(other));

            com.group32.cpt202.zyl_project.zyl_login.User request = new com.group32.cpt202.zyl_project.zyl_login.User();
            request.setUsername("bob");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateProfile(1L, request));
            assertEquals("username already exists", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("listUsers")
    class ListUsersTests {

        @Test
        @DisplayName("TC-LST-01: lists all users when role is null")
        void listUsers_all() {
            when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of(savedEntity));

            List<com.group32.cpt202.zyl_project.zyl_login.User> users = userService.listUsers(null);

            assertEquals(1, users.size());
            assertEquals("alice", users.get(0).getUsername());
        }

        @Test
        @DisplayName("TC-LST-02: filters users by role")
        void listUsers_byRole() {
            savedEntity.setRole(User.Role.CONTRIBUTOR);
            when(userRepository.findByRoleOrderByIdAsc(User.Role.CONTRIBUTOR)).thenReturn(List.of(savedEntity));

            List<com.group32.cpt202.zyl_project.zyl_login.User> users = userService.listUsers(UserRole.CONTRIBUTOR);

            assertEquals(1, users.size());
            assertEquals(UserRole.CONTRIBUTOR, users.get(0).getRole());
        }
    }
}
