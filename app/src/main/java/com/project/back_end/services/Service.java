package com.project.back_end.services;

import org.springframework.stereotype.Service;

@Service
public class Service {

    private final TokenService tokenService;

    public Service(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public boolean validateToken(String token, String role) {
        if (token == null || token.isBlank()) {
            return false;
        }

        return tokenService.validateToken(token, role);
    }
