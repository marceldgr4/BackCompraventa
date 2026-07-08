package com.CompraVenta.Backend.Modules.Pawns.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawnPaymentResponse(
        Long id,
        UUID pawnGlobalId,
        BigDecimal amount,
        LocalDate paymentDate,
        String notes,
        UUID CreateByEmployeeGlobalId,
        boolean isMissed,
        Instant createdAt
) {
}
