package com.marketplace.user.controller;

import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.ServiceOfferResponse;
import com.marketplace.user.entity.ServiceOffer;
import com.marketplace.user.mapper.ServiceOfferMapper;
import com.marketplace.user.service.offer.ServiceOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal Controller — service-to-service calls only.
 * No JWT required. Permitted via SecurityConfig: /internal/**
 *
 * Used by Booking Service after a booking is CONFIRMED to deactivate the offer
 * so no other customer can book the same slot.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalOfferController {

    private final ServiceOfferService serviceOfferService;
    private final ServiceOfferMapper serviceOfferMapper;

    /**
     * PATCH /internal/offers/{offerId}/deactivate
     * Called by Booking Service after booking is CONFIRMED.
     * Sets offer status to INACTIVE so it no longer appears in customer browsing.
     *
     * @param offerId the offer ID to deactivate
     * @return 200 OK with updated offer
     */
    @PatchMapping("/offers/{offerId}/deactivate")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> deactivateOffer(
            @PathVariable Long offerId) {
        log.info("Internal: deactivating offer id={} after confirmed booking", offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);
        if (offer.getStatus() == ServiceOffer.Status.INACTIVE) {
            log.info("Offer {} is already INACTIVE, skipping", offerId);
            return ResponseEntity.ok(
                    ApiResponse.success("Offer already inactive", serviceOfferMapper.toDTO(offer))
            );
        }

        serviceOfferService.softDeleteOffer(offerId);
        ServiceOffer updated = serviceOfferService.getOfferById(offerId);

        log.info("Internal: offer {} deactivated successfully", offerId);
        return ResponseEntity.ok(
                ApiResponse.success("Offer deactivated successfully", serviceOfferMapper.toDTO(updated))
        );
    }

    /**
     * PATCH /internal/offers/{offerId}/reactivate
     * Called by Booking Service after booking is CANCELLED.
     * Sets offer status back to ACTIVE so it can be booked again.
     *
     * @param offerId the offer ID to reactivate
     * @return 200 OK with updated offer
     */
    @PatchMapping("/offers/{offerId}/reactivate")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> reactivateOffer(
            @PathVariable Long offerId) {
        log.info("Internal: reactivating offer id={} after booking cancelled", offerId);

        ServiceOffer offer = serviceOfferService.getOfferById(offerId);
        if (offer.getStatus() == ServiceOffer.Status.ACTIVE) {
            log.info("Offer {} is already ACTIVE, skipping", offerId);
            return ResponseEntity.ok(
                    ApiResponse.success("Offer already active", serviceOfferMapper.toDTO(offer))
            );
        }

        // Toggle status from INACTIVE to ACTIVE
        ServiceOffer updated = serviceOfferService.toggleOfferStatus(offerId);

        log.info("Internal: offer {} reactivated successfully", offerId);
        return ResponseEntity.ok(
                ApiResponse.success("Offer reactivated successfully", serviceOfferMapper.toDTO(updated))
        );
    }
}
