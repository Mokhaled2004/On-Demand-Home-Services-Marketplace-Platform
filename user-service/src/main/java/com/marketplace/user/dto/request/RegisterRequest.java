package com.marketplace.user.dto.request;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

import com.marketplace.user.entity.User;

@Data @Validated
public class RegisterRequest {
    @NotBlank private String username;
    @Email private String email;
    @NotBlank @Size(min=8) private String password;
    @NotNull private User.UserRole role;
    private String professionType;
}

