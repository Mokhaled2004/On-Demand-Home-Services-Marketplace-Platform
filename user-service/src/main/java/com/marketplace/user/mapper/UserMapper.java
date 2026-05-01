package com.marketplace.user.mapper;

import com.marketplace.user.dto.response.UserResponse;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * User Mapper
 * Maps User entity to UserResponse DTO
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     * @param user the User entity
     * @return UserResponse DTO
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setProfessionType(user.getProfessionType());

        return response;
    }
}
