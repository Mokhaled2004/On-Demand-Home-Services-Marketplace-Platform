package com.marketplace.user.controller;

import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.AvailabilityResponse;
import com.marketplace.user.dto.response.ServiceOfferResponse;
import com.marketplace.user.entity.ServiceOffer;
import com.marketplace.user.mapper.ServiceOfferMapper;
import com.marketplace.user.service.offer.ServiceOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer Service Browsing Controller
 * All endpoints require authentication (any authenticated user can browse)
 * 
 * Base URL: /services
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CustomerServiceController {

    private final ServiceOfferService serviceOfferService;
    private final ServiceOfferMapper serviceOfferMapper;

    /**
     * GET /services
     * Browse all active services
     * 
     * @return 200 OK with List<ServiceOfferResponse>
     * Filters: status = ACTIVE, availableFrom >= NOW()
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceOfferResponse>>> getAllServices() {
        log.info("Customer browsing all active services");

        List<ServiceOffer> offers = serviceOfferService.getAllActiveOffers();
        List<ServiceOfferResponse> responses = offers.stream()
                .map(serviceOfferMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Services retrieved successfully", responses)
        );
    }

    /**
     * GET /services/category/{categoryId}
     * Browse services by category
     * 
     * @param categoryId the category ID to filter by
     * @return 200 OK with List<ServiceOfferResponse>
     * Filters: category_id = categoryId, status = ACTIVE, availableFrom >= NOW()
     * @throws CategoryNotFoundException if category doesn't exist
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceOfferResponse>>> getServicesByCategory(
            @PathVariable Long categoryId) {
        log.info("Customer browsing services in category: {}", categoryId);

        List<ServiceOffer> offers = serviceOfferService.getOffersByCategory(categoryId);
        List<ServiceOfferResponse> responses = offers.stream()
                .map(serviceOfferMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Services retrieved successfully", responses)
        );
    }

    /**
     * GET /services/search
     * Search services by keyword and/or price range
     * 
     * @param keyword optional search keyword (searches title and description)
     * @param categoryId optional category filter
     * @param minPrice optional minimum price filter
     * @param maxPrice optional maximum price filter
     * @return 200 OK with List<ServiceOfferResponse>
     * Filters: status = ACTIVE, availableFrom >= NOW()
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ServiceOfferResponse>>> searchServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        log.info("Customer searching services: keyword={}, categoryId={}, minPrice={}, maxPrice={}",
                keyword, categoryId, minPrice, maxPrice);

        List<ServiceOffer> offers = serviceOfferService.searchOffers(keyword, categoryId, minPrice, maxPrice);
        List<ServiceOfferResponse> responses = offers.stream()
                .map(serviceOfferMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Services retrieved successfully", responses)
        );
    }

    /**
     * GET /services/{offerId}
     * Get service details
     * 
     * @param offerId the offer ID
     * @return 200 OK with ServiceOfferResponse
     * Validation: offer must be ACTIVE
     * @throws OfferNotFoundException if offer not found
     */
    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> getServiceDetails(
            @PathVariable Long offerId) {
        log.info("Customer viewing service details: {}", offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        // Verify offer is active
        if (!"ACTIVE".equals(offer.getStatus())) {
            throw new com.marketplace.user.exception.OfferNotFoundException(offerId);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Service retrieved successfully", serviceOfferMapper.toDTO(offer))
        );
    }

    /**
     * GET /services/{offerId}/availability
     * Check service availability
     * 
     * @param offerId the offer ID
     * @return 200 OK with AvailabilityResponse
     * @throws OfferNotFoundException if offer not found
     */
    @GetMapping("/{offerId}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @PathVariable Long offerId) {
        log.info("Customer checking availability for service: {}", offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        AvailabilityResponse availability = new AvailabilityResponse();
        availability.setAvailableFrom(offer.getAvailableFrom());
        availability.setAvailableTo(offer.getAvailableTo());
        availability.setStatus(offer.getStatus());

        // Check if currently available
        LocalDateTime now = LocalDateTime.now();
        boolean isAvailable = "ACTIVE".equals(offer.getStatus())
                && !now.isBefore(offer.getAvailableFrom())
                && !now.isAfter(offer.getAvailableTo());
        availability.setIsAvailable(isAvailable);

        return ResponseEntity.ok(
                ApiResponse.success("Availability retrieved successfully", availability)
        );
    }
}
