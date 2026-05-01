package com.marketplace.user.dto.response;

import com.marketplace.user.entity.User;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private User.UserRole role;
    private String professionType;
}
