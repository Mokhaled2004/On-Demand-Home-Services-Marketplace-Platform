package com.marketplace.bookingservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * HTTP client for User Service (port 8081).
 * Handles user verification and wallet operations.
 *
 * NOTE: wallet endpoints (/wallet/deduct, /wallet/refund) require the
 * customer's JWT token — they identify the user from the token, not the body.
 * We forward the token received from the original request.
 */
@ApplicationScoped
public class UserServiceClient {

    private static final Logger LOG = Logger.getLogger(UserServiceClient.class.getName());
    private static final String USER_SERVICE_BASE = "http://localhost:8081";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserServiceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    /**
     * Verify a user exists in User Service.
     * GET /users/{userId}
     *
     * @return the user's data as a JsonNode, or null if not found
     */
    public JsonNode getUser(Long userId, String jwtToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USER_SERVICE_BASE + "/users/" + userId))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode body = objectMapper.readTree(response.body());
                return body.get("data");
            } else {
                LOG.warning("User Service returned " + response.statusCode() + " for userId=" + userId);
                return null;
            }
        } catch (Exception e) {
            LOG.severe("Failed to call User Service getUser: " + e.getMessage());
            throw new RuntimeException("User Service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Deduct wallet balance for a booking.
     * POST /wallet/deduct
     * Body: { "amount": X, "bookingId": Y }
     *
     * The JWT token identifies WHICH user's wallet to deduct from.
     *
     * @return true if deduction succeeded, false if insufficient balance
     */
    public boolean deductWallet(BigDecimal amount, Long bookingId, String jwtToken) {
        try {
            String body = String.format(
                    "{\"amount\": %s, \"bookingId\": %d}",
                    amount.toPlainString(), bookingId
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USER_SERVICE_BASE + "/wallet/deduct"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LOG.info("Wallet deducted successfully for bookingId=" + bookingId);
                return true;
            } else {
                LOG.warning("Wallet deduction failed. Status=" + response.statusCode()
                        + " Body=" + response.body());
                return false;
            }
        } catch (Exception e) {
            LOG.severe("Failed to call User Service deductWallet: " + e.getMessage());
            throw new RuntimeException("User Service unavailable during deduction: " + e.getMessage(), e);
        }
    }

    /**
     * Refund wallet balance (compensation / rollback).
     * POST /wallet/refund
     * Body: { "amount": X, "bookingId": Y, "idempotencyKey": "..." }
     *
     * @return true if refund succeeded
     */
    public boolean refundWallet(BigDecimal amount, Long bookingId, String idempotencyKey, String jwtToken) {
        try {
            String body = String.format(
                    "{\"amount\": %s, \"bookingId\": %d, \"idempotencyKey\": \"%s\"}",
                    amount.toPlainString(), bookingId, idempotencyKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USER_SERVICE_BASE + "/wallet/refund"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LOG.info("Wallet refunded successfully for bookingId=" + bookingId);
                return true;
            } else {
                LOG.warning("Wallet refund failed. Status=" + response.statusCode()
                        + " Body=" + response.body());
                return false;
            }
        } catch (Exception e) {
            LOG.severe("Failed to call User Service refundWallet: " + e.getMessage());
            // Don't rethrow — refund failure should be logged but not crash the response
            return false;
        }
    }
}
