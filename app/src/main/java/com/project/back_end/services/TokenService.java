package com.project.back_end.services;

import org.springframework.stereotype.Component;

@Component
public class TokenService {

    public String generateToken(String email, String role) {
        if (email == null || email.isBlank() || role == null || role.isBlank()) {
            return "invalid";
        }
        return String.format("%s|%s|%d", email.trim(), role.trim(), System.currentTimeMillis());
    }

    public String extractEmail(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String normalizedToken = token.trim();
        if (normalizedToken.startsWith("Bearer ")) {
            normalizedToken = normalizedToken.substring(7);
        }
        String[] parts = normalizedToken.split("\\|");
        return parts.length >= 1 ? parts[0] : null;
    }

    public boolean validateToken(String token, String role) {
        if (token == null || token.isBlank() || role == null || role.isBlank()) {
            return false;
        }

        String normalizedToken = token.trim();
        if (normalizedToken.startsWith("Bearer ")) {
            normalizedToken = normalizedToken.substring(7);
        }

        String[] parts = normalizedToken.split("\\|");
        if (parts.length < 2) {
            return false;
        }

        return !parts[0].isBlank() && parts[1].equalsIgnoreCase(role);
    }
}
