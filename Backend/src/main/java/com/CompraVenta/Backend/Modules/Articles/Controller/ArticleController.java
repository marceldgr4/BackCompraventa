package com.CompraVenta.Backend.Modules.Articles.Controller;

import com.CompraVenta.Backend.Modules.Articles.Dto.Request.CreateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.StockAdjustResquest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Request.UpdateArticleRequest;
import com.CompraVenta.Backend.Modules.Articles.Dto.Response.ArticleResponse;
import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import com.CompraVenta.Backend.Modules.Articles.Service.ArticleService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Validated
@Tag(name = "Articles", description = "gestion de invetario de articulos")
public class ArticleController {
    private final ArticleService articleService;

    //---Get---
    @GetMapping
    @Operation(summary = "Lista articulos con filtros y paginacion",
    description = "admin ve todo los articulos. empleado solo ve los disponible(amount > 0).")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> findAll(
            @Parameter(description = "Busqueda parcial por nombre")
            @RequestParam(required = false)
            String search,
            @Parameter(description = "filtar por categori")
            @RequestParam(required = false)ArticleCategory category,
            @Parameter(description = "Solo article con stock > 0")
            @RequestParam(defaultValue = "false")Boolean onlyAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size,
            @RequestParam(defaultValue = "nameArticle")String sortBy){

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return  ResponseEntity.ok(ApiResponse.ok(
                articleService.findAll(search,category,onlyAvailable, pageable)));

    }
    @GetMapping("/{globalId}")
    @Operation(summary = "obtener detalle de un articulo por UUID")
    public ResponseEntity<ApiResponse<ArticleResponse>> findByGlobalId(
            @PathVariable UUID globalId
    ){
        return ResponseEntity.ok(ApiResponse.ok(articleService.findByGlobalId(globalId)));
    }
    //---POST---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear articulo (solo admin)")
    public ResponseEntity<ApiResponse<ArticleResponse>> create(
            @RequestBody
            @Valid CreateArticleRequest request
            ){
        ArticleResponse created = articleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{globalId}")
                .buildAndExpand(created.globalId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));
    }
    //---PUT---
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actulizar articulos completos")
    public ResponseEntity<ApiResponse<ArticleResponse>> update(
            @PathVariable UUID globalId,
            @RequestBody
            @Valid UpdateArticleRequest request
    ){
        return ResponseEntity.ok(ApiResponse.ok(articleService.update(globalId, request)));
    }
    //---DELETE----
    @DeleteMapping("(/{globalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar articulos")
    public ResponseEntity<Void> delete(@PathVariable UUID globalId){
        articleService.delete(globalId);
        return  ResponseEntity.noContent().build();
    }

    //---PATH----
    @PatchMapping("/{globalId}/basic")
    @Operation(summary = "actulzar campos basicos")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateBasic(
            @PathVariable UUID globalId,
            @RequestBody
            @Valid UpdateArticleRequest request
    ){
        return ResponseEntity.ok(ApiResponse.ok(articleService.update(globalId, request)));
    }
    @PatchMapping("/{globalId}/stock/add")
    @Operation(summary = "agregar unidades al stock")
    public ResponseEntity<ApiResponse<ArticleResponse>> addStock(
            @PathVariable UUID globalId,
            @RequestBody  @Valid StockAdjustResquest request
    ){
        return ResponseEntity.ok(ApiResponse.ok(
                articleService.addStock(globalId, request),
                "Stock actulizado correctamente"
        ));
    }
    @PatchMapping("/{globalId}/stock/remove")
    @Operation(summary = "retirar unidades del stock")
    public ResponseEntity<ApiResponse<ArticleResponse>> removeStock(
            @PathVariable UUID globalId,
            @RequestBody @Valid StockAdjustResquest request
    ){
        return ResponseEntity.ok(ApiResponse.ok(articleService.removeStock(globalId, request),
                "Stock retirado correctamente"));
    }


}
