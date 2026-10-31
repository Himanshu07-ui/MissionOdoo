package com.brightloop.billing.controller;

import com.brightloop.billing.config.JwtFilter;
import com.brightloop.billing.dto.RegisterRequest;
import com.brightloop.billing.entity.User;
import com.brightloop.billing.repository.UserRepository;
import com.brightloop.billing.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepo;
    private final AuthService authService;

    public UserController(UserRepository userRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.authService = authService;
    }

    @GetMapping("/customers")
    public List<User> customers() {
        return userRepo.findByRole(User.Role.CUSTOMER);
    }

    @GetMapping("/users")
    public List<User> users() {
        return userRepo.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest req) {
        try {
            return ResponseEntity.ok(authService.register(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepo.findById(id).map(u -> {
            if (body.containsKey("name")) u.setName(body.get("name"));
            if (body.containsKey("email")) u.setEmail(body.get("email").toLowerCase());
            userRepo.save(u);
            return ResponseEntity.ok((Object) u);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        User current = (User) req.getAttribute(JwtFilter.ATTR);
        if (current != null && current.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete yourself."));
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
