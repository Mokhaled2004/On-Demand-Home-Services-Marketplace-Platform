package com.marketplace.user.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Email must be a valid email address")
    private String email;

    @Size(max = 100, message = "Profession type must not exceed 100 characters")
    private String professionType;
}
