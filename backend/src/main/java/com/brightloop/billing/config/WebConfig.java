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

    /** CORS filter — allows all origins for the hackathon demo */
    @Bean
    public FilterRegistrationBean<Filter> corsFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((req, res, chain) -> {
            HttpServletResponse r = (HttpServletResponse) res;
            HttpServletRequest  q = (HttpServletRequest) req;
            r.setHeader("Access-Control-Allow-Origin",  q.getHeader("Origin") != null ? q.getHeader("Origin") : "*");
            r.setHeader("Access-Control-Allow-Credentials", "true");
            r.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
            r.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept");
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
