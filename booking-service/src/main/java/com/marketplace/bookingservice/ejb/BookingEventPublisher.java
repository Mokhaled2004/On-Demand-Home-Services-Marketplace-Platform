package com.marketplace.bookingservice.ejb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * EJB TYPE 2: @Singleton
 *
 * Manages a single shared RabbitMQ connection for the entire application.
 * Opens the connection on startup (@PostConstruct) and closes it on shutdown (@PreDestroy).
 *
 * Why Singleton?
 * - RabbitMQ connections are expensive to create — one per app is the right pattern.
 * - All booking events are published through this single bean.
 * - @Lock(READ) allows multiple threads to publish concurrently without blocking.
 *
 * Exchange: booking.events (topic exchange)
 * Routing keys:
 *   booking.confirmed  → customer confirmed, payment deducted
 *   booking.failed     → payment failed or service unavailable
 *   booking.cancelled  → customer cancelled a confirmed booking
 */
@Singleton
@Startup
public class BookingEventPublisher {

    private static final Logger LOG = Logger.getLogger(BookingEventPublisher.class.getName());

    // RabbitMQ connection settings — update host/port/credentials as needed
    private static final String RABBITMQ_HOST     = "localhost";
    private static final int    RABBITMQ_PORT     = 5672;
    private static final String RABBITMQ_USER     = "guest";
    private static final String RABBITMQ_PASSWORD = "guest";
    private static final String EXCHANGE_NAME     = "booking.events";

    private Connection connection;
    private Channel   channel;
    private final ObjectMapper objectMapper;

    public BookingEventPublisher() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    // ===== Lifecycle =====

    @PostConstruct
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT);
            factory.setUsername(RABBITMQ_USER);
            factory.setPassword(RABBITMQ_PASSWORD);
            factory.setConnectionTimeout(5000);
            factory.setAutomaticRecoveryEnabled(true); // auto-reconnect on network blip

            connection = factory.newConnection("booking-service");
            channel    = connection.createChannel();

            // Declare a durable topic exchange — survives RabbitMQ restarts
            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

            LOG.info("RabbitMQ connection established. Exchange '" + EXCHANGE_NAME + "' ready.");
        } catch (Exception e) {
            // Log but don't crash the app — bookings still work without RabbitMQ
            LOG.severe("Failed to connect to RabbitMQ: " + e.getMessage()
                    + ". Events will not be published.");
            connection = null;
            channel    = null;
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (channel != null && channel.isOpen())   channel.close();
            if (connection != null && connection.isOpen()) connection.close();
            LOG.info("RabbitMQ connection closed.");
        } catch (Exception e) {
            LOG.warning("Error closing RabbitMQ connection: " + e.getMessage());
        }
    }

    // ===== Publish Methods =====

    /**
     * Publish BOOKING_CONFIRMED event.
     * Routing key: booking.confirmed
     * Notification Service listens on this to notify customer + provider.
     */
    @Lock(LockType.READ)
    public void publishBookingConfirmed(Long bookingId, Long customerId, String customerName,
                                        Long providerId, BigDecimal amount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",    "BOOKING_CONFIRMED");
        payload.put("bookingId",    bookingId);
        payload.put("customerId",   customerId);
        payload.put("customerName", customerName != null ? customerName : "Customer");
        payload.put("providerId",   providerId);
        payload.put("amount",       amount);

        publish("booking.confirmed", payload);
    }

    /**
     * Publish BOOKING_FAILED event.
     * Routing key: booking.failed
     * Notification Service listens on this to notify customer of rejection.
     */
    @Lock(LockType.READ)
    public void publishBookingFailed(Long bookingId, Long customerId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",  "BOOKING_FAILED");
        payload.put("bookingId",  bookingId);
        payload.put("customerId", customerId);
        payload.put("reason",     reason);

        publish("booking.failed", payload);
    }

    /**
     * Publish BOOKING_CANCELLED event.
     * Routing key: booking.cancelled
     * Notification Service listens on this to notify customer + provider of cancellation.
     */
    @Lock(LockType.READ)
    public void publishBookingCancelled(Long bookingId, Long customerId, String customerName,
                                        Long providerId, BigDecimal amount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",    "BOOKING_CANCELLED");
        payload.put("bookingId",    bookingId);
        payload.put("customerId",   customerId);
        payload.put("customerName", customerName != null ? customerName : "Customer");
        payload.put("providerId",   providerId);
        payload.put("amount",       amount);

        publish("booking.cancelled", payload);
    }

    // ===== Private Helper =====

    private void publish(String routingKey, Map<String, Object> payload) {
        if (channel == null || !channel.isOpen()) {
            LOG.warning("RabbitMQ channel not available. Skipping event: " + routingKey);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    null,
                    json.getBytes(StandardCharsets.UTF_8)
            );
            LOG.info("Published event [" + routingKey + "]: " + json);
        } catch (Exception e) {
            LOG.severe("Failed to publish event [" + routingKey + "]: " + e.getMessage());
        }
    }
}
