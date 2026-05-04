package com.marketplace.user.service.category;

import java.util.List;

import com.marketplace.user.entity.ServiceCategory;

public interface ServiceCategoryService {

    ServiceCategory createCategory(String name, String description);
    ServiceCategory getCategoryById(Long categoryId);
    List<ServiceCategory> getAllCategories();
    ServiceCategory updateCategory(Long categoryId, String name, String description);
    void softDeleteCategory(Long categoryId);
    void hardDeleteCategory(Long categoryId);
}
