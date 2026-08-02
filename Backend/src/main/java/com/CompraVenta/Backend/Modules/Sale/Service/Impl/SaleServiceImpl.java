package com.CompraVenta.Backend.Modules.Sale.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.Sale.Dto.Request.CreateSaleRequest;
import com.CompraVenta.Backend.Modules.Sale.Dto.Request.SaleItemRequest;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleResponse;
import com.CompraVenta.Backend.Modules.Sale.Entity.Sale;
import com.CompraVenta.Backend.Modules.Sale.Mapper.SaleMapper;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleDetailRepository;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleProcedureRepository;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleRepository;
import com.CompraVenta.Backend.Modules.Sale.Service.SaleService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
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
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private  final SaleProcedureRepository saleProcedureRepository;
    private final ArticleRepository articleRepository;
    private  final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private  final SaleMapper saleMapper;

    private Employee getCurrentEmployee() {
        String email = SecurityContext.getCurrentUsername();
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(()-> new BusinessException(" No se encontro el empleado autenticado"));

    }



    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> findAll(UUID clienteGlobalId, LocalDate dateFrom,
                                              LocalDate dateTo, Pageable pageable) {
        Long clienteId = resolveClienteId(clienteGlobalId);
        Long employeeId = SecurityContext.hasRole("ADMIN") ? null : getCurrentEmployee().getId();
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        Page<Sale> sales = saleRepository.findByFilters(employeeId, clienteId, from, to, pageable);
        return PageResponse.from(sales.map(this::toResponseWithDetails));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findByGlobalId(UUID globalId) {
        Sale sale =findEntityOrThrow(globalId);
        if(!SecurityContext.hasRole("ADMIN") && ! isOwnedByCurrentEmployee(sale)){
            throw new BusinessException(" No tiene permiso para ver esta vental");

        }
        return toResponseWithDetails(sale);
    }

    @Override
    @Transactional
    @Auditable(operation = "CREATE_SALE", entity = "sales")
    public SaleResponse create(CreateSaleRequest request){
        Employee employee = getCurrentEmployee();
        Long clienteId = resolveActiveClienteId(request.clienteGlobalId());

        List<SaleProcedureRepository.SaleItemPayload> payload = request.items().stream()
                .map(this::toItemPayload)
                .toList();
        Long saleId = saleProcedureRepository.registerSale(
                employee.getId(),
                clienteId,
                request.clienteNombreAnon(),
                request.notes(),
                payload
        );
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(()-> new BusinessException(" No se pudo recuperar la venta recien creada el empleado"));

        log.info("La venta creada : ID={}, globalId={}, employeeId={}", sale.getId(), sale.getGlobalId(), sale.getEmployeeId());
        return  toResponseWithDetails(sale);

    }
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "DELETE_SALE", entity = "sales")
    public void delete(UUID globalId) {
        Sale sale = findEntityOrThrow(globalId);
        
        // Return stock to articles
        var details = saleDetailRepository.findBySaleIdOrderById(sale.getId());
        for (var detail : details) {
            Article article = articleRepository.findById(detail.getArticleId())
                    .orElseThrow(() -> new BusinessException("Artículo no encontrado para revertir stock"));
            article.setAmount(article.getAmount() + detail.getAmount());
            articleRepository.save(article);
        }
        
        sale.setDeleted(true);
        saleRepository.save(sale);
        log.info("Venta eliminada lógicamente (anulada) globalId ={}", globalId);
    }

    private SaleProcedureRepository.SaleItemPayload toItemPayload(SaleItemRequest item) {
        Article article = articleRepository.findByGlobalId(item.articleGlobalId())
                .orElseThrow(()-> new ResourceNotFoundException("Article", item.articleGlobalId()));
        return  new SaleProcedureRepository.SaleItemPayload(article.getId(), item.amount(),item.unitPrice());

    }

    private SaleResponse toResponseWithDetails(Sale sale) {
        var details = saleDetailRepository.findBySaleIdOrderById(sale.getId());
        return saleMapper.toResponse(sale, details);
    }
    private Long resolveClienteId(UUID clienteGlobalId) {
        if(clienteGlobalId == null) return null;
        return clienteRepository.findByGlobalId(clienteGlobalId)
                .map(Cliente::getId)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente", clienteGlobalId));
    }
    private Long resolveActiveClienteId(UUID clienteGlobalId) {
        if(clienteGlobalId == null) return null;
        Cliente cliente = clienteRepository.findByGlobalId(clienteGlobalId)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente", clienteGlobalId));
        if(!cliente.isActive()){
            throw new BusinessException("El cliente no está activo");
        }
        return cliente.getId();
    }
    private boolean isOwnedByCurrentEmployee(Sale sale) {
        return  getCurrentEmployee().getId().equals(sale.getEmployeeId());
    }

    private Sale findEntityOrThrow(UUID globalId) {
        return saleRepository.findByGlobalId(globalId)
                .orElseThrow(()-> new ResourceNotFoundException("Sale Global", globalId));
    }


}
