package com.marketplace.user.repository;

import com.marketplace.user.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Wallet entity
 * Provides data access methods for wallet operations
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Find wallet by user ID
     * @param userId the user ID to search for
     * @return Optional containing wallet if found
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Check if wallet exists for user
     * @param userId the user ID to check
     * @return true if wallet exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}
