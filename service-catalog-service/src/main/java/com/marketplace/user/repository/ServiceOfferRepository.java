package com.marketplace.user.repository;

import com.marketplace.user.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    List<ServiceOffer> findByProviderId(Long providerId);
    List<ServiceOffer> findByCategoryId(Long categoryId);
    List<ServiceOffer> findByStatus(ServiceOffer.Status status);
    List<ServiceOffer> findByCategoryIdAndStatus(Long categoryId, ServiceOffer.Status status);
}
