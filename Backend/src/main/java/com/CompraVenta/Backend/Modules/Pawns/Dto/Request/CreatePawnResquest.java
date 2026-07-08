package com.CompraVenta.Backend.Modules.Pawns.Dto.Request;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePawnResquest(
        @NotNull(message = "El articulo es obligatorio")
        UUID articleGlobalId,
        @NotNull(message = "El cliente es obligatorio")
        UUID clienteGloabalId,
        @NotNull(message = "la cantidad es obligatorio")
        @Min(value = 1, message = "el precio debe ser mayor a 0")
        Integer amount,

        @NotNull(message = "El precio del empeño es obligatorio")
        @DecimalMin(value = "0.01", message = "el precio debe ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "el precio no tenmer mas de 2 decimales")
        BigDecimal price,
        @Digits(integer = 8, fraction = 2, message = "El peso no pude temer mas de 2 decimales")
        BigDecimal weightGrame,

        @NotNull(message = "la cantidad de cuotas es es obligatorio")
        @Min(value = 1,message = "debe haber al menos 1 cuota")
        Integer installmentCount,

        @NotNull(message = "la fecha de empeño es obligatorio")
        LocalDate pawnDate,
        @NotNull(message = "La fecha de devolucion es obligatorio")
        LocalDate returnDate,

        String notes
) {
}
