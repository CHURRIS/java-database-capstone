package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final TokenService tokenService;
    private final Service service;

    public AdminController(AdminRepository adminRepository, TokenService tokenService, Service service) {
        this.adminRepository = adminRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@Valid @RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();
        Admin existing = adminRepository.findByUsername(admin.getUsername());
        if (existing == null || !existing.getPassword().equals(admin.getPassword())) {
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = tokenService.generateToken(existing.getEmail(), "admin");
        response.put("message", "Login successful");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}

