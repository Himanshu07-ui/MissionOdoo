package com.brightloop.billing.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

@Configuration
public class WebConfig {

    /** Register the JWT filter for all /api/** routes */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> reg = new FilterRegistrationBean<>(jwtFilter);
        reg.addUrlPatterns("/api/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    /** Permissive CORS filter supporting localhost, file://, private network, and preflight */
    @Bean
    public FilterRegistrationBean<Filter> corsFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((req, res, chain) -> {
            HttpServletResponse r = (HttpServletResponse) res;
            HttpServletRequest  q = (HttpServletRequest) req;

            String origin = q.getHeader("Origin");
            if (origin != null && !origin.isEmpty() && !"null".equalsIgnoreCase(origin)) {
                r.setHeader("Access-Control-Allow-Origin", origin);
                r.setHeader("Access-Control-Allow-Credentials", "true");
            } else {
                r.setHeader("Access-Control-Allow-Origin", "*");
            }

            String reqHeaders = q.getHeader("Access-Control-Request-Headers");
            if (reqHeaders != null && !reqHeaders.isEmpty()) {
                r.setHeader("Access-Control-Allow-Headers", reqHeaders);
            } else {
                r.setHeader("Access-Control-Allow-Headers", "*");
            }

            r.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
            r.setHeader("Access-Control-Allow-Private-Network", "true");
            r.setHeader("Access-Control-Max-Age", "3600");

            if ("OPTIONS".equalsIgnoreCase(q.getMethod())) {
                r.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            chain.doFilter(req, res);
        });
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE - 1);
        return reg;
    }
}
