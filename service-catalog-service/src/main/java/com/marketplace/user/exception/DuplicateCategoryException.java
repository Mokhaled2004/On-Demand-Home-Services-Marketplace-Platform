package com.marketplace.user.exception;

/**
 * Thrown when trying to create a category with a name that already exists
 * HTTP Status: 409 Conflict
 */
public class DuplicateCategoryException extends RuntimeException {

    public DuplicateCategoryException(String categoryName) {
        super("Category with name '" + categoryName + "' already exists");
    }
}
