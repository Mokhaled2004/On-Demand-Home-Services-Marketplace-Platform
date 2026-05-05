package com.marketplace.bookingservice;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 * Registers all REST resources under /api
 * Full URL: http://localhost:8080/booking-service-1.0-SNAPSHOT/api/bookings
 */
@ApplicationPath("/api")
public class BookingApplication extends Application {
}
