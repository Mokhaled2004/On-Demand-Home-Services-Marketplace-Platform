package com.marketplace.user.dto.request;

import lombok.Data;

/**
 * Update User Request DTO
 * Contains fields for updating user profile
 */
@Data
public class UpdateUserRequest {

    private String email;

    private String professionType;
}
