package com.marketplace.user.exception;

/**
 * Thrown when offer data is invalid
 * Examples: negative price, availableTo before availableFrom
 * HTTP Status: 400 Bad Request
 */
public class InvalidOfferDataException extends RuntimeException {

    public InvalidOfferDataException(String message) {
        super(message);
    }
}
