package com.marketplace.notification.dto;

import com.marketplace.notification.entity.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private Long bookingId;
    private String type;
    private String title;
    private String message;
    private Boolean readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponse from(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.id         = n.getId();
        r.userId     = n.getUserId();
        r.bookingId  = n.getBookingId();
        r.type       = n.getType().name();
        r.title      = n.getTitle();
        r.message    = n.getMessage();
        r.readStatus = n.getReadStatus();
        r.createdAt  = n.getCreatedAt();
        r.readAt     = n.getReadAt();
        return r;
    }
}
