package com.marketplace.user.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
