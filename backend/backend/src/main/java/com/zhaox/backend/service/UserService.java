package com.zhaox.backend.service;

import com.zhaox.backend.entity.User;
import com.zhaox.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate a user by username and password.
     * Supports both BCrypt-hashed and plain-text stored passwords so legacy data still works.
     */
    public User authenticateUser(String username, String rawPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            return null;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        String storedPassword = Optional.ofNullable(user.getPassword()).orElse("");
        boolean passwordMatches = passwordEncoder.matches(rawPassword, storedPassword) || storedPassword.equals(rawPassword);

        if (!passwordMatches) {
            return null;
        }

        // 如果 status 存在且是禁用状态（0），直接拒绝登录
        if ("0".equals(user.getStatus())) {
            return null;
        }

        return user;
    }
}
