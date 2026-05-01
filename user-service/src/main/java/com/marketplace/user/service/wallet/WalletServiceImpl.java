package com.marketplace.user.service.wallet;

import com.marketplace.user.entity.Wallet;
import com.marketplace.user.entity.WalletTransaction;
import com.marketplace.user.exception.InsufficientBalanceException;
import com.marketplace.user.exception.UserNotFoundException;
import com.marketplace.user.repository.WalletRepository;
import com.marketplace.user.repository.WalletTransactionRepository;
import com.marketplace.user.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Wallet Service Implementation
 * Implements business logic for wallet operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Wallet getWalletBalance(Long userId) {
        log.info("Fetching wallet balance for user: {}", userId);

        // Verify user exists
        if (!userService.userExists(userId)) {
            log.warn("User not found: {}", userId);
            throw new UserNotFoundException("User not found: " + userId);
        }

        // Get wallet
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for user: {}", userId);
                    return new UserNotFoundException("Wallet not found for user: " + userId);
                });
    }

    @Override
    public Wallet deductBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey) {
        log.info("Deducting balance for user: {}, amount: {}, bookingId: {}", userId, amount, bookingId);

        // Check for duplicate transaction using idempotency key
        if (walletTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate transaction detected with idempotency key: {}", idempotencyKey);
            // Return existing wallet state
            return getWalletBalance(userId);
        }

        // Get wallet
        Wallet wallet = getWalletBalance(userId);

        // Validate sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for user: {}, required: {}, available: {}", userId, amount, wallet.getBalance());
            throw new InsufficientBalanceException("Insufficient balance. Required: " + amount + ", Available: " + wallet.getBalance());
        }

        // Deduct balance (optimistic locking via @Version)
        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Record transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(WalletTransaction.TransactionType.DEBIT)
                .amount(amount)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Balance deducted successfully for user: {}, new balance: {}", userId, updatedWallet.getBalance());

        return updatedWallet;
    }

    @Override
    public Wallet refundBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey) {
        log.info("Refunding balance for user: {}, amount: {}, bookingId: {}", userId, amount, bookingId);

        // Check for duplicate transaction using idempotency key
        if (walletTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate refund detected with idempotency key: {}", idempotencyKey);
            // Return existing wallet state
            return getWalletBalance(userId);
        }

        // Get wallet
        Wallet wallet = getWalletBalance(userId);

        // Add balance
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Record transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(WalletTransaction.TransactionType.CREDIT)
                .amount(amount)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Balance refunded successfully for user: {}, new balance: {}", userId, updatedWallet.getBalance());

        return updatedWallet;
    }

    @Override
    public Wallet addFunds(Long userId, BigDecimal amount) {
        log.info("Adding funds for user: {}, amount: {}", userId, amount);

        // Get wallet
        Wallet wallet = getWalletBalance(userId);

        // Add balance
        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Record transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(WalletTransaction.TransactionType.CREDIT)
                .amount(amount)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .idempotencyKey("ADD_FUNDS_" + userId + "_" + System.currentTimeMillis())
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Funds added successfully for user: {}, new balance: {}", userId, updatedWallet.getBalance());

        return updatedWallet;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateBalance(Long userId, BigDecimal amount) {
        log.info("Validating balance for user: {}, required amount: {}", userId, amount);

        try {
            Wallet wallet = getWalletBalance(userId);
            boolean isValid = wallet.getBalance().compareTo(amount) >= 0;
            log.info("Balance validation result for user: {}, isValid: {}", userId, isValid);
            return isValid;
        } catch (UserNotFoundException e) {
            log.warn("User not found during balance validation: {}", userId);
            return false;
        }
    }

    @Override
    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for user: {}", userId);

        // Verify user exists
        if (!userService.userExists(userId)) {
            log.warn("User not found: {}", userId);
            throw new UserNotFoundException("User not found: " + userId);
        }

        // Check if wallet already exists
        if (walletRepository.existsByUserId(userId)) {
            log.warn("Wallet already exists for user: {}", userId);
            return walletRepository.findByUserId(userId).get();
        }

        // Create new wallet with initial balance of 0
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully for user: {}, walletId: {}", userId, savedWallet.getId());

        return savedWallet;
    }
}
