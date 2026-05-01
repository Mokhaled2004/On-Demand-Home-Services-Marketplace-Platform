package com.marketplace.user.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.user.dto.request.LoginRequest;
import com.marketplace.user.dto.request.RegisterRequest;
import com.marketplace.user.dto.request.UpdateUserRequest;
import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.LoginResponse;
import com.marketplace.user.dto.response.UserResponse;
import com.marketplace.user.entity.User;
import com.marketplace.user.mapper.UserMapper;
import com.marketplace.user.security.JwtTokenProvider;
import com.marketplace.user.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(
                req.getUsername(), req.getEmail(),
                req.getPassword(), req.getRole(), req.getProfessionType(), req.getInitialBalance());

        return ResponseEntity.status(201).body(
                ApiResponse.success("User registered successfully", userMapper.toUserResponse(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setToken(token);
        loginResponse.setRole(user.getRole());

        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(user)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest req) {
        User user = userService.updateUser(userId, req.getEmail(), req.getProfessionType());
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", userMapper.toUserResponse(user)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
