package com.marketplace.user.service.user;

import com.marketplace.user.entity.User;

/**
 * User Service Interface
 * Defines business logic for user operations
 */
public interface UserService {

    /**
     * Register a new user
     * @param username unique username
     * @param email unique email
     * @param password plain text password (will be hashed)
     * @param role CUSTOMER or SERVICE_PROVIDER
     * @param professionType profession if SERVICE_PROVIDER
     * @return saved User entity
     * @throws com.marketplace.user.exception.UserAlreadyExistsException if username or email exists
     */
    User register(String username, String email, String password, User.UserRole role, String professionType);

    /**
     * Authenticate user with credentials
     * @param username username
     * @param password plain text password
     * @return authenticated User entity
     * @throws com.marketplace.user.exception.InvalidCredentialsException if credentials invalid
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    User login(String username, String password);

    /**
     * Get user by ID
     * @param userId user ID
     * @return User entity
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    User getUserById(Long userId);

    /**
     * Update user profile
     * @param userId user ID
     * @param email new email (optional, can be null)
     * @param professionType new profession type (optional, can be null)
     * @return updated User entity
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    User updateUser(Long userId, String email, String professionType);

    /**
     * Delete user account
     * @param userId user ID
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    void deleteUser(Long userId);

    /**
     * Check if user exists
     * @param userId user ID
     * @return true if exists, false otherwise
     */
    boolean userExists(Long userId);
}
