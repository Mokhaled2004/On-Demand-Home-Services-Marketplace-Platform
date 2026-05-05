package com.marketplace.user.controller;

import com.marketplace.user.dto.request.CreateOfferRequest;
import com.marketplace.user.dto.request.UpdateOfferRequest;
import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.ServiceOfferResponse;
import com.marketplace.user.entity.ServiceOffer;
import com.marketplace.user.exception.UnauthorizedOfferAccessException;
import com.marketplace.user.mapper.ServiceOfferMapper;
import com.marketplace.user.service.offer.ServiceOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provider Offer Management Controller
 * All endpoints require ROLE_SERVICE_PROVIDER
 * 
 * Base URL: /provider/offers
 */
@RestController
@RequestMapping("/provider/offers")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SERVICE_PROVIDER')")
public class ProviderOfferController {

    private final ServiceOfferService serviceOfferService;
    private final ServiceOfferMapper serviceOfferMapper;

    /**
     * Extract provider ID from JWT token
     * The Authentication object contains the userId from JWT claims
     */
    private Long getAuthenticatedProviderId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    /**
     * POST /provider/offers
     * Create a new service offer
     * Provider ID is extracted from JWT token
     * 
     * @param request CreateOfferRequest with offer details
     * @param authentication Spring Security authentication (contains JWT)
     * @return 201 Created with ServiceOfferResponse
     * @throws InvalidOfferDataException if price <= 0 or availableTo <= availableFrom
     * @throws CategoryNotFoundException if category doesn't exist
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> createOffer(
            @Valid @RequestBody CreateOfferRequest request,
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} creating new offer: {}", providerId, request.getTitle());

        ServiceOffer offer = serviceOfferService.createOffer(
                providerId,
                request.getCategoryId(),
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getAvailableFrom(),
                request.getAvailableTo()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Offer created successfully",
                        serviceOfferMapper.toDTO(offer)
                ));
    }

    /**
     * GET /provider/offers
     * List all offers created by the authenticated provider
     * 
     * @param authentication Spring Security authentication (contains JWT)
     * @return 200 OK with List<ServiceOfferResponse>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceOfferResponse>>> getMyOffers(
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} fetching their offers", providerId);

        List<ServiceOffer> offers = serviceOfferService.getOffersByProviderId(providerId);
        List<ServiceOfferResponse> responses = offers.stream()
                .map(serviceOfferMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Offers retrieved successfully", responses)
        );
    }

    /**
     * GET /provider/offers/{offerId}
     * Get a single offer by ID
     * Provider can only view their own offers
     * 
     * @param offerId the offer ID
     * @param authentication Spring Security authentication (contains JWT)
     * @return 200 OK with ServiceOfferResponse
     * @throws OfferNotFoundException if offer not found
     * @throws UnauthorizedOfferAccessException if offer doesn't belong to provider
     */
    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> getOffer(
            @PathVariable Long offerId,
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} fetching offer: {}", providerId, offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        // Verify ownership
        if (!offer.getProviderId().equals(providerId)) {
            throw new UnauthorizedOfferAccessException(providerId, offerId);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Offer retrieved successfully", serviceOfferMapper.toDTO(offer))
        );
    }

    /**
     * PUT /provider/offers/{offerId}
     * Update an existing offer
     * Provider can only update their own offers
     * 
     * @param offerId the offer ID to update
     * @param request UpdateOfferRequest with new details
     * @param authentication Spring Security authentication (contains JWT)
     * @return 200 OK with updated ServiceOfferResponse
     * @throws OfferNotFoundException if offer not found
     * @throws UnauthorizedOfferAccessException if offer doesn't belong to provider
     * @throws InvalidOfferDataException if data is invalid
     */
    @PutMapping("/{offerId}")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> updateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody UpdateOfferRequest request,
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} updating offer: {}", providerId, offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        // Verify ownership
        if (!offer.getProviderId().equals(providerId)) {
            throw new UnauthorizedOfferAccessException(providerId, offerId);
        }

        ServiceOffer updatedOffer = serviceOfferService.updateOffer(
                offerId,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getAvailableFrom(),
                request.getAvailableTo(),
                request.getStatus()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Offer updated successfully", serviceOfferMapper.toDTO(updatedOffer))
        );
    }

    /**
     * PATCH /provider/offers/{offerId}/toggle-status
     * Toggle offer status between ACTIVE and INACTIVE
     * Provider can only toggle their own offers
     *
     * @param offerId the offer ID
     * @param authentication Spring Security authentication (contains JWT)
     * @return 200 OK with updated ServiceOfferResponse
     */
    @PatchMapping("/{offerId}/toggle-status")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> toggleOfferStatus(
            @PathVariable Long offerId,
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} toggling status for offer: {}", providerId, offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        // Verify ownership
        if (!offer.getProviderId().equals(providerId)) {
            throw new UnauthorizedOfferAccessException(providerId, offerId);
        }

        ServiceOffer updated = serviceOfferService.toggleOfferStatus(offerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Offer status toggled to " + updated.getStatus(),
                        serviceOfferMapper.toDTO(updated)
                )
        );
    }

    /**
     * DELETE /provider/offers/{offerId}
     * Delete (soft delete) an offer
     * Provider can only delete their own offers
     * 
     * @param offerId the offer ID to delete
     * @param hard optional query param: if "true", performs hard delete
     * @param authentication Spring Security authentication (contains JWT)
     * @return 204 No Content
     * @throws OfferNotFoundException if offer not found
     * @throws UnauthorizedOfferAccessException if offer doesn't belong to provider
     */
    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long offerId,
            @RequestParam(required = false, defaultValue = "false") boolean hard,
            Authentication authentication) {
        Long providerId = getAuthenticatedProviderId(authentication);
        log.info("Provider {} deleting offer: {} (hard={})", providerId, offerId, hard);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);

        // Verify ownership
        if (!offer.getProviderId().equals(providerId)) {
            throw new UnauthorizedOfferAccessException(providerId, offerId);
        }

        if (hard) {
            serviceOfferService.hardDeleteOffer(offerId);
        } else {
            serviceOfferService.softDeleteOffer(offerId);
        }

        return ResponseEntity.noContent().build();
    }
}
