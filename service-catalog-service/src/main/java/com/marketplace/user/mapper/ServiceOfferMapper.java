package com.marketplace.user.mapper;

import com.marketplace.user.dto.request.CreateOfferRequest;
import com.marketplace.user.dto.response.ServiceOfferResponse;
import com.marketplace.user.entity.ServiceOffer;
import org.springframework.stereotype.Component;

/**
 * Service Offer Mapper
 * Maps ServiceOffer entity to/from DTOs
 */
@Component
public class ServiceOfferMapper {

    /**
     * Convert ServiceOffer entity to ServiceOfferResponse DTO
     * @param offer the ServiceOffer entity
     * @return ServiceOfferResponse DTO
     */
    public ServiceOfferResponse toDTO(ServiceOffer offer) {
        if (offer == null) {
            return null;
        }

        ServiceOfferResponse response = new ServiceOfferResponse();
        response.setId(offer.getId());
        response.setProviderId(offer.getProviderId());
        response.setCategoryId(offer.getCategory() != null ? offer.getCategory().getId() : null);
        response.setTitle(offer.getTitle());
        response.setDescription(offer.getDescription());
        response.setPrice(offer.getPrice());
        response.setAvailableFrom(offer.getAvailableFrom());
        response.setAvailableTo(offer.getAvailableTo());
        response.setStatus(offer.getStatus().toString());
        response.setCreatedAt(offer.getCreatedAt());
        response.setUpdatedAt(offer.getUpdatedAt());

        return response;
    }

    /**
     * Convert CreateOfferRequest to ServiceOffer entity
     * @param request the CreateOfferRequest
     * @return ServiceOffer entity
     */
    public ServiceOffer toEntity(CreateOfferRequest request) {
        if (request == null) {
            return null;
        }

        ServiceOffer offer = new ServiceOffer();
        // Note: category will be set by the service layer
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        offer.setAvailableFrom(request.getAvailableFrom());
        offer.setAvailableTo(request.getAvailableTo());

        return offer;
    }
}
