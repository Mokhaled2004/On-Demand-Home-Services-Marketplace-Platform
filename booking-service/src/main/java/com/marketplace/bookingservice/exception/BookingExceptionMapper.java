package com.marketplace.bookingservice.exception;

import com.marketplace.bookingservice.dto.response.ApiResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global JAX-RS exception mapper.
 * Catches all RuntimeExceptions and maps them to proper HTTP responses
 * with a consistent ApiResponse envelope.
 */
@Provider
public class BookingExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException ex) {

        if (ex instanceof BookingNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ex.getMessage()))
                    .build();
        }

        if (ex instanceof InsufficientBalanceException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ex.getMessage()))
                    .build();
        }

        if (ex instanceof ServiceUnavailableException) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ex.getMessage()))
                    .build();
        }

        if (ex instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ex.getMessage()))
                    .build();
        }

        if (ex instanceof IllegalStateException) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error(ex.getMessage()))
                    .build();
        }

        // Fallback — 500
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error("Internal server error: " + ex.getMessage()))
                .build();
    }
}
