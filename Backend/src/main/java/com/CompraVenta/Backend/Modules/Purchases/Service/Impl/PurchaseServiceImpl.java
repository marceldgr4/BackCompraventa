package com.CompraVenta.Backend.Modules.Purchases.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.CreatePurchaseRequest;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.PurchaseItemRequest;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Response.PurchaseResponse;
import com.CompraVenta.Backend.Modules.Purchases.Entity.Purchase;
import com.CompraVenta.Backend.Modules.Purchases.Mapper.PurchaseMapper;
import com.CompraVenta.Backend.Modules.Purchases.Repository.PurchaseRepository;
import com.CompraVenta.Backend.Modules.Purchases.Service.PurchaseService;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ArticleRepository articleRepository;
    private final PurchaseMapper purchaseMapper;
    private final ArticleCreationService articleCreationService;
    private final ClienteResolutionService clienteResolutionService;
    private final EmployeeContextService employeeContextService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseResponse> findAll(UUID clienteGlobalId, LocalDate dateFrom,
                                                  LocalDate dateTo, Pageable pageable) {
        Long clienteId = clienteResolutionService.resolveClienteIdOrNull(clienteGlobalId);
        Long employeeId = employeeContextService.isAdmin() ? null : employeeContextService.getCurrentEmployeeId();
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        Page<Purchase> purchases = purchaseRepository.findByFilters(employeeId, clienteId, from, to, pageable);
        return PageResponse.from(purchases.map(purchaseMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse findByGlobalId(UUID globalId) {
        Purchase purchase = findEntityOrThrow(globalId);
        employeeContextService.validatePurchaseAccessOrThrow(purchase);
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    @Auditable(operation = "CREATE_PURCHASE", entity = "purchases")
    public List<PurchaseResponse> create(CreatePurchaseRequest request) {
        Employee employee = employeeContextService.getCurrentEmployee();
        Cliente cliente = clienteResolutionService.resolveOrCreate(request);

        List<PurchaseResponse> created = request.items().stream()
                .map(item -> registerSingleItemPurchase(employee, cliente, item, request.notes()))
                .map(purchaseMapper::toResponse)
                .toList();

        log.info("Compra registrada: employeeId={}, clienteId={}, items={}",
                employee.getId(), cliente != null ? cliente.getId() : null, created.size());
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "DELETE_PURCHASE", entity = "purchases")
    public void delete(UUID globalId) {
        Purchase purchase = findEntityOrThrow(globalId);
        Article article = articleRepository.findById(purchase.getArticleId())
                .orElseThrow(() -> new BusinessException("Artículo no encontrado"));

        if (articleRepository.hasActiveSales(article.getId())) {
            throw new BusinessException("No se puede anular la compra: el artículo '" + article.getNameArticle() +
                    "' tiene ventas registradas asociadas.");
        }
        if (articleRepository.hasActivePawns(article.getId())) {
            throw new BusinessException("No se puede anular la compra: el artículo '" + article.getNameArticle() +
                    "' tiene empeños activos o vencidos asociados.");
        }

        purchaseRepository.delete(purchase);
        articleRepository.delete(article);
        log.info("Compra anulada: globalId={}, articulo eliminado={}", globalId, article.getNameArticle());
    }

    private Purchase registerSingleItemPurchase(Employee employee, Cliente cliente,
                                                PurchaseItemRequest item, String notes) {
        Article article = articleCreationService.createFromPurchaseItem(cliente, item);

        Purchase purchase = Purchase.builder()
                .employee(employee)
                .employeeId(employee.getId())
                .cliente(cliente)
                .clienteId(cliente != null ? cliente.getId() : null)
                .article(article)
                .articleId(article.getId())
                .purchasePrice(item.purchasePrice())
                .notes(notes)
                .build();

        return purchaseRepository.save(purchase);
    }

    private Purchase findEntityOrThrow(UUID globalId) {
        return purchaseRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", globalId));
    }
}
