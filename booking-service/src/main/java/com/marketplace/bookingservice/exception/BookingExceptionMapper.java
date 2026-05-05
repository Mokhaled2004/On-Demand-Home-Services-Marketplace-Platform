package com.marketplace.bookingservice.exception;

public class BookingExceptionMapper extends RuntimeException {
    public BookingExceptionMapper(String message) {
        super(message);
    }
}
