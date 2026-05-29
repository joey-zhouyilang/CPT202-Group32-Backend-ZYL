package com.group32.cpt202.zyl_project.zyl_login;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for {@link UserController}.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("TC-API-REG-01: POST /api/zyl/auth/register returns 200 on success")
    void register_success() throws Exception {
        User response = new User();
        response.setId(1L);
        response.setUsername("alice");
        response.setRole(UserRole.USER);
        response.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(userService.register(any(User.class))).thenReturn(response);

        User request = new User();
        request.setUsername("alice");
        request.setPassword("secret");

        mockMvc.perform(post("/api/zyl/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("TC-API-REG-02: POST /api/zyl/auth/register returns 400 on validation error")
    void register_failure() throws Exception {
        when(userService.register(any(User.class)))
                .thenThrow(new RuntimeException("username already exists"));

        User request = new User();
        request.setUsername("alice");
        request.setPassword("secret");

        mockMvc.perform(post("/api/zyl/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username already exists"));
    }

    @Test
    @DisplayName("TC-API-LOG-01: POST /api/zyl/auth/login returns 200 on success")
    void login_success() throws Exception {
        User response = new User();
        response.setId(1L);
        response.setUsername("alice");
        response.setRole(UserRole.USER);

        when(userService.login("alice", "secret")).thenReturn(response);

        User request = new User();
        request.setUsername("alice");
        request.setPassword("secret");

        mockMvc.perform(post("/api/zyl/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("TC-API-LOG-02: POST /api/zyl/auth/login returns 401 on failure")
    void login_failure() throws Exception {
        when(userService.login("alice", "wrong"))
                .thenThrow(new RuntimeException("invalid password"));

        User request = new User();
        request.setUsername("alice");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/zyl/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("invalid password"));
    }

    @Test
    @DisplayName("TC-API-PRO-01: GET /api/zyl/profile/{id} returns profile")
    void getProfile_success() throws Exception {
        User response = new User();
        response.setId(1L);
        response.setUsername("alice");

        when(userService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/zyl/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("TC-API-PRO-02: PUT /api/zyl/profile/{id} updates profile")
    void updateProfile_success() throws Exception {
        User response = new User();
        response.setId(1L);
        response.setUsername("alice2");
        response.setBio("new bio");

        when(userService.updateProfile(eq(1L), any(User.class))).thenReturn(response);

        User request = new User();
        request.setUsername("alice2");
        request.setBio("new bio");

        mockMvc.perform(put("/api/zyl/profile/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice2"))
                .andExpect(jsonPath("$.bio").value("new bio"));
    }

    @Test
    @DisplayName("TC-API-LST-01: GET /api/zyl/users returns user list")
    void listUsers_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole(UserRole.USER);

        when(userService.listUsers(null)).thenReturn(List.of(user));

        mockMvc.perform(get("/api/zyl/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    @DisplayName("TC-API-LST-02: GET /api/zyl/users?role=ADMIN filters by role")
    void listUsers_byRole() throws Exception {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);

        when(userService.listUsers(UserRole.ADMIN)).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/zyl/users").param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }
}
