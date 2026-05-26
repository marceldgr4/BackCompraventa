package com.CompraVenta.Backend.Shared.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorDetail error;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Recurse Created ok")
                .build();
    }
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.of(message))
                .build();
    }
    public static <T> ApiResponse<T> error(String message, String code){
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.of(message, code))
                .build();
    }
}
