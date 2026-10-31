package com.brightloop.billing.service;

import com.brightloop.billing.config.JwtUtil;
import com.brightloop.billing.dto.AuthResponse;
import com.brightloop.billing.dto.LoginRequest;
import com.brightloop.billing.dto.RegisterRequest;
import com.brightloop.billing.entity.User;
import com.brightloop.billing.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtUtil jwt;

    public AuthService(UserRepository userRepo, JwtUtil jwt) {
        this.userRepo = userRepo;
        this.jwt = jwt;
    }

    @PostConstruct
    public void initSeedPasswords() {
        Map<String, String> demoPasswords = Map.of(
            "admin@brightloop.io", "Admin@1234",
            "staff@brightloop.io", "Staff@1234",
            "cust@acme.com", "Cust@12345",
            "ops@globex.com", "Globex@1234"
        );
        for (Map.Entry<String, String> entry : demoPasswords.entrySet()) {
            userRepo.findByEmail(entry.getKey()).ifPresent(user -> {
                user.setPasswordHash(encoder.encode(entry.getValue()));
                userRepo.save(user);
            });
        }
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect."));
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email or password is incorrect.");
        }
        return toResponse(user);
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }
        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setRole(req.getRole() == null ? User.Role.CUSTOMER : req.getRole());
        userRepo.save(user);
        return toResponse(user);
    }

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwt.generate(u), u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
