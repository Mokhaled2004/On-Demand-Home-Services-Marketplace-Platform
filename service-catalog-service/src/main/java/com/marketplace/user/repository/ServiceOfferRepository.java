package com.marketplace.user.repository;

import com.marketplace.user.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    List<ServiceOffer> findByProviderId(Long providerId);
    List<ServiceOffer> findByCategoryId(Long categoryId);
    List<ServiceOffer> findByStatus(ServiceOffer.Status status);
    List<ServiceOffer> findByCategoryIdAndStatus(Long categoryId, ServiceOffer.Status status);
    
    // Custom queries for filtering active offers
    List<ServiceOffer> findByCategoryIdAndStatusAndAvailableFromGreaterThanEqual(
            Long categoryId, 
            ServiceOffer.Status status, 
            LocalDateTime availableFrom);
    
    List<ServiceOffer> findByStatusAndAvailableFromGreaterThanEqual(
            ServiceOffer.Status status, 
            LocalDateTime availableFrom);
    
    @Query("SELECT o FROM ServiceOffer o WHERE " +
           "o.status = :status AND " +
           "o.availableFrom >= :now AND " +
           "(:keyword IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR o.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR o.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR o.price <= :maxPrice)")
    List<ServiceOffer> searchActiveOffers(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") ServiceOffer.Status status,
            @Param("now") LocalDateTime now);
}
