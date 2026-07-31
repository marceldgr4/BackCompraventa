package com.CompraVenta.Backend.Modules.Sale.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleResponse(
        Long id,
        UUID globalId,

        UUID employeeGlobalId,
        String employeeFullName,

        UUID clienteGlobalId,
        String clienteFullName,
        String clienteNombreAnon,

        LocalDateTime saleDate,
        String notes,
        BigDecimal total,
        List<SaleDetailResponse> details,
        LocalDateTime createdAt
) {
}
