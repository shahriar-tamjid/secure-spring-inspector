package com.example.samplevulnerable.config;

public class SecurityConfig {
    public void configure(Object http) {
        // Educational fixture: intentionally insecure and never used as a real service.
        http.csrf(csrf -> csrf.disable());
    }
}
