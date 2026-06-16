package com.CompraVenta.Backend.Modules.Articles.Dto.Request;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateArticleRequest(
        @NotBlank(,message = "El nombre del articulo es obligatorio")
        @Size(max = 255, message = "El nombre no puede supera 255 caractres")
        String nameArticle,

        @Size(max = 2000, message = "La descripcion no puede sueperar 2000 caractres")
        String description,

        @NotNull(message = "la categoria es obligatoria")
        ArticleCategory category,

        SourceType sourceType,
        ItemStatus itemStatus,

        @PositiveOrZero(message = "la cantidad no puede ser negativa")
        Integer amount,

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01",message = "el precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2,message = "el precio no tener mas de 2 deciamles")
        BigDecimal price,

        @Digits(integer = 10,fraction = 2)
        BigDecimal purchasePrice,

        Long clienteId
) {
}
