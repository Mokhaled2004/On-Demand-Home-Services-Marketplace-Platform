package com.marketplace.bookingservice.repository;

import com.marketplace.bookingservice.entity.Booking;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Data access layer for Booking entity.
 *
 * Uses RESOURCE_LOCAL transactions (direct JDBC to Neon PostgreSQL).
 * Annotated @Singleton so one EntityManagerFactory is shared across the app.
 * Each operation opens its own EntityManager and manages its own transaction.
 */
@Singleton
@Startup
public class BookingRepository {

    private static final Logger LOG = Logger.getLogger(BookingRepository.class.getName());

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        emf = Persistence.createEntityManagerFactory("bookingPU");
        LOG.info("BookingRepository: EntityManagerFactory initialized.");
    }

    @PreDestroy
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    // ===== Write Operations =====

    public Booking save(Booking booking) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (booking.getId() == null) {
                em.persist(booking);
                em.flush(); // force INSERT so we get the generated ID back
            } else {
                booking = em.merge(booking);
                em.flush();
            }
            tx.commit();
            return booking;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save booking: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void updateStatus(Long bookingId, Booking.BookingStatus newStatus) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                "UPDATE Booking b SET b.status = :status, b.updatedAt = CURRENT_TIMESTAMP WHERE b.id = :id"
            )
            .setParameter("status", newStatus)
            .setParameter("id", bookingId)
            .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update booking status: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void markEventPublished(Long bookingId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                "UPDATE Booking b SET b.eventPublished = true WHERE b.id = :id"
            )
            .setParameter("id", bookingId)
            .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to mark event published: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ===== Read Operations =====

    public Optional<Booking> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Booking booking = em.find(Booking.class, id);
            return Optional.ofNullable(booking);
        } finally {
            em.close();
        }
    }

    public List<Booking> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Booking b ORDER BY b.createdAt DESC", Booking.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Booking> findByCustomerId(Long customerId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Booking b WHERE b.customerId = :customerId ORDER BY b.createdAt DESC",
                Booking.class
            ).setParameter("customerId", customerId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Booking> findByProviderId(Long providerId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Booking b WHERE b.providerId = :providerId ORDER BY b.createdAt DESC",
                Booking.class
            ).setParameter("providerId", providerId).getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Booking> findByIdempotencyKey(String key) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Booking> results = em.createQuery(
                "SELECT b FROM Booking b WHERE b.idempotencyKey = :key",
                Booking.class
            ).setParameter("key", key).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
}
