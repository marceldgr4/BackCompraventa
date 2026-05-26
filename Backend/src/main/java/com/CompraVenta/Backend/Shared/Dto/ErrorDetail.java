package com.CompraVenta.Backend.Shared.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ErrorDetail {
    private final String message;
    private final String code;

    public static ErrorDetail of(String message) {
        return ErrorDetail.builder().message(message).build();
    }
    public static ErrorDetail of(String message, String code) {
        return ErrorDetail.builder().message(message).code(code).build();
    }
}
