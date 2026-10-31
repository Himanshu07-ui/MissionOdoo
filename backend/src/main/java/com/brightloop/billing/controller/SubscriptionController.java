package com.brightloop.billing.controller;

import com.brightloop.billing.config.JwtFilter;
import com.brightloop.billing.dto.SubscriptionRequest;
import com.brightloop.billing.entity.Subscription;
import com.brightloop.billing.entity.User;
import com.brightloop.billing.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public List<Subscription> list(HttpServletRequest req) {
        User user = (User) req.getAttribute(JwtFilter.ATTR);
        if (user != null && user.getRole() == User.Role.CUSTOMER) {
            return subscriptionService.listForCustomer(user.getId());
        }
        return subscriptionService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> get(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.get(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SubscriptionRequest req) {
        try { return ResponseEntity.ok(subscriptionService.create(req)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/{id}/action")
    public ResponseEntity<?> action(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String action = body.get("action");
            return ResponseEntity.ok(subscriptionService.performAction(id, action));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
