package com.CompraVenta.Backend.Modules.Sale.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemRequest(
        @NotNull(message = "El articulo es obligatorio")
        UUID articleGlobalId,

        @NotNull(message = "la cantidad es obligatoria")
        @Min(value = 1, message = "la cantidad debe ser mayor a 0")
        Integer amount,

        @NotNull(message = "el precio unitario es obligatorio")
        @DecimalMin(value = "0.01", message = "el precio unitario debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "el precio unitario no puede tener mas de 2 decimales")
        BigDecimal unitPrice
) {
}
