package com.marketplace.user.controller;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.user.dto.request.LoginRequest;
import com.marketplace.user.dto.request.RegisterRequest;
import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.LoginResponse;
import com.marketplace.user.dto.response.UserResponse;
import com.marketplace.user.entity.User;
import com.marketplace.user.mapper.UserMapper;
import com.marketplace.user.service.user.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.getUsername(), req.getEmail(),
        req.getPassword(), req.getRole(), req.getProfessionType());
        
        return ResponseEntity.status(201).body(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("User registered successfully")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtTokenProvider.generateToken(user.getId());
        
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .token(token)
                    .role(user.getRole())
                    .build())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest req) {
        User user = userService.updateUser(userId, req.getEmail(), req.getProfessionType());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("User updated successfully")
                .data(userMapper.toDTO(user))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(204).build();
    }
    
}
