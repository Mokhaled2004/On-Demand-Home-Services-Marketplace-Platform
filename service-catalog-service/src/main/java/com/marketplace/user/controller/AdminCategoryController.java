package com.marketplace.user.controller;

import com.marketplace.user.dto.request.CreateCategoryRequest;
import com.marketplace.user.dto.request.UpdateCategoryRequest;
import com.marketplace.user.dto.response.ApiResponse;
import com.marketplace.user.dto.response.ServiceCategoryResponse;
import com.marketplace.user.entity.ServiceCategory;
import com.marketplace.user.mapper.ServiceCategoryMapper;
import com.marketplace.user.service.category.ServiceCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Category Management Controller
 * All endpoints require ROLE_ADMIN
 * 
 * Base URL: /admin/categories
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final ServiceCategoryService serviceCategoryService;
    private final ServiceCategoryMapper serviceCategoryMapper;

    /**
     * POST /admin/categories
     * Create a new service category
     * 
     * @param request CreateCategoryRequest with name and description
     * @return 201 Created with ServiceCategoryResponse
     * @throws DuplicateCategoryException if category name already exists
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("Admin creating new category: {}", request.getName());

        ServiceCategory category = serviceCategoryService.createCategory(
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully",
                        serviceCategoryMapper.toDTO(category)
                ));
    }

    /**
     * GET /admin/categories
     * List all service categories
     * 
     * @return 200 OK with List<ServiceCategoryResponse>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceCategoryResponse>>> getAllCategories() {
        log.info("Admin fetching all categories");

        List<ServiceCategory> categories = serviceCategoryService.getAllCategories();
        List<ServiceCategoryResponse> responses = categories.stream()
                .map(serviceCategoryMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", responses)
        );
    }

    /**
     * GET /admin/categories/{categoryId}
     * Get a single category by ID
     * 
     * @param categoryId the category ID
     * @return 200 OK with ServiceCategoryResponse
     * @throws CategoryNotFoundException if category not found
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> getCategory(
            @PathVariable Long categoryId) {
        log.info("Admin fetching category: {}", categoryId);

        ServiceCategory category = serviceCategoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", serviceCategoryMapper.toDTO(category))
        );
    }

    /**
     * PUT /admin/categories/{categoryId}
     * Update an existing category
     * 
     * @param categoryId the category ID to update
     * @param request UpdateCategoryRequest with new name and description
     * @return 200 OK with updated ServiceCategoryResponse
     * @throws CategoryNotFoundException if category not found
     * @throws DuplicateCategoryException if new name already exists
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("Admin updating category: {}", categoryId);

        ServiceCategory category = serviceCategoryService.updateCategory(
                categoryId,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", serviceCategoryMapper.toDTO(category))
        );
    }

    /**
     * DELETE /admin/categories/{categoryId}
     * Delete (soft delete) a category
     * Sets category as inactive instead of hard delete
     * 
     * @param categoryId the category ID to delete
     * @param hard optional query param: if "true", performs hard delete
     * @return 204 No Content
     * @throws CategoryNotFoundException if category not found
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean hard) {
        log.info("Admin deleting category: {} (hard={})", categoryId, hard);

        if (hard) {
            serviceCategoryService.hardDeleteCategory(categoryId);
        } else {
            serviceCategoryService.softDeleteCategory(categoryId);
        }

        return ResponseEntity.noContent().build();
    }
}
