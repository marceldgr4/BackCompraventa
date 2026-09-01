package com.CompraVenta.Backend.Modules.Purchases.Dto.Request;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PurchaseItemRequest(
        @NotBlank(message = "El nombre del articulo es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String articleName,

        @Size(max = 2000, message = "La descripcion no puede 2000 caracteres")
        String articleDescription,

        @NotNull(message = "La categoria del articulo es obligatoria")
        ArticleCategory articleCategory,
        ItemStatus articleItemStatus,

        @NotNull(message = "la cantidad es obligatoria")
        @Min(value = 1, message = "la cantidad debe ser al 1")
        Integer amount,

        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio de compra no puede tener más de 2 decimales")
        BigDecimal purchasePrice,

        @NotNull(message = "El precio de venta sugerido es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio de venta no puede tener más de 2 decimales")
        BigDecimal salePrice
) {
}
