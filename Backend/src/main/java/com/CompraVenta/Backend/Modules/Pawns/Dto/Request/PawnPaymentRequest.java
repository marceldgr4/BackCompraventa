package com.CompraVenta.Backend.Modules.Pawns.Dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PawnPaymentRequest(
        @NotNull(message = "el monto es obligatorio")
        @PositiveOrZero(message = "el monto no puede ser negativo")
        BigDecimal amount,
        @NotNull(message = "La fecha de pagos es obligatorio")
        LocalDate paymentDate,
        String notes
) {
}
