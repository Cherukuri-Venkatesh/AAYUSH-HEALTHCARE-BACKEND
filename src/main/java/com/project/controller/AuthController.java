package com.project.controller;

import com.project.dto.LoginRequest;
import com.project.entity.LoginUser;
import com.project.jwt.JwtUtil;
import com.project.repository.LoginUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private LoginUserRepository loginUserRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null || request.getRole() == null) {
            throw new RuntimeException("Email, password and role are required");
        }

        String email = request.getEmail().trim().toLowerCase();
        String role = request.getRole().trim().toUpperCase();

        LoginUser user = loginUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Invalid Email"));

        String storedPassword = user.getPassword();
        
        // Plain text password comparison
        if (storedPassword == null || !storedPassword.equals(request.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        if (!role.equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Invalid Role");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole().toUpperCase());
    }
}