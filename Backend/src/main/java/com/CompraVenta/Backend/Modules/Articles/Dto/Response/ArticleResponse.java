package com.CompraVenta.Backend.Modules.Articles.Dto.Response;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArticleResponse(
        Long id,
        UUID globalId,
        Long clienteId,
        String nameArticle,
        String description,
        ArticleCategory category,
        SourceType sourceType,
        ItemStatus itemStatus,
        Integer amount,
        BigDecimal price,
        BigDecimal purchasePrice,

        boolean hasStock,
        String stockStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
