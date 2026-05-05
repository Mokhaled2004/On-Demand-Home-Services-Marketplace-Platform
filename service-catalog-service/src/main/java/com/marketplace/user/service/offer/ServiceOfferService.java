package com.marketplace.user.service.offer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.marketplace.user.dto.response.AvailabilityResponse;
import com.marketplace.user.entity.ServiceOffer;

public interface ServiceOfferService {
    ServiceOffer createOffer(
            Long providerId,
            Long categoryId,
            String title,
            String description,
            BigDecimal price,
            LocalDateTime availableFrom,
            LocalDateTime availableTo);

    ServiceOffer getOfferById(Long offerId);

    List<ServiceOffer> getOffersByProviderId(Long providerId);

    List<ServiceOffer> getOffersByCategory(Long categoryId);

    List<ServiceOffer> getAllActiveOffers();

    List<ServiceOffer> searchOffers(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    ServiceOffer updateOffer(
            Long offerId,
            String title,
            String description,
            BigDecimal price,
            LocalDateTime availableFrom,
            LocalDateTime availableTo,
            String status);

    void softDeleteOffer(Long offerId);

    void hardDeleteOffer(Long offerId);

    ServiceOffer toggleOfferStatus(Long offerId);

    AvailabilityResponse getAvailability(Long offerId);
}
