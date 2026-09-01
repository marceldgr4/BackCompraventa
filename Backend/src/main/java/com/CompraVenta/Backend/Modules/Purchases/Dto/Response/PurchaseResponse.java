package com.CompraVenta.Backend.Modules.Purchases.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseResponse(
        Long id,
        UUID globalId,

        UUID employeeGlobalId,
        String employeeFullName,

        UUID clienteGlobalId,
        String clienteFullName,

        UUID articleGlobalId,
        String articleName,

        BigDecimal purchasePrice,
        LocalDateTime purchaseDate,

        String notes,
        LocalDateTime createdAt
) {
}
