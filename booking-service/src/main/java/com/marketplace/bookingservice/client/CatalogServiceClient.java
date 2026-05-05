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
 * HTTP client for Catalog Service (port 8083).
 * Retrieves service offer details and checks availability.
 */
@ApplicationScoped
public class CatalogServiceClient {

    private static final Logger LOG = Logger.getLogger(CatalogServiceClient.class.getName());
    private static final String CATALOG_SERVICE_BASE = "http://localhost:8083";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CatalogServiceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    /**
     * Get service offer details from Catalog Service.
     * GET /services/{offerId}
     *
     * @return JsonNode with offer data (id, providerId, price, status, etc.)
     *         or null if not found / not active
     */
    public JsonNode getServiceOffer(Long offerId, String jwtToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CATALOG_SERVICE_BASE + "/services/" + offerId))
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
                LOG.warning("Catalog Service returned " + response.statusCode()
                        + " for offerId=" + offerId);
                return null;
            }
        } catch (Exception e) {
            LOG.severe("Failed to call Catalog Service getServiceOffer: " + e.getMessage());
            throw new RuntimeException("Catalog Service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a service offer is currently available.
     * GET /services/{offerId}/availability
     *
     * @return true if the offer is ACTIVE and within its availability window
     */
    public boolean checkAvailability(Long offerId, String jwtToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CATALOG_SERVICE_BASE + "/services/" + offerId + "/availability"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode body = objectMapper.readTree(response.body());
                JsonNode data = body.get("data");
                if (data != null && data.has("isAvailable")) {
                    return data.get("isAvailable").asBoolean();
                }
            }
            return false;
        } catch (Exception e) {
            LOG.severe("Failed to call Catalog Service checkAvailability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deactivate offer after booking is CONFIRMED.
     * PATCH /internal/offers/{offerId}/deactivate
     * No JWT needed — internal endpoint.
     */
    public void deactivateOffer(Long offerId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CATALOG_SERVICE_BASE + "/internal/offers/" + offerId + "/deactivate"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                LOG.info("Offer " + offerId + " deactivated after booking confirmed.");
            } else {
                LOG.warning("Failed to deactivate offer " + offerId + ". Status=" + response.statusCode());
            }
        } catch (Exception e) {
            // Don't fail the booking if this call fails — log and continue
            LOG.warning("Could not deactivate offer " + offerId + ": " + e.getMessage());
        }
    }

    /**
     * Helper: extract price from offer JsonNode returned by getServiceOffer()
     */
    public BigDecimal extractPrice(JsonNode offerData) {
        if (offerData != null && offerData.has("price")) {
            return new BigDecimal(offerData.get("price").asText());
        }
        throw new IllegalStateException("Offer data missing price field");
    }

    /**
     * Helper: extract providerId from offer JsonNode returned by getServiceOffer()
     */
    public Long extractProviderId(JsonNode offerData) {
        if (offerData != null && offerData.has("providerId")) {
            return offerData.get("providerId").asLong();
        }
        throw new IllegalStateException("Offer data missing providerId field");
    }
}
