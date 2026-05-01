package com.marketplace.user.controller;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.user.dto.request.AddFundsRequest;
import com.marketplace.user.dto.request.DeductBalanceRequest;
import com.marketplace.user.dto.request.RefundRequest;
import com.marketplace.user.dto.request.ValidateBalanceRequest;
import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.WalletResponse;
import com.marketplace.user.entity.Wallet;
import com.marketplace.user.mapper.WalletMapper;
import com.marketplace.user.service.wallet.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    /**
     * Get the userId of the currently authenticated user from the JWT token.
     * The JwtAuthenticationFilter stores the userId as the authentication details.
     */
    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getDetails();
    }

    /**
     * GET /wallet/me
     * Get the wallet balance of the currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyBalance() {
        Long userId = getAuthenticatedUserId();
        Wallet wallet = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(walletMapper.toWalletResponse(wallet)));
    }

    /**
     * POST /wallet/add-funds
     * Add funds to the currently logged-in user's wallet.
     * The userId in the request body is IGNORED - always uses the authenticated user.
     */
    @PostMapping("/add-funds")
    public ResponseEntity<ApiResponse<WalletResponse>> addFunds(
            @Valid @RequestBody AddFundsRequest req) {
        Long userId = getAuthenticatedUserId(); // Always from JWT, never from body
        Wallet wallet = walletService.addFunds(userId, req.getAmount());
        return ResponseEntity.ok(
                ApiResponse.success("Funds added successfully", walletMapper.toWalletResponse(wallet)));
    }

    /**
     * POST /wallet/deduct
     * Deduct balance from the currently logged-in user's wallet for a booking.
     * The userId in the request body is IGNORED - always uses the authenticated user.
     */
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<WalletResponse>> deductBalance(
            @Valid @RequestBody DeductBalanceRequest req) {
        Long userId = getAuthenticatedUserId(); // Always from JWT, never from body
        Wallet wallet = walletService.deductBalance(
                userId,
                req.getAmount(),
                req.getBookingId(),
                UUID.randomUUID().toString());
        return ResponseEntity.ok(
                ApiResponse.success("Balance deducted successfully", walletMapper.toWalletResponse(wallet)));
    }

    /**
     * POST /wallet/refund
     * Refund balance to the currently logged-in user's wallet.
     * The userId in the request body is IGNORED - always uses the authenticated user.
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<WalletResponse>> refundBalance(
            @Valid @RequestBody RefundRequest req) {
        Long userId = getAuthenticatedUserId(); // Always from JWT, never from body
        Wallet wallet = walletService.refundBalance(
                userId,
                req.getAmount(),
                req.getBookingId(),
                req.getIdempotencyKey());
        return ResponseEntity.ok(
                ApiResponse.success("Balance refunded successfully", walletMapper.toWalletResponse(wallet)));
    }

    /**
     * POST /wallet/validate
     * Check if the currently logged-in user has sufficient balance.
     * The userId in the request body is IGNORED - always uses the authenticated user.
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateBalance(
            @Valid @RequestBody ValidateBalanceRequest req) {
        Long userId = getAuthenticatedUserId(); // Always from JWT, never from body
        boolean isValid = walletService.validateBalance(userId, req.getAmount());
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}
