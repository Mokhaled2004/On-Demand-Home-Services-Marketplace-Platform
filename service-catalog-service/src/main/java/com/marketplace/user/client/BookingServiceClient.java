package com.marketplace.user.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BookingServiceClient {
    private final RestTemplate restTemplate;
    private final String bookingServiceUrl;

    public BookingServiceClient(
            RestTemplate restTemplate,
            @Value("${services.booking.url:http://localhost:8082}") String bookingServiceUrl) {
        this.restTemplate = restTemplate;
        this.bookingServiceUrl = bookingServiceUrl;
    }

    public Object getBooking(Long bookingId) {
        return restTemplate.getForObject(
                bookingServiceUrl + "/bookings/{bookingId}",
                Object.class,
                bookingId);
    }

    public String getBookingStatus(Long bookingId) {
        Object response = getBooking(bookingId);

        if (response instanceof Map<?, ?> responseMap) {
            Object data = responseMap.get("data");
            if (data instanceof Map<?, ?> dataMap && dataMap.get("status") != null) {
                return dataMap.get("status").toString();
            }
            if (responseMap.get("status") != null) {
                return responseMap.get("status").toString();
            }
        }

        return null;
    }
}
