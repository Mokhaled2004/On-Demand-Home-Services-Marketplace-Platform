package com.marketplace.user.service.category;

import com.marketplace.user.entity.ServiceCategory;
import com.marketplace.user.exception.CategoryNotFoundException;
import com.marketplace.user.exception.DuplicateCategoryException;
import com.marketplace.user.repository.ServiceCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private static final Logger log = LoggerFactory.getLogger(ServiceCategoryServiceImpl.class);

    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryServiceImpl(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    @Override
    public ServiceCategory createCategory(String name, String description) {
        log.info("Creating category: {}", name);
        if (serviceCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryException(name);
        }
        ServiceCategory category = new ServiceCategory();
        category.setName(name);
        category.setDescription(description);
        return serviceCategoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceCategory getCategoryById(Long categoryId) {
        log.info("Getting category by id={}", categoryId);
        return serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategory> getAllCategories() {
        log.info("Getting all categories");
        return serviceCategoryRepository.findAll();
    }

    @Override
    public ServiceCategory updateCategory(Long categoryId, String name, String description) {
        log.info("Updating category id={}", categoryId);
        ServiceCategory category = getCategoryById(categoryId);
        if (!category.getName().equalsIgnoreCase(name) && serviceCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryException(name);
        }
        category.setName(name);
        category.setDescription(description);
        return serviceCategoryRepository.save(category);
    }

    @Override
    public void softDeleteCategory(Long categoryId) {
        log.info("Deleting category id={}", categoryId);
        // throws CategoryNotFoundException (404) if not found
        getCategoryById(categoryId);
        serviceCategoryRepository.deleteById(categoryId);
    }

    @Override
    public void hardDeleteCategory(Long categoryId) {
        log.info("Hard deleting category id={}", categoryId);
        // throws CategoryNotFoundException (404) if not found
        getCategoryById(categoryId);
        serviceCategoryRepository.deleteById(categoryId);
    }
}
