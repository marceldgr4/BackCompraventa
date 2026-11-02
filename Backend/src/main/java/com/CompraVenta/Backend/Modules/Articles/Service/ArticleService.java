package com.CompraVenta.Backend.Modules.Articles.Service;


import com.CompraVenta.Backend.Modules.Articles.Dto.Request.CreateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.StockAdjustRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.UpdateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Response.ArticleResponse;
import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ArticleService {

    PageResponse<ArticleResponse> findAll(String search, ArticleCategory articleCategory,
                                          Boolean onlyAvailable, Pageable pageable);

    ArticleResponse findByGlobalId(UUID globalId);
    ArticleResponse create(CreateArticleRequest request);
    ArticleResponse update(UUID globalId, UpdateArticleRequest request);
    ArticleResponse addStock(UUID globalId, StockAdjustRequest request);
    ArticleResponse removeStock(UUID globalId, StockAdjustRequest request);
    void delete(UUID globalId);
}
