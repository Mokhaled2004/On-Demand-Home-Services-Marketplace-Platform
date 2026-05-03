package com.marketplace.user.exception;

/**
 * Thrown when a requested offer is not found
 * HTTP Status: 404 Not Found
 */
public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(String message) {
        super(message);
    }

    public OfferNotFoundException(Long offerId) {
        super("Offer with ID " + offerId + " not found");
    }
}
