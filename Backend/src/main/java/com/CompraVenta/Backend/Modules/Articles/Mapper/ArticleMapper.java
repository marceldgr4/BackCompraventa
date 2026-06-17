package com.CompraVenta.Backend.Modules.Articles.Mapper;

import com.CompraVenta.Backend.Modules.Articles.Dto.Request.CreateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.UpdateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Response.ArticleResponse;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {
    public Article toEntity(CreateArticleRequest request){
        return Article.builder()
                .nameArticle(request.nameArticle().trim())
                .description(request.description())
                .category(request.category())
                .sourceType(request.sourceType() !=null ? request.sourceType(): SourceType.OTROS)
                .itemState(request.itemStatus())
                .amount(request.amount()!=null ? request.amount():0)
                .price(request.price())
                .purchasePrice(request.purchasePrice())
                .clienteId(request.clienteId())
                .build();
    }
    public ArticleResponse toResponse(Article article){
        boolean hasStock = article.hasStock();
        return new ArticleResponse(
                article.getId(),
                article.getGlobalId(),
                article.getClienteId(),
                article.getNameArticle(),
                article.getDescription(),
                article.getCategory(),
                article.getSourceType(),
                article.getItemState(),
                article.getAmount(),
                article.getPrice(),
                article.getPurchasePrice(),
                hasStock,
                hasStock ? "Disponible": "sin stock",
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
    public  void applyUpdate(Article article, UpdateArticleRequest request){
        if(request.nameArticle()!=null)article.setNameArticle(request.nameArticle().trim());
        if(request.description()!=null)article.setDescription(request.description().trim());
        if(request.category()!=null)article.setCategory(request.category());
        if (request.itemStatus() !=null)article.setItemState(request.itemStatus());
        if (request.price()!=null)article.setPrice(request.price());
        if (request.purchasePrice()!=null)article.setPurchasePrice(request.purchasePrice());
    }

    public void applyBasicUpdate(Article article, UpdateArticleRequest request){
        if(request.nameArticle()!=null)article.setNameArticle(request.nameArticle().trim());
        if(request.description()!=null)article.setDescription(request.description().trim());
        if(request.category()!=null)article.setCategory(request.category());
        if (request.itemStatus() !=null)article.setItemState(request.itemStatus());

    }
}

