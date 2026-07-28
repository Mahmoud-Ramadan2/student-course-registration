package com.mahmoudramadan.studentregistration.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse <T> {
    private boolean success;
    private T data;
    private String message;
    private Instant timestamp;
    private String requestId;


    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .requestId(MDC.get("requestId"))
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .requestId(MDC.get("requestId"))
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {

        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .requestId(MDC.get("requestId"))
                .build();
    }

    public static ApiResponse<Void> error(
            String message) {
        return ApiResponse.<Void>builder()
                .success(false).message(message)
                .timestamp(Instant.now())
                .requestId(MDC.get("requestId"))
                .build();
    }

}
