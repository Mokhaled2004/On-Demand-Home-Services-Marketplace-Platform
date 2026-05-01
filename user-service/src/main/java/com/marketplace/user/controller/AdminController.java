package com.marketplace.user.controller;

import com.marketplace.user.dto.response.*;
import com.marketplace.user.entity.CompensationLog;
import com.marketplace.user.entity.User;
import com.marketplace.user.entity.Wallet;
import com.marketplace.user.entity.WalletTransaction;
import com.marketplace.user.mapper.UserMapper;
import com.marketplace.user.repository.CompensationLogRepository;
import com.marketplace.user.repository.UserRepository;
import com.marketplace.user.repository.WalletRepository;
import com.marketplace.user.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin Controller
 * All endpoints here require ADMIN role.
 * Authorization is enforced via JWT - only users with role=ADMIN can access these.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CompensationLogRepository compensationLogRepository;
    private final UserMapper userMapper;

    /**
     * GET /admin/users
     * View all registered users in the system.
     * ADMIN ONLY.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Admin: fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        log.info("Admin: found {} users", users.size());
        return ResponseEntity.ok(ApiResponse.success("All users retrieved successfully", users));
    }

    /**
     * GET /admin/transactions
     * View all wallet transactions across all users.
     * This is the money movement history (DEBIT, CREDIT, REFUND).
     * ADMIN ONLY.
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        log.info("Admin: fetching all wallet transactions");

        List<WalletTransaction> transactions = walletTransactionRepository.findAll();

        List<TransactionResponse> response = transactions.stream()
                .map(tx -> {
                    TransactionResponse dto = new TransactionResponse();
                    dto.setId(tx.getId());
                    dto.setWalletId(tx.getWalletId());
                    dto.setTransactionType(tx.getTransactionType());
                    dto.setAmount(tx.getAmount());
                    dto.setReferenceId(tx.getReferenceId());
                    dto.setDescription(tx.getDescription());
                    dto.setStatus(tx.getStatus());
                    dto.setCreatedAt(tx.getCreatedAt());

                    // Enrich with userId and username from wallet
                    walletRepository.findById(tx.getWalletId()).ifPresent(wallet -> {
                        dto.setUserId(wallet.getUserId());
                        userRepository.findById(wallet.getUserId()).ifPresent(user ->
                                dto.setUsername(user.getUsername()));
                    });

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Admin: found {} transactions", response.size());
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved successfully", response));
    }

    /**
     * GET /admin/compensation-log
     * View all compensation log entries (booking-related deductions and refunds).
     * ADMIN ONLY.
     */
    @GetMapping("/compensation-log")
    public ResponseEntity<ApiResponse<List<CompensationLogResponse>>> getAllCompensationLogs() {
        log.info("Admin: fetching all compensation logs");

        List<CompensationLog> logs = compensationLogRepository.findAll();

        List<CompensationLogResponse> response = logs.stream()
                .map(cl -> {
                    CompensationLogResponse dto = new CompensationLogResponse();
                    dto.setId(cl.getId());
                    dto.setBookingId(cl.getBookingId());
                    dto.setUserId(cl.getUserId());
                    dto.setTransactionId(cl.getTransactionId());
                    dto.setAction(cl.getAction());
                    dto.setAmount(cl.getAmount());
                    dto.setStatus(cl.getStatus());
                    dto.setReason(cl.getReason());
                    dto.setCreatedAt(cl.getCreatedAt());

                    // Enrich with username
                    userRepository.findById(cl.getUserId()).ifPresent(user ->
                            dto.setUsername(user.getUsername()));

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Admin: found {} compensation log entries", response.size());
        return ResponseEntity.ok(ApiResponse.success("All compensation logs retrieved successfully", response));
    }

    /**
     * GET /admin/users/{userId}/transactions
     * View all wallet transactions for a specific user.
     * ADMIN ONLY.
     */
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @PathVariable Long userId) {
        log.info("Admin: fetching transactions for user: {}", userId);

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No wallet found for user", List.of()));
        }

        Wallet wallet = walletOpt.get();
        String username = userRepository.findById(userId)
                .map(User::getUsername).orElse("unknown");

        List<TransactionResponse> response = walletTransactionRepository
                .findByWalletId(wallet.getId())
                .stream()
                .map(tx -> {
                    TransactionResponse dto = new TransactionResponse();
                    dto.setId(tx.getId());
                    dto.setWalletId(tx.getWalletId());
                    dto.setUserId(userId);
                    dto.setUsername(username);
                    dto.setTransactionType(tx.getTransactionType());
                    dto.setAmount(tx.getAmount());
                    dto.setReferenceId(tx.getReferenceId());
                    dto.setDescription(tx.getDescription());
                    dto.setStatus(tx.getStatus());
                    dto.setCreatedAt(tx.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Admin: found {} transactions for user: {}", response.size(), userId);
        return ResponseEntity.ok(ApiResponse.success("User transactions retrieved successfully", response));
    }

    /**
     * GET /admin/users/{userId}/compensation-log
     * View all compensation log entries for a specific user.
     * ADMIN ONLY.
     */
    @GetMapping("/users/{userId}/compensation-log")
    public ResponseEntity<ApiResponse<List<CompensationLogResponse>>> getUserCompensationLog(
            @PathVariable Long userId) {
        log.info("Admin: fetching compensation log for user: {}", userId);

        String username = userRepository.findById(userId)
                .map(User::getUsername).orElse("unknown");

        List<CompensationLogResponse> response = compensationLogRepository
                .findByUserId(userId)
                .stream()
                .map(cl -> {
                    CompensationLogResponse dto = new CompensationLogResponse();
                    dto.setId(cl.getId());
                    dto.setBookingId(cl.getBookingId());
                    dto.setUserId(cl.getUserId());
                    dto.setUsername(username);
                    dto.setTransactionId(cl.getTransactionId());
                    dto.setAction(cl.getAction());
                    dto.setAmount(cl.getAmount());
                    dto.setStatus(cl.getStatus());
                    dto.setReason(cl.getReason());
                    dto.setCreatedAt(cl.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Admin: found {} compensation entries for user: {}", response.size(), userId);
        return ResponseEntity.ok(ApiResponse.success("User compensation log retrieved successfully", response));
    }
}
