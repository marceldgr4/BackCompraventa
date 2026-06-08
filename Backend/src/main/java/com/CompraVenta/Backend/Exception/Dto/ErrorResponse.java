package com.CompraVenta.Backend.Exception.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String code,
        List<String>details
) {
    public static ErrorResponse of(int status, String message, String code) {
        return new ErrorResponse(LocalDateTime.now(),status,message,code,null);
    }
    public static ErrorResponse ofValidation(int status, String message, String code, List<String> details) {
        return new ErrorResponse(LocalDateTime.now(),status,message,"VALIDATION_ERROR",details);
    }
}
