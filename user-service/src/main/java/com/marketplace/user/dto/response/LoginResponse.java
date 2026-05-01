package com.marketplace.user.dto.response;

import com.marketplace.user.entity.User;

import lombok.Data;

@Data
public class LoginResponse {
    private long userId;
    private String username;
    private String token;
    private User.UserRole role;
    
}
