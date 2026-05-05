package com.marketplace.notification.repository;

import com.marketplace.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByNotificationId(Long notificationId);

    List<NotificationLog> findByStatus(NotificationLog.DeliveryStatus status);
}
