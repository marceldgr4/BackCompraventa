package com.CompraVenta.Backend.Modules.Articles.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.CreateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.StockAdjustResquest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.UpdateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Response.ArticleResponse;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Mapper.ArticleMapper;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Articles.Service.ArticleService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> findAll(String search, ArticleCategory category,
                                                 Boolean onLyAvailable, Pageable pageable){
        boolean isAdmin = SecurityContext.hasRole("ADMIN");
        boolean effectiveOnlyAvailable = isAdmin ? onLyAvailable :Boolean.TRUE;
        return PageResponse.from(
                ArticleRepository.findByFilters(search,category,effectiveOnlyAvailable, pageable).map(articleMapper::toResponse)
        );
    }
    @Override
    @Transactional(readOnly = true)
    public ArticleResponse findByGlobalId(UUID globalId) {
        return articleRepository.findByGlobalId(globalId)
                .map(articleMapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Article",globalId));
    }
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "CREATE_ARTICLE", entity = "articles")
    public ArticleResponse create(CreateArticleRequest request){
        Article article = articleMapper.toEntoty(request);

        if(article.hasNegativeMargin()){
            log.warn("Articulo '{}' creado con marge neagtivo: compra={}, venta{}," +
                    article.getNameArticle(),article.getPurchasePrice(), article.getPrice());
        }
        Article saved = articleRepository.save(article);
        log.info("Articulo creado: id{}, globalId={},categori={}",
                saved.getId(),saved.getGlobalId(),saved.getCategory());
        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "UPDATE_ARTICLE",entity = "articles")
    public ArticleResponse update(UUID globalId, UpdateArticleRequest request) {
        Article article = findEntityOrThrow(globalId);
        articleMapper.applyUpdate(article,request);
        if ((article.hasNegativeMargin())){
            log.warn("Articulo '{}' actulzado con margen negativo: compra ={}, ventas={}",
                    article.getNameArticle(),article.getPurchasePrice(), article.getPrice());
        }
        Article saved = articleRepository.save(article);
        log.info("Articulo actulizado (full): gloabalId={}",globalId);
        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Auditable(operation = "ADD_STOCK",entity = "articles")
    public ArticleResponse addStock(UUID globalId, StockAdjustResquest resquest){
        Article article = findEntityOrThrow(globalId);
        int updated = articleRepository.adjustStock(article.getId(),resquest.quantity());
        if (updated == 0){
            throw new BusinessException("No se pudo actualizar el stock"+ globalId);
        }
        Article refreshed = findEntityOrThrow(globalId);
        log.info("Stock agregar: globalId={}, cantidad={}, nuevoStock ={}",
                globalId, resquest.quantity(), refreshed.getAmount());
        return articleMapper.toResponse(refreshed);
    }
    private Article findEntityOrThrow(UUID globalId){
        return  articleRepository.findByGlobalId(globalId)
                .orElseThrow(()->new ResourceNotFoundException("Article",globalId));
    }
    @Override
    @Transactional
    @Auditable(operation = "REMOVE_STOCK",entity = "articles")
    public ArticleResponse removeStock(UUID globalId, StockAdjustResquest resquest){
        Article article = findEntityOrThrow(globalId);
        if (article.getAmount()< resquest.quantity()){
            throw new BusinessException(String.format("Stock insuficiente para el artículo '%s'. Disponible: %d, solicitado: %d",
                    article.getNameArticle(),article.getAmount(),resquest.quantity()));
        }
        int update = articleRepository.adjustStock(article.getId(),resquest.quantity());
        if (update == 0){
            throw new BusinessException("No se pudo reducir el stock. Verifique la cantidad disponible."+ globalId);
        }
        Article refreshed = findEntityOrThrow(globalId);
        log.info("Stock retirado: globalId={}, cantidad={}, nuevoStock={}",
                globalId, resquest.quantity(), refreshed.getAmount());
        return articleMapper.toResponse(refreshed);
    }
    @Override
    @Transactional
    @Auditable(operation = "DELETE_ARTICLE",entity = "articles")
    public void delete(UUID globalId){
        Article article = findEntityOrThrow(globalId);
        if(articleRepository.hasActivePawns(article.getId())){
            throw new BusinessException(
                    "No se puede eliminar el artículo '" + article.getNameArticle() +
                            "': tiene empeños activos o vencidos asociados.");

        }
        if (articleRepository.hasActiveSales(article.getId())){
            throw new BusinessException(
                    "No se puede eliminar el artículo '" + article.getNameArticle() +
                            "': tiene ventas registradas asociadas.");
        }
        articleRepository.delete(article);
        log.info("Articulo eliminado: globalId ={}, nombre={}", globalId, article.getNameArticle());
    }
}
