package com.marketplace.bookingservice.rest;

import com.marketplace.bookingservice.dto.request.CreateBookingRequest;
import com.marketplace.bookingservice.dto.response.ApiResponse;
import com.marketplace.bookingservice.dto.response.BookingResponse;
import com.marketplace.bookingservice.dto.response.BookingWithCustomerResponse;
import com.marketplace.bookingservice.ejb.BookingOrchestrationBean;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * JAX-RS REST controller for booking operations.
 * Base path: /api/bookings
 *
 * All endpoints require a JWT token in the Authorization header.
 * The customerId is extracted from the JWT payload — never trusted from the request body.
 */
@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private static final Logger LOG = Logger.getLogger(BookingResource.class.getName());

    @EJB
    private BookingOrchestrationBean bookingBean;

    // ===== POST /api/bookings =====
    // Create a new booking (CUSTOMER)

    @POST
    public Response createBooking(CreateBookingRequest request,
                                  @Context HttpHeaders headers) {

        String jwt        = extractJwt(headers);
        Long   customerId = extractUserIdFromJwt(jwt);

        BookingResponse booking = bookingBean.createBooking(request, customerId, jwt);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Booking created successfully", booking))
                .build();
    }

    // ===== GET /api/bookings/{id} =====
    // Get a single booking by ID

    @GET
    @Path("/{id}")
    public Response getBooking(@PathParam("id") Long id,
                               @Context HttpHeaders headers) {

        extractJwt(headers); // ensure authenticated
        BookingResponse booking = bookingBean.getBookingById(id);
        return Response.ok(ApiResponse.success(booking)).build();
    }

    // ===== GET /api/bookings/customer/{customerId} =====
    // Get all bookings for a customer

    @GET
    @Path("/customer/{customerId}")
    public Response getBookingsByCustomer(@PathParam("customerId") Long customerId,
                                          @Context HttpHeaders headers) {

        extractJwt(headers); // ensure authenticated
        List<BookingResponse> bookings = bookingBean.getBookingsByCustomer(customerId);
        return Response.ok(ApiResponse.success("Bookings retrieved", bookings)).build();
    }

    // ===== GET /api/bookings/provider/{providerId} =====
    // Get all bookings for a provider

    @GET
    @Path("/provider/{providerId}")
    public Response getBookingsByProvider(@PathParam("providerId") Long providerId,
                                          @Context HttpHeaders headers) {

        extractJwt(headers); // ensure authenticated
        List<BookingResponse> bookings = bookingBean.getBookingsByProvider(providerId);
        return Response.ok(ApiResponse.success("Bookings retrieved", bookings)).build();
    }

    // ===== GET /api/bookings =====
    // Get all bookings (admin use)

    @GET
    public Response getAllBookings(@Context HttpHeaders headers) {
        extractJwt(headers); // ensure authenticated
        List<BookingResponse> bookings = bookingBean.getAllBookings();
        return Response.ok(ApiResponse.success("All bookings retrieved", bookings)).build();
    }

    // ===== POST /api/bookings/{id}/complete =====
    // Provider marks a booking as completed after service is done

    @POST
    @Path("/{id}/complete")
    public Response completeBooking(@PathParam("id") Long bookingId,
                                    @Context HttpHeaders headers) {
        String jwt        = extractJwt(headers);
        Long   providerId = extractUserIdFromJwt(jwt);

        BookingResponse booking = bookingBean.completeBooking(bookingId, providerId);
        return Response.ok(ApiResponse.success("Booking marked as completed", booking)).build();
    }

    // ===== POST /api/bookings/{id}/cancel =====
    // Cancel a confirmed booking (CUSTOMER only)

    @POST
    @Path("/{id}/cancel")
    public Response cancelBooking(@PathParam("id") Long bookingId,
                                  @Context HttpHeaders headers) {

        String jwt        = extractJwt(headers);
        Long   customerId = extractUserIdFromJwt(jwt);

        BookingResponse booking = bookingBean.cancelBooking(bookingId, customerId, jwt);
        return Response.ok(ApiResponse.success("Booking cancelled successfully", booking)).build();
    }

    // ===== GET /api/bookings/provider/{providerId}/completed =====
    // Requirement 9: Provider views completed services with customer names

    @GET
    @Path("/provider/{providerId}/completed")
    public Response getCompletedBookingsForProvider(@PathParam("providerId") Long providerId,
                                                     @Context HttpHeaders headers) {
        String jwt = extractJwt(headers);
        List<BookingWithCustomerResponse> bookings =
                bookingBean.getCompletedBookingsForProvider(providerId, jwt);
        return Response.ok(ApiResponse.success(
                "Completed bookings retrieved", bookings)).build();
    }

    // ===== JWT Helpers =====

    /**
     * Extract the raw JWT token from the Authorization: Bearer <token> header.
     */
    private String extractJwt(HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Missing or invalid Authorization header",
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
        return authHeader.substring(7); // strip "Bearer "
    }

    /**
     * Extract the userId from the JWT payload.
     *
     * User Service generates tokens with:
     *   .subject(username)          → sub = "john_doe"  (string)
     *   .claim("userId", userId)    → userId = 42       (number)
     *
     * So we read the "userId" custom claim, NOT "sub".
     */
    private Long extractUserIdFromJwt(String jwt) {
        try {
            String[] parts   = jwt.split("\\.");
            String   payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // payload looks like: {"sub":"john_doe","userId":42,"role":"CUSTOMER",...}
            // Find "userId": <number>
            int userIdIndex = payload.indexOf("\"userId\"");
            if (userIdIndex == -1) {
                throw new NotAuthorizedException("JWT missing userId claim",
                        Response.status(Response.Status.UNAUTHORIZED).build());
            }

            int colonIndex = payload.indexOf(":", userIdIndex);
            // userId is a number (no quotes), read until next comma or }
            int start = colonIndex + 1;
            // skip any whitespace
            while (start < payload.length() && payload.charAt(start) == ' ') start++;
            int end = start;
            while (end < payload.length()
                    && payload.charAt(end) != ','
                    && payload.charAt(end) != '}') {
                end++;
            }
            String userIdValue = payload.substring(start, end).trim();

            return Long.parseLong(userIdValue);

        } catch (NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            LOG.warning("Failed to parse JWT: " + e.getMessage());
            throw new NotAuthorizedException("Invalid JWT token",
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
