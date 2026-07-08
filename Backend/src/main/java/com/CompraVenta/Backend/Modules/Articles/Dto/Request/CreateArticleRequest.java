package com.CompraVenta.Backend.Modules.Articles.Dto.Request;

import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "El nombre del articulo es obligatorio")
    @Size(max = 255, message = "El nombre no puede supera 255 caractres")
    private String nameArticle;

    @Size(max = 2000, message = "La descripcion no puede sueperar 2000 caractres")
    private String description;

    @NotNull(message = "la categoria es obligatoria")
    private ArticleCategory category;

    private SourceType sourceType;
    private ItemStatus itemStatus;

    @PositiveOrZero(message = "la cantidad no puede ser negativa")
    private Integer amount;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "el precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "el precio no tener mas de 2 deciamles")
    private BigDecimal price;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal purchasePrice;

    private Long clienteId;
}
