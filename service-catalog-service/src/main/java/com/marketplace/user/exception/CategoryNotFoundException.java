package com.marketplace.user.exception;

/**
 * Thrown when a requested category is not found
 * HTTP Status: 404 Not Found
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long categoryId) {
        super("Category with ID " + categoryId + " not found");
    }
}
