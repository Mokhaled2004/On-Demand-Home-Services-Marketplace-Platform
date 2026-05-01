package com.marketplace.user.service.user;

import com.marketplace.user.entity.User;
import com.marketplace.user.exception.InvalidCredentialsException;
import com.marketplace.user.exception.UserAlreadyExistsException;
import com.marketplace.user.exception.UserNotFoundException;
import com.marketplace.user.repository.UserRepository;
import com.marketplace.user.security.PasswordEncoderUtil;
import com.marketplace.user.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service Implementation
 * Implements business logic for user operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoderUtil passwordEncoderUtil;
    private final WalletService walletService;

    @Override
    public User register(String username, String email, String password, User.UserRole role, String professionType) {
        log.info("Registering new user: {}", username);

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        // Create new user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoderUtil.encodePassword(password))
                .role(role)
                .professionType(professionType)
                .isActive(true)
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        // Create wallet for new user
        walletService.createWallet(savedUser.getId());
        log.info("Wallet created for user: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User login(String username, String password) {
        log.info("User login attempt: {}", username);

        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UserNotFoundException("User not found: " + username);
                });

        // Verify password
        if (!passwordEncoderUtil.matchPassword(password, user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            log.warn("User account is inactive: {}", username);
            throw new InvalidCredentialsException("User account is inactive");
        }

        log.info("User login successful: {}", username);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.info("Fetching user: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });
    }

    @Override
    public User updateUser(Long userId, String email, String professionType) {
        log.info("Updating user: {}", userId);

        // Get user
        User user = getUserById(userId);

        // Update email if provided
        if (email != null && !email.isEmpty()) {
            // Check if new email is already used by another user
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                log.warn("Email already exists: {}", email);
                throw new UserAlreadyExistsException("Email already exists: " + email);
            }
            user.setEmail(email);
        }

        // Update profession type if provided
        if (professionType != null && !professionType.isEmpty()) {
            user.setProfessionType(professionType);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return updatedUser;
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            log.warn("User not found: {}", userId);
            throw new UserNotFoundException("User not found: " + userId);
        }

        // Delete user
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }
}
