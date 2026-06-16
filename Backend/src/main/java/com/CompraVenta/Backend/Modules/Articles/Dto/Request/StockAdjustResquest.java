package com.CompraVenta.Backend.Modules.Articles.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockAdjustResquest(
        @NotNull(message = "La cantidad es obligatorio")
        @Min(value = 1, message = "la cantidad debe ser al menos 1")
        Integer quantity,
        String reason
) {
}
