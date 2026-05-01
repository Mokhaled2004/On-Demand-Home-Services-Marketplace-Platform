package com.marketplace.user.repository;

import com.marketplace.user.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WalletTransaction entity
 * Provides data access methods for wallet transaction operations
 */
@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    /**
     * Find all transactions for a wallet
     * @param walletId the wallet ID to search for
     * @return List of transactions for the wallet
     */
    List<WalletTransaction> findByWalletId(Long walletId);

    /**
     * Find transactions by wallet ID and status
     * @param walletId the wallet ID to search for
     * @param status the transaction status to filter by
     * @return List of transactions matching the criteria
     */
    List<WalletTransaction> findByWalletIdAndStatus(Long walletId, WalletTransaction.TransactionStatus status);

    /**
     * Find transaction by idempotency key
     * Used to prevent duplicate transactions
     * @param idempotencyKey the unique idempotency key
     * @return Optional containing transaction if found
     */
    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Check if transaction exists by idempotency key
     * @param idempotencyKey the unique idempotency key
     * @return true if transaction exists, false otherwise
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
