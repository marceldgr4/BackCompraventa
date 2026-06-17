package com.CompraVenta.Backend.Modules.Articles.Entity;


import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Enums.ItemStatus;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import com.CompraVenta.Backend.Shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Builder
@Entity
@Table(
        name = "articles",
        schema = "public",
        indexes = {
                @Index(name = "idx_article_global_id", columnList = "global_id", unique = true),
                @Index(name = "idx_articles_name",        columnList = "name_article"),
                @Index(name = "idx_articles_category",    columnList = "category"),
                @Index(name = "idx_articles_amount",      columnList = "amount"),
                @Index(name = "idx_articles_source_type", columnList = "source_type")

        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "name_article",nullable = false,length = 255)
    private String nameArticle;

    @Column(name = "description",columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category",nullable = false, length = 50)
    private ArticleCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_Type",length = 20)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_State",length = 20)
    private ItemStatus itemState;

    @Column(name = "amount",nullable = false)
    @Builder.Default
    private Integer amount =0;

    @Column(name = "price",nullable = false,precision = 12,scale = 2)
    private BigDecimal price;

    @Column(name = "purchase_Price",precision = 12,scale = 2)
    private  BigDecimal purchasePrice;

    public boolean hasStock() {
        return this.amount !=null && this.amount > 0;
    }
    public boolean hasNegativeMargin(){
        return purchasePrice != null && price != null && purchasePrice.compareTo(price) >= 0;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Article a)) return false;
        return  getId() != null && getId().equals(a.getId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
