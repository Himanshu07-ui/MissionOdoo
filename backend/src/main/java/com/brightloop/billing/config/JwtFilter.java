package com.brightloop.billing.config;

import com.brightloop.billing.entity.User;
import com.brightloop.billing.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Reads the Authorization: Bearer <token> header, validates the JWT,
 * and stores the authenticated User in a request attribute.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserRepository userRepo;

    public JwtFilter(JwtUtil jwt, UserRepository userRepo) {
        this.jwt = jwt;
        this.userRepo = userRepo;
    }

    public static final String ATTR = "authenticatedUser";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Long userId = jwt.getUserId(token);
                userRepo.findById(userId).ifPresent(u -> req.setAttribute(ATTR, u));
            } catch (Exception ignored) { }
        }
        chain.doFilter(req, res);
    }
}
