package com.marketplace.user.mapper;

import com.marketplace.user.dto.request.CreateCategoryRequest;
import com.marketplace.user.dto.response.ServiceCategoryResponse;
import com.marketplace.user.entity.ServiceCategory;
import org.springframework.stereotype.Component;

/**
 * Service Category Mapper
 * Maps ServiceCategory entity to/from DTOs
 */
@Component
public class ServiceCategoryMapper {

    /**
     * Convert ServiceCategory entity to ServiceCategoryResponse DTO
     * @param category the ServiceCategory entity
     * @return ServiceCategoryResponse DTO
     */
    public ServiceCategoryResponse toDTO(ServiceCategory category) {
        if (category == null) {
            return null;
        }

        ServiceCategoryResponse response = new ServiceCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }

    /**
     * Convert CreateCategoryRequest to ServiceCategory entity
     * @param request the CreateCategoryRequest
     * @return ServiceCategory entity
     */
    public ServiceCategory toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }

        ServiceCategory category = new ServiceCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return category;
    }
}
