package com.marketplace.user.service.compensation;

import com.marketplace.user.entity.CompensationLog;
import com.marketplace.user.repository.CompensationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Compensation Service Implementation
 * Implements business logic for compensation tracking
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompensationServiceImpl implements CompensationService {

    private final CompensationLogRepository compensationLogRepository;

    @Override
    public CompensationLog logDeduction(String bookingId, Long userId, Long transactionId, BigDecimal amount) {
        log.info("Logging deduction - bookingId: {}, userId: {}, amount: {}", bookingId, userId, amount);

        CompensationLog compensationLog = CompensationLog.builder()
                .bookingId(bookingId)
                .userId(userId)
                .transactionId(transactionId)
                .action(CompensationLog.CompensationAction.DEDUCTED)
                .amount(amount)
                .status(CompensationLog.CompensationStatus.COMPLETED)
                .build();

        CompensationLog savedLog = compensationLogRepository.save(compensationLog);
        log.info("Deduction logged successfully - logId: {}", savedLog.getId());

        return savedLog;
    }

    @Override
    public CompensationLog logRefund(String bookingId, Long userId, Long transactionId, BigDecimal amount) {
        log.info("Logging refund - bookingId: {}, userId: {}, amount: {}", bookingId, userId, amount);

        CompensationLog compensationLog = CompensationLog.builder()
                .bookingId(bookingId)
                .userId(userId)
                .transactionId(transactionId)
                .action(CompensationLog.CompensationAction.REFUNDED)
                .amount(amount)
                .status(CompensationLog.CompensationStatus.COMPLETED)
                .build();

        CompensationLog savedLog = compensationLogRepository.save(compensationLog);
        log.info("Refund logged successfully - logId: {}", savedLog.getId());

        return savedLog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensationLog> getCompensationHistory(Long userId) {
        log.info("Fetching compensation history for user: {}", userId);

        List<CompensationLog> history = compensationLogRepository.findByUserId(userId);
        log.info("Found {} compensation records for user: {}", history.size(), userId);

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensationLog> getBookingCompensationHistory(String bookingId) {
        log.info("Fetching compensation history for booking: {}", bookingId);

        List<CompensationLog> history = compensationLogRepository.findByBookingId(bookingId);
        log.info("Found {} compensation records for booking: {}", history.size(), bookingId);

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensationLog> getPendingCompensations(Long userId) {
        log.info("Fetching pending compensations for user: {}", userId);

        List<CompensationLog> pending = compensationLogRepository.findByUserIdAndStatus(
                userId,
                CompensationLog.CompensationStatus.PENDING
        );
        log.info("Found {} pending compensation records for user: {}", pending.size(), userId);

        return pending;
    }
}
