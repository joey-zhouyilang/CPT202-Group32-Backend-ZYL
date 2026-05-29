package com.group32.cpt202.zyl_project.zyl_login;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// 服务层，封装业务逻辑。
/**
 * UserService 类，处理当前模块的业务逻辑或数据结构。
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public User register(User request) {
        if (request == null) {
            throw new RuntimeException("request is required");
        }

        String username = trimToNull(request.getUsername());
        String password = trimToNull(request.getPassword());
        String email = trimToNull(request.getEmail());
        if (username == null) {
            throw new RuntimeException("username is required");
        }
        if (password == null) {
            throw new RuntimeException("password is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("username already exists");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("email already exists");
        }

        com.group32.cpt202.LY_contributor.entity.User entity =
                new com.group32.cpt202.LY_contributor.entity.User();
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setRole(resolveRegistrationRole(request.getRole()));
        entity.setEmail(email);
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setBio(trimToNull(request.getBio()));
        entity.setAvatarUrl(trimToNull(request.getAvatarUrl()));

        return toDto(userRepository.save(entity));
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public User login(String identifier, String password) {
        if (identifier == null || identifier.isBlank()) {
            throw new RuntimeException("username or email is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("password is required");
        }

        String normalizedIdentifier = identifier.trim();
        com.group32.cpt202.LY_contributor.entity.User entity = userRepository.findByUsername(normalizedIdentifier)
                .or(() -> userRepository.findByEmail(normalizedIdentifier))
                .orElseThrow(() -> new RuntimeException("user not found"));

        String storedPassword = entity.getPassword();
        if (storedPassword == null || !password.equals(storedPassword)) {
            throw new RuntimeException("invalid password");
        }
        return toDto(entity);
    }

    public User getProfile(Long userId) {
        com.group32.cpt202.LY_contributor.entity.User entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        return toDto(entity);
    }

    public List<User> listUsers(UserRole role) {
        List<com.group32.cpt202.LY_contributor.entity.User> users = role == null
                ? userRepository.findAllByOrderByIdAsc()
                : userRepository.findByRoleOrderByIdAsc(toSharedRole(role));
        return users.stream().map(this::toDto).toList();
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public User updateProfile(Long userId, User request) {
        com.group32.cpt202.LY_contributor.entity.User entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        String newUsername = trimToNull(request.getUsername());
        if (newUsername != null) {
            boolean duplicated = userRepository.findByUsername(newUsername)
                    .filter(user -> !user.getId().equals(userId))
                    .isPresent();
            if (duplicated) {
                throw new RuntimeException("username already exists");
            }
            entity.setUsername(newUsername);
        }

        String newEmail = trimToNull(request.getEmail());
        if (newEmail != null) {
            boolean duplicatedEmail = userRepository.findByEmail(newEmail)
                    .filter(user -> !user.getId().equals(userId))
                    .isPresent();
            if (duplicatedEmail) {
                throw new RuntimeException("email already exists");
            }
            entity.setEmail(newEmail);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(request.getPassword());
        }

        entity.setPhone(trimToNull(request.getPhone()));
        entity.setBio(trimToNull(request.getBio()));
        entity.setAvatarUrl(trimToNull(request.getAvatarUrl()));

        return toDto(userRepository.save(entity));
    }
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private com.group32.cpt202.LY_contributor.entity.User.Role resolveRegistrationRole(UserRole requestedRole) {
        return requestedRole == UserRole.ADMIN
                ? com.group32.cpt202.LY_contributor.entity.User.Role.ADMIN
                : com.group32.cpt202.LY_contributor.entity.User.Role.USER;
    }

    private User toDto(com.group32.cpt202.LY_contributor.entity.User entity) {
        User dto = new User();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setRole(toModuleRole(entity.getRole()));
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setBio(entity.getBio());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private com.group32.cpt202.LY_contributor.entity.User.Role toSharedRole(UserRole role) {
        return switch (role) {
            case ADMIN -> com.group32.cpt202.LY_contributor.entity.User.Role.ADMIN;
            case CONTRIBUTOR -> com.group32.cpt202.LY_contributor.entity.User.Role.CONTRIBUTOR;
            case USER -> com.group32.cpt202.LY_contributor.entity.User.Role.USER;
        };
    }

    private UserRole toModuleRole(com.group32.cpt202.LY_contributor.entity.User.Role role) {
        return switch (role) {
            case ADMIN -> UserRole.ADMIN;
            case CONTRIBUTOR -> UserRole.CONTRIBUTOR;
            case USER -> UserRole.USER;
        };
    }
}