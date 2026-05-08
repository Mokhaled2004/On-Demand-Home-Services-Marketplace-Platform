package com.marketplace.bookingservice.ejb;

import com.fasterxml.jackson.databind.JsonNode;
import com.marketplace.bookingservice.client.CatalogServiceClient;
import com.marketplace.bookingservice.client.UserServiceClient;
import com.marketplace.bookingservice.dto.request.CreateBookingRequest;
import com.marketplace.bookingservice.dto.response.BookingResponse;
import com.marketplace.bookingservice.dto.response.BookingWithCustomerResponse;
import com.marketplace.bookingservice.entity.Booking;
import com.marketplace.bookingservice.exception.BookingNotFoundException;
import com.marketplace.bookingservice.exception.InsufficientBalanceException;
import com.marketplace.bookingservice.exception.ServiceUnavailableException;
import com.marketplace.bookingservice.repository.BookingRepository;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * EJB TYPE 1: @Stateless
 * Core booking orchestration logic.
 * Uses RESOURCE_LOCAL transactions — BookingRepository manages its own commits.
 */
@Stateless
public class BookingOrchestrationBean {

    private static final Logger LOG = Logger.getLogger(BookingOrchestrationBean.class.getName());

    @EJB
    private BookingRepository bookingRepository;

    @Inject
    private UserServiceClient userServiceClient;

    @Inject
    private CatalogServiceClient catalogServiceClient;

    @EJB
    private BookingEventPublisher eventPublisher;

    // ===== CREATE BOOKING =====

    public BookingResponse createBooking(CreateBookingRequest request,
                                         Long customerId,
                                         String jwtToken) {
        // 1. Validate — only serviceOfferId is required
        if (request.getServiceOfferId() == null) {
            throw new IllegalArgumentException("serviceOfferId is required.");
        }

        // 2. Get service offer from Catalog Service
        JsonNode offerData = catalogServiceClient.getServiceOffer(request.getServiceOfferId(), jwtToken);
        if (offerData == null) {
            throw new ServiceUnavailableException(
                    "Service offer not found or not active: id=" + request.getServiceOfferId());
        }

        String offerStatus = offerData.has("status") ? offerData.get("status").asText() : "";
        if (!"ACTIVE".equalsIgnoreCase(offerStatus)) {
            throw new ServiceUnavailableException(
                    "Service offer is not available. Current status: " + offerStatus);
        }

        BigDecimal price      = catalogServiceClient.extractPrice(offerData);
        Long       providerId = catalogServiceClient.extractProviderId(offerData);

        // 3. Verify customer exists
        JsonNode customerData = userServiceClient.getUser(customerId, jwtToken);
        if (customerData == null) {
            throw new IllegalArgumentException("Customer not found: id=" + customerId);
        }

        // Extract customer name for notifications
        String customerName = customerData.has("username")
                ? customerData.get("username").asText()
                : "Customer";

        // 4. Resolve serviceStart / serviceEnd
        // If not provided in request, use the offer's availableFrom / availableTo
        LocalDateTime serviceStart = request.getServiceStart();
        LocalDateTime serviceEnd   = request.getServiceEnd();

        if (serviceStart == null) {
            String fromStr = offerData.has("availableFrom") ? offerData.get("availableFrom").asText() : null;
            if (fromStr == null || fromStr.equals("null")) {
                throw new IllegalArgumentException("Offer has no availableFrom. Please provide serviceStart.");
            }
            // Handle both "2026-05-10T09:00:00" and array formats
            serviceStart = LocalDateTime.parse(fromStr);
        }
        if (serviceEnd == null) {
            String toStr = offerData.has("availableTo") ? offerData.get("availableTo").asText() : null;
            if (toStr == null || toStr.equals("null")) {
                throw new IllegalArgumentException("Offer has no availableTo. Please provide serviceEnd.");
            }
            serviceEnd = LocalDateTime.parse(toStr);
        }

        // 5. Idempotency check
        String idempotencyKey = request.getIdempotencyKey() != null
                ? request.getIdempotencyKey()
                : "BOOKING_CUST" + customerId + "_OFFER" + request.getServiceOfferId()
                  + "_" + System.currentTimeMillis();

        var existing = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            LOG.info("Duplicate booking request. Returning existing id=" + existing.get().getId());
            return BookingResponse.from(existing.get());
        }

        // Check no active booking exists for this offer+slot (PENDING or CONFIRMED only)
        boolean slotTaken = bookingRepository.existsActiveBookingForSlot(
                request.getServiceOfferId(), serviceStart, serviceEnd);
        if (slotTaken) {
            throw new ServiceUnavailableException(
                    "This service slot is already booked. Please choose a different time.");
        }

        // 6. Save as PENDING
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setServiceOfferId(request.getServiceOfferId());
        booking.setProviderId(providerId);
        booking.setServiceStart(serviceStart);
        booking.setServiceEnd(serviceEnd);
        booking.setAmount(price);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setIdempotencyKey(idempotencyKey);
        booking.setEventPublished(false);

        booking = bookingRepository.save(booking);
        final Long bookingId = booking.getId();
        LOG.info("Booking created as PENDING. id=" + bookingId);

        // 7. Deduct wallet
        boolean deducted = userServiceClient.deductWallet(price, bookingId, jwtToken);

        if (deducted) {
            // SUCCESS
            bookingRepository.updateStatus(bookingId, Booking.BookingStatus.CONFIRMED);
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            eventPublisher.publishBookingConfirmed(bookingId, customerId, customerName, providerId, price);
            bookingRepository.markEventPublished(bookingId);
            // Deactivate the offer so no other customer can book the same slot
            catalogServiceClient.deactivateOffer(request.getServiceOfferId());
            LOG.info("Booking CONFIRMED. id=" + bookingId);
        } else {
            // FAILURE
            bookingRepository.updateStatus(bookingId, Booking.BookingStatus.FAILED);
            booking.setStatus(Booking.BookingStatus.FAILED);
            eventPublisher.publishBookingFailed(bookingId, customerId, "Insufficient wallet balance");
            LOG.warning("Booking FAILED (insufficient balance). id=" + bookingId);
            throw new InsufficientBalanceException(
                    "Insufficient wallet balance. Booking id=" + bookingId + " has been rejected.");
        }

        return BookingResponse.from(booking);
    }

    // ===== COMPLETE BOOKING (Provider marks service as done) =====

    public BookingResponse completeBooking(Long bookingId, Long providerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("You can only complete your own bookings.");
        }
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot complete booking with status: " + booking.getStatus()
                    + ". Only CONFIRMED bookings can be marked as completed.");
        }

        bookingRepository.updateStatus(bookingId, Booking.BookingStatus.COMPLETED);
        booking.setStatus(Booking.BookingStatus.COMPLETED);

        LOG.info("Booking COMPLETED by provider. id=" + bookingId);
        return BookingResponse.from(booking);
    }

    // ===== CANCEL BOOKING =====

    public BookingResponse cancelBooking(Long bookingId, Long customerId, String jwtToken) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only cancel your own bookings.");
        }
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot cancel booking with status: " + booking.getStatus()
                    + ". Only CONFIRMED bookings can be cancelled.");
        }

        String refundKey = "REFUND_BOOKING_" + bookingId + "_" + UUID.randomUUID();
        userServiceClient.refundWallet(booking.getAmount(), bookingId, refundKey, jwtToken);

        bookingRepository.updateStatus(bookingId, Booking.BookingStatus.CANCELLED);
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Fetch customer name for notification
        String customerName = "Customer";
        try {
            JsonNode customerData = userServiceClient.getUser(customerId, jwtToken);
            if (customerData != null && customerData.has("username")) {
                customerName = customerData.get("username").asText();
            }
        } catch (Exception e) {
            LOG.warning("Could not fetch customer name for cancellation notification: " + e.getMessage());
        }

        eventPublisher.publishBookingCancelled(
                bookingId, customerId, customerName, booking.getProviderId(), booking.getAmount());

        // Reactivate the offer so it can be booked again
        catalogServiceClient.reactivateOffer(booking.getServiceOfferId());

        LOG.info("Booking CANCELLED. id=" + bookingId);
        return BookingResponse.from(booking);
    }

    // ===== READ OPERATIONS =====

    public BookingResponse getBookingById(Long bookingId) {
        return BookingResponse.from(
                bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new BookingNotFoundException(bookingId)));
    }

    public List<BookingResponse> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId)
                .stream().map(BookingResponse::from).collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByProvider(Long providerId) {
        return bookingRepository.findByProviderId(providerId)
                .stream().map(BookingResponse::from).collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream().map(BookingResponse::from).collect(Collectors.toList());
    }

    /**
     * Requirement 9: Provider views completed (CONFIRMED) services with customer names.
     * Calls User Service for each booking to enrich with customer info.
     */
    public List<BookingWithCustomerResponse> getCompletedBookingsForProvider(
            Long providerId, String jwtToken) {

        return bookingRepository.findByProviderId(providerId)
                .stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED
                          || b.getStatus() == Booking.BookingStatus.COMPLETED
                          || b.getStatus() == Booking.BookingStatus.CANCELLED)
                .map(booking -> {
                    String customerName  = "Unknown";
                    String customerEmail = "";
                    try {
                        JsonNode customer = userServiceClient.getUser(booking.getCustomerId(), jwtToken);
                        if (customer != null) {
                            customerName  = customer.has("username")
                                    ? customer.get("username").asText() : "Unknown";
                            customerEmail = customer.has("email")
                                    ? customer.get("email").asText() : "";
                        }
                    } catch (Exception e) {
                        LOG.warning("Could not fetch customer info for id="
                                + booking.getCustomerId() + ": " + e.getMessage());
                    }
                    return BookingWithCustomerResponse.from(booking, customerName, customerEmail);
                })
                .collect(Collectors.toList());
    }
}
