package com.CompraVenta.Backend.Modules.Purchases.Service.Impl;

import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.PurchaseItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCreationService {

    private final ArticleRepository articleRepository;

    public Article createFromPurchaseItem(Cliente cliente, PurchaseItemRequest item) {
        Article article = Article.builder()
                .clienteId(cliente != null ? cliente.getId() : null)
                .nameArticle(item.articleName().trim())
                .description(item.articleDescription() != null ? item.articleDescription().trim() : null)
                .category(item.articleCategory())
                .sourceType(SourceType.COMPRA)
                .itemState(item.articleItemStatus())
                .amount(item.amount())
                .price(item.salePrice())
                .purchasePrice(item.purchasePrice())
                .build();

        validateMargin(article);
        article = articleRepository.save(article);

        log.info("Artículo creado: id={}, nombre={}, proveedor={}",
                article.getId(), article.getNameArticle(),
                cliente != null ? cliente.getFirstName() : "Anónimo");

        return article;
    }

    private void validateMargin(Article article) {
        if (article.hasNegativeMargin()) {
            log.warn("Artículo '{}' con margen negativo o nulo: compra={}, venta={}",
                    article.getNameArticle(), article.getPurchasePrice(), article.getPrice());
        }
    }
}
