package com.marketplace.user.repository;
import com.marketplace.user.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// - Extends JpaRepository<ServiceCategory, Long>
//    - Methods: findByNameIgnoreCase(String name), existsByNameIgnoreCase(String name)

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    Optional<ServiceCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
