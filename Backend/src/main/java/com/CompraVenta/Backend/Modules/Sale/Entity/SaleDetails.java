package com.CompraVenta.Backend.Modules.Sale.Entity;

import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Entity
@Table(
        name = "sales_details",
        schema = "public",
        indexes = {
                @Index(name = "idx_sale_details_sale_id",columnList = "sale_id"),
                @Index(name = "idx_sale_details_article",columnList = "article_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false, insertable = false,updatable = false)
    private Sale sale;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id",nullable = false,insertable = false,updatable = false)
    private Article article;

    @Column(name = "article_id",nullable = false)
    private Long articleId;

    @Column(name = "amount",nullable = false)
    private Integer amount;

    @Column(name = "unit_price", nullable = false, precision = 12,scale = 2)
    private BigDecimal unitPrice;

    public  BigDecimal getSubTotal(){
        return unitPrice.multiply(BigDecimal.valueOf(amount));
    }
}
