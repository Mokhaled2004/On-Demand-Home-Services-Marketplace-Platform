package com.marketplace.notification.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Notification Service (Consumer side).
 *
 * Exchange: booking.events (topic, durable) — declared by Booking Service
 * Queues:   3 durable queues, one per event type
 * Bindings: each queue bound to the exchange with its routing key
 */
@Configuration
public class RabbitMQConfig {

    // Exchange name — must match what Booking Service declares
    public static final String EXCHANGE = "booking.events";

    // Queue names
    public static final String QUEUE_CONFIRMED  = "booking.confirmed.queue";
    public static final String QUEUE_FAILED     = "booking.failed.queue";
    public static final String QUEUE_CANCELLED  = "booking.cancelled.queue";

    // Routing keys — must match what Booking Service publishes
    public static final String KEY_CONFIRMED  = "booking.confirmed";
    public static final String KEY_FAILED     = "booking.failed";
    public static final String KEY_CANCELLED  = "booking.cancelled";

    // ===== Exchange =====

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ===== Queues (durable = survive RabbitMQ restart) =====

    @Bean
    public Queue confirmedQueue() {
        return new Queue(QUEUE_CONFIRMED, true);
    }

    @Bean
    public Queue failedQueue() {
        return new Queue(QUEUE_FAILED, true);
    }

    @Bean
    public Queue cancelledQueue() {
        return new Queue(QUEUE_CANCELLED, true);
    }

    // ===== Bindings =====

    @Bean
    public Binding confirmedBinding(Queue confirmedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(confirmedQueue).to(bookingExchange).with(KEY_CONFIRMED);
    }

    @Bean
    public Binding failedBinding(Queue failedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(failedQueue).to(bookingExchange).with(KEY_FAILED);
    }

    @Bean
    public Binding cancelledBinding(Queue cancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(cancelledQueue).to(bookingExchange).with(KEY_CANCELLED);
    }

    // ===== JSON Message Converter =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
