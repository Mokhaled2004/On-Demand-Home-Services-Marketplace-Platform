package com.marketplace.user.util;

import java.time.LocalDateTime;

import com.marketplace.user.dto.response.ApiResponse;

public final class ApiResponseBuilder {
    private ApiResponseBuilder() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(Constants.STATUS_SUCCESS)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .status(Constants.STATUS_SUCCESS)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .status(Constants.STATUS_ERROR)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .status(Constants.STATUS_ERROR)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
