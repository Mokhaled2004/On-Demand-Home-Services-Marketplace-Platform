package com.marketplace.user.exception;

/**
 * Thrown when a provider tries to modify an offer they don't own
 * HTTP Status: 403 Forbidden
 */
public class UnauthorizedOfferAccessException extends RuntimeException {

    public UnauthorizedOfferAccessException(String message) {
        super(message);
    }

    public UnauthorizedOfferAccessException(Long providerId, Long offerId) {
        super("Provider " + providerId + " is not authorized to access offer " + offerId);
    }
}
