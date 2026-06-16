package com.CompraVenta.Backend.Modules.Articles.Dto.Request;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateArticleRequest(
        @Size(max = 255)
        String nameArticle,
        @Size(max = 2000)
        String description,
        ArticleCategory category,
        ItemStatus itemStatus,

        @DecimalMin(value = "0.01", message = "el precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal purchasePrice
) {
}
