package com.user.service;

import com.user.domain.Role;
import com.user.domain.User;
import com.user.dto.RegisterRequest;
import com.user.dto.UserResponse;
import com.user.dto.ProfileUpdateRequest;
import com.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        // Uniqueness checks
        if (repo.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (repo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Build new user
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setFullName(req.getFullName());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRole(Role.USER);

        // Persist
        repo.save(u);

        // Map to response
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(u);
    }

    @Transactional
    public UserResponse updateProfile(String username, ProfileUpdateRequest req) {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        repo.save(u);
        return toResponse(u);
    }

    // Helper mapper
    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setRole(u.getRole() != null ? u.getRole().name() : "UNKNOWN");
        return r;
    }

    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }
}