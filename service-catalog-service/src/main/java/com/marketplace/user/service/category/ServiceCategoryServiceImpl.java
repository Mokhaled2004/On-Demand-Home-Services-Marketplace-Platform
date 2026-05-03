package com.marketplace.user.service.category;

// - @Service, @Transactional
//    - Inject: ServiceCategoryRepository
//    - Implement all methods from ServiceCategoryService interface
//    - Validation: Check for duplicate names (case-insensitive)
//    - Logging: Log all operations
@Service
@Transactional
public class ServiceCategoryServiceImpl implements ServiceCategoryService {
    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryServiceImpl(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    @Override
    public ServiceCategory createCategory(String name, String description) {
        if (serviceCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists.");
        }
        ServiceCategory category = new ServiceCategory();
        category.setName(name);
        category.setDescription(description);
        return serviceCategoryRepository.save(category);
    }

    @Override
    public ServiceCategory getCategoryById(Long categoryId) {
        return serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));
    }

    @Override
    public List<ServiceCategory> getAllCategories() {
        return serviceCategoryRepository.findAll();
    }

    @Override
    public ServiceCategory updateCategory(Long categoryId, String name, String description) {
        ServiceCategory category = getCategoryById(categoryId);
        if (!category.getName().equalsIgnoreCase(name) && serviceCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists.");
        }
        category.setName(name);
        category.setDescription(description);
        return serviceCategoryRepository.save(category);
    }

    @Override
    public void softDeleteCategory(Long categoryId) {
        ServiceCategory category = getCategoryById(categoryId);
        category.setDeleted(true);
        serviceCategoryRepository.save(category);
    }

    @Override
    public void hardDeleteCategory(Long categoryId) {
        serviceCategoryRepository.deleteById(categoryId);
    }
}
