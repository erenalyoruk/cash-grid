package com.erenalyoruk.cashgrid.auth.service;

import com.erenalyoruk.cashgrid.auth.dto.*;
import com.erenalyoruk.cashgrid.auth.model.Role;
import com.erenalyoruk.cashgrid.auth.model.User;
import com.erenalyoruk.cashgrid.auth.repository.UserRepository;
import com.erenalyoruk.cashgrid.auth.security.JwtTokenProvider;
import com.erenalyoruk.cashgrid.common.exception.BusinessException;
import com.erenalyoruk.cashgrid.common.exception.ConflictException;
import com.erenalyoruk.cashgrid.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("USERNAME_TAKEN", "Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("EMAIL_TAKEN", "Email is already taken");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_ROLE", "Invalid role: " + request.role());
        }

        User user =
                User.builder()
                        .username(request.username())
                        .email(request.email())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .role(role)
                        .build();

        userRepository.save(user);

        log.info("User registered: {} with role {}", user.getUsername(), user.getRole());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user =
                userRepository
                        .findByUsername(request.username())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User", "username", request.username()));

        if (!user.getIsActive()) {
            throw new BusinessException("USER_DISABLED", "User account is disabled");
        }

        log.info("User logged in: {}", user.getUsername());

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException("INVALID_TOKEN", "Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsername(token);

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("User", "username", username));

        log.info("Token refreshed for user: {}", username);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("User", "username", username));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public AuthResponse updateUsername(String currentUsername, UpdateUsernameRequest request) {
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new ConflictException("USERNAME_TAKEN", "Username is already taken");
        }

        User user =
                userRepository
                        .findByUsername(currentUsername)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User", "username", currentUsername));

        user.setUsername(request.newUsername());
        userRepository.save(user);

        log.info("User {} updated their username to {}", currentUsername, request.newUsername());

        return buildAuthResponse(user);
    }

    @Transactional
    public UserResponse updateEmail(String username, UpdateEmailRequest request) {
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new ConflictException("EMAIL_TAKEN", "Email is already taken");
        }

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("User", "username", username));

        user.setEmail(request.newEmail());
        userRepository.save(user);

        log.info("User {} updated their email to {}", username, request.newEmail());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("User", "username", username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("User {} successfully updated their password", username);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken =
                jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken =
                jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
