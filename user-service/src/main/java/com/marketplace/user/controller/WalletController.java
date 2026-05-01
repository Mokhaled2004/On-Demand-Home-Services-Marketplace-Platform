package com.marketplace.user.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.user.dto.request.AddFundsRequest;
import com.marketplace.user.dto.request.DeductBalanceRequest;
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
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<WalletResponse>> deductBalance(
            @Valid @RequestBody DeductBalanceRequest req) {
        Wallet wallet = walletService.deductBalance(
            req.getUserId(), 
            req.getAmount(), 
            req.getBookingId(),
            UUID.randomUUID().toString()
        );
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Balance deducted successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<WalletResponse>> refundBalance(
            @Valid @RequestBody RefundRequest req) {
        Wallet wallet = walletService.refundBalance(
            req.getUserId(), 
            req.getAmount(), 
            req.getBookingId(),
            UUID.randomUUID().toString()
        );
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Balance refunded successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/add-funds")
    public ResponseEntity<ApiResponse<WalletResponse>> addFunds(
            @Valid @RequestBody AddFundsRequest req) {
        Wallet wallet = walletService.addFunds(req.getUserId(), req.getAmount());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .message("Funds added successfully")
                .data(walletMapper.toDTO(wallet))
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateBalance(
            @Valid @RequestBody ValidateBalanceRequest req) {
        boolean isValid = walletService.validateBalance(req.getUserId(), req.getAmount());
        return ResponseEntity.ok(
            ApiResponse.builder()
                .status("SUCCESS")
                .data(isValid)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
