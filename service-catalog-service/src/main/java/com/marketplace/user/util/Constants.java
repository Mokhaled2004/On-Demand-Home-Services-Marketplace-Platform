package com.marketplace.user.util;

/**
 * Application Constants
 * Centralized place for all constant values
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // ─── Error Messages ─────────────────────────────────────────────────────

    public static final String CATEGORY_NOT_FOUND = "Category not found";
    public static final String OFFER_NOT_FOUND = "Offer not found";
    public static final String DUPLICATE_CATEGORY = "Category with this name already exists";
    public static final String UNAUTHORIZED_OFFER_ACCESS = "You are not authorized to access this offer";
    public static final String INVALID_OFFER_DATA = "Invalid offer data";
    public static final String INVALID_PRICE = "Price must be greater than 0";
    public static final String INVALID_DATE_RANGE = "Available to date must be after available from date";

    // ─── Success Messages ───────────────────────────────────────────────────

    public static final String CATEGORY_CREATED = "Category created successfully";
    public static final String CATEGORY_UPDATED = "Category updated successfully";
    public static final String CATEGORY_DELETED = "Category deleted successfully";
    public static final String OFFER_CREATED = "Offer created successfully";
    public static final String OFFER_UPDATED = "Offer updated successfully";
    public static final String OFFER_DELETED = "Offer deleted successfully";

    // ─── Status Values ──────────────────────────────────────────────────────

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // ─── Default Values ─────────────────────────────────────────────────────

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_CATEGORY_NAME_LENGTH = 255;
    public static final int MAX_OFFER_TITLE_LENGTH = 255;
}
