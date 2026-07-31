package com.CompraVenta.Backend.Modules.Sale.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleDetailResponse(
        Long id,
        UUID articleGlobalId,
        String articleName,
        Integer amount,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
