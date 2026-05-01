package com.marketplace.user.dto.request;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data @Validated
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;

}
