package com.CompraVenta.Backend.Modules.Pawns.Dto.Request;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAgilePawnRequest(
        // ── Datos del cliente rápido ─────────────────────────────────────────
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String clienteFirstName,

        @Size(max = 10, message = "La cédula no puede superar 10 caracteres")
        String clienteCedula,

        @Pattern(regexp = "^[+\\d\\s\\-()]{0,20}$", message = "El teléfono no tiene un formato válido")
        String clientePhone,

        // ── Datos del artículo ───────────────────────────────────────────────
        @NotBlank(message = "El nombre del artículo es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String articleName,

        String articleDescription,

        @NotNull(message = "La categoría del artículo es obligatoria")
        ArticleCategory articleCategory,

        ItemStatus articleItemStatus,

        @NotNull(message = "El precio de venta de referencia es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal articlePrice,

        // ── Datos del empeño ─────────────────────────────────────────────────
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer amount,

        @NotNull(message = "El precio del empeño es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal pawnPrice,

        @Digits(integer = 8, fraction = 2)
        BigDecimal weightGrams,

        @NotNull(message = "La cantidad de cuotas es obligatoria")
        @Min(value = 1, message = "Debe haber al menos 1 cuota")
        Integer installmentCount,

        @NotNull(message = "La fecha de empeño es obligatoria")
        LocalDate pawnDate,

        @NotNull(message = "La fecha de devolución es obligatoria")
        LocalDate returnDate,

        String notes
) {
}
