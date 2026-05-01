package com.marketplace.user.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationServiceClient {
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationServiceClient(
            RestTemplate restTemplate,
            @Value("${services.notification.url:http://localhost:8084}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public Object sendNotification(Object request) {
        return restTemplate.postForObject(
                notificationServiceUrl + "/notifications/send",
                request,
                Object.class);
    }

    public Object sendNotification(Long userId, String type, String title, String message) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("type", type);
        request.put("title", title);
        request.put("message", message);

        return sendNotification(request);
    }

    public Object getNotification(Long notificationId) {
        return restTemplate.getForObject(
                notificationServiceUrl + "/notifications/{notificationId}",
                Object.class,
                notificationId);
    }

    public Object getUserNotifications(Long userId) {
        return restTemplate.getForObject(
                notificationServiceUrl + "/notifications/user/{userId}",
                Object.class,
                userId);
    }
}
