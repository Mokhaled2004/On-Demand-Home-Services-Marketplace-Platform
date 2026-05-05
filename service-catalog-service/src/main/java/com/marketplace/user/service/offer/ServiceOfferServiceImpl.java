package com.marketplace.user.service.offer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marketplace.user.dto.response.AvailabilityResponse;
import com.marketplace.user.entity.ServiceCategory;
import com.marketplace.user.entity.ServiceOffer;
import com.marketplace.user.exception.CategoryNotFoundException;
import com.marketplace.user.exception.InvalidOfferDataException;
import com.marketplace.user.exception.OfferNotFoundException;
import com.marketplace.user.repository.ServiceCategoryRepository;
import com.marketplace.user.repository.ServiceOfferRepository;

@Service
@Transactional
public class ServiceOfferServiceImpl implements ServiceOfferService {
    private static final Logger log = LoggerFactory.getLogger(ServiceOfferServiceImpl.class);

    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceOfferServiceImpl(
            ServiceOfferRepository serviceOfferRepository,
            ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceOfferRepository = serviceOfferRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    @Override
    public ServiceOffer createOffer(Long providerId, Long categoryId, String title, String description,
                                    BigDecimal price, LocalDateTime availableFrom, LocalDateTime availableTo) {
        log.info("Creating offer for providerId={}, categoryId={}", providerId, categoryId);

        validateOfferData(price, availableFrom, availableTo);

        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        ServiceOffer offer = new ServiceOffer();
        offer.setProviderId(providerId);
        offer.setCategory(category);
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setPrice(price);
        offer.setAvailableFrom(availableFrom);
        offer.setAvailableTo(availableTo);
        offer.setStatus(ServiceOffer.Status.ACTIVE);

        return serviceOfferRepository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOffer getOfferById(Long offerId) {
        log.info("Getting offer by id={}", offerId);

        return serviceOfferRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id: " + offerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOffer> getOffersByProviderId(Long providerId) {
        log.info("Getting offers for providerId={}", providerId);

        return serviceOfferRepository.findByProviderId(providerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOffer> getOffersByCategory(Long categoryId) {
        log.info("Getting active offers for categoryId={}", categoryId);

        // Validate category exists first - throws 404 if not found
        if (!serviceCategoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException(categoryId);
        }

        return serviceOfferRepository
                .findByCategoryIdAndStatusAndAvailableFromGreaterThanEqual(
                        categoryId,
                        ServiceOffer.Status.ACTIVE,
                        LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOffer> getAllActiveOffers() {
        log.info("Getting all active offers");

        return serviceOfferRepository
                .findByStatusAndAvailableFromGreaterThanEqual(
                    ServiceOffer.Status.ACTIVE, 
                    LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOffer> searchOffers(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching offers keyword={}, categoryId={}, minPrice={}, maxPrice={}",
                keyword, categoryId, minPrice, maxPrice);

        return serviceOfferRepository.searchActiveOffers(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                ServiceOffer.Status.ACTIVE,
                LocalDateTime.now());
    }

    @Override
    public ServiceOffer updateOffer(Long offerId, String title, String description, BigDecimal price,
                                    LocalDateTime availableFrom, LocalDateTime availableTo, String status) {
        log.info("Updating offer id={}", offerId);

        validateOfferData(price, availableFrom, availableTo);

        // Validate status value before applying
        ServiceOffer.Status offerStatus;
        try {
            offerStatus = ServiceOffer.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidOfferDataException("Invalid status value. Accepted values: ACTIVE, INACTIVE");
        }

        ServiceOffer offer = getOfferById(offerId);
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setPrice(price);
        offer.setAvailableFrom(availableFrom);
        offer.setAvailableTo(availableTo);
        offer.setStatus(offerStatus);

        return serviceOfferRepository.save(offer);
    }

    @Override
    public void softDeleteOffer(Long offerId) {
        log.info("Soft deleting offer id={}", offerId);

        ServiceOffer offer = getOfferById(offerId);
        offer.setStatus(ServiceOffer.Status.INACTIVE);
        serviceOfferRepository.save(offer);
    }

    @Override
    public void hardDeleteOffer(Long offerId) {
        log.info("Hard deleting offer id={}", offerId);

        ServiceOffer offer = getOfferById(offerId);
        serviceOfferRepository.delete(offer);
    }

    @Override
    public ServiceOffer toggleOfferStatus(Long offerId) {
        log.info("Toggling status for offer id={}", offerId);

        ServiceOffer offer = getOfferById(offerId);
        if (offer.getStatus() == ServiceOffer.Status.ACTIVE) {
            offer.setStatus(ServiceOffer.Status.INACTIVE);
        } else {
            offer.setStatus(ServiceOffer.Status.ACTIVE);
        }
        return serviceOfferRepository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long offerId) {
        log.info("Getting availability for offer id={}", offerId);

        ServiceOffer offer = getOfferById(offerId);
        LocalDateTime now = LocalDateTime.now();

        // isAvailable = offer is ACTIVE AND current time is within the availability window
        boolean isAvailable = offer.getStatus() == ServiceOffer.Status.ACTIVE
                && !now.isBefore(offer.getAvailableFrom())   // now >= availableFrom
                && !now.isAfter(offer.getAvailableTo());     // now <= availableTo

        AvailabilityResponse response = new AvailabilityResponse();
        response.setAvailableFrom(offer.getAvailableFrom());
        response.setAvailableTo(offer.getAvailableTo());
        response.setStatus(offer.getStatus().toString());
        response.setIsAvailable(isAvailable);

        return response;
    }

    private void validateOfferData(BigDecimal price, LocalDateTime availableFrom, LocalDateTime availableTo) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOfferDataException("Price must be greater than zero");
        }

        if (availableFrom == null || availableTo == null || !availableTo.isAfter(availableFrom)) {
            throw new InvalidOfferDataException("availableTo must be after availableFrom");
        }
    }
}
