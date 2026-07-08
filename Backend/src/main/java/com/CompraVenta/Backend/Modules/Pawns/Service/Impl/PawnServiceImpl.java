package com.CompraVenta.Backend.Modules.Pawns.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Enums.SourceType;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Enums.RegistrationType;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreateAgilePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreatePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.PawnPaymentRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnPaymentResponse;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnResponse;
import com.CompraVenta.Backend.Modules.Pawns.Entity.Pawn;
import com.CompraVenta.Backend.Modules.Pawns.Entity.PawnPayment;
import com.CompraVenta.Backend.Modules.Pawns.Enums.PawnStatus;
import com.CompraVenta.Backend.Modules.Pawns.Mapper.PawnMapper;
import com.CompraVenta.Backend.Modules.Pawns.Repository.PawnPaymentRepository;
import com.CompraVenta.Backend.Modules.Pawns.Repository.PawnRepository;
import com.CompraVenta.Backend.Modules.Pawns.Service.PawnService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PawnServiceImpl implements PawnService {

    private final PawnRepository pawnRepository;
    private final PawnPaymentRepository pawnPaymentRepository;
    private final ArticleRepository articleRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private final PawnMapper pawnMapper;

    private Employee getCurrentEmployee() {
        String email = SecurityContext.getCurrentUsername();
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("No se encontró el empleado autenticado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PawnResponse> findAll(PawnStatus status, UUID clienteGlobalId, UUID employeeGlobalId,
                                      LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Long clienteId = null;
        if (clienteGlobalId != null) {
            clienteId = clienteRepository.findByGlobalId(clienteGlobalId)
                    .map(Cliente::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }
        
        Long employeeId = null;
        if (employeeGlobalId != null) {
            employeeId = employeeRepository.findByGlobalId(employeeGlobalId)
                    .map(Employee::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));
        }

        return pawnRepository.findByFilters(status, employeeId, clienteId, dateFrom, dateTo, pageable)
                .map(pawnMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PawnResponse findByGlobalId(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE_PAWN")
    public PawnResponse create(CreatePawnRequest request) {
        Employee employee = getCurrentEmployee();
        
        Cliente cliente = clienteRepository.findByGlobalId(request.clienteGloabalId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        
        if (!cliente.isActive()) {
            throw new BusinessException("El cliente no está activo");
        }

        Article article = articleRepository.findByGlobalId(request.articleGlobalId())
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        if (!article.hasStock() || article.getAmount() < request.amount()) {
            throw new BusinessException("No hay stock suficiente para el artículo");
        }

        int updatedRows = articleRepository.adjustStock(article.getId(), -request.amount());
        if (updatedRows == 0) {
            throw new BusinessException("Error al actualizar el stock del artículo");
        }
        article.setAmount(article.getAmount() - request.amount());

        Pawn pawn = pawnMapper.toEntity(request, article, cliente, employee);
        pawn = pawnRepository.save(pawn);
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE_AGILE_PAWN")
    public PawnResponse createAgile(CreateAgilePawnRequest request) {
        Employee employee = getCurrentEmployee();

        Cliente cliente = Cliente.builder()
                .firstName(request.clienteFirstName())
                .cedula(request.clienteCedula())
                .phone(request.clientePhone())
                .registrationType(RegistrationType.RAPIDO)
                .status(ClienteStatus.ACTIVO)
                .build();
        cliente = clienteRepository.save(cliente);

        Article article = Article.builder()
                .clienteId(cliente.getId())
                .nameArticle(request.articleName())
                .description(request.articleDescription())
                .category(request.articleCategory())
                .sourceType(SourceType.EMPENO)
                .itemState(request.articleItemStatus())
                .amount(0) // Stock in sales inventory is 0 since it is pawned
                .price(request.articlePrice())
                .build();
        article = articleRepository.save(article);
        
        CreatePawnRequest pawnRequest = new CreatePawnRequest(
                article.getGlobalId(),
                cliente.getGlobalId(),
                request.amount(),
                request.pawnPrice(),
                request.weightGrams(),
                request.installmentCount(),
                request.pawnDate(),
                request.returnDate(),
                request.notes()
        );

        Pawn pawn = pawnMapper.toEntity(pawnRequest, article, cliente, employee);
        pawn = pawnRepository.save(pawn);
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "REGISTER_PAWN_PAYMENT")
    public PawnPaymentResponse registerPayment(UUID globalId, PawnPaymentRequest request) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
                
        Employee employee = getCurrentEmployee();

        if (!pawn.canAcceptPayments()) {
            throw new BusinessException("El empeño no acepta pagos en su estado actual");
        }

        pawn.registerPayment(request.amount());
        
        PawnPayment payment = PawnPayment.builder()
                .pawnId(pawn.getId())
                .pawn(pawn)
                .amount(request.amount())
                .paymentDate(LocalDate.now())
                .notes(request.notes())
                .createdByEmployeeId(employee.getId())
                .build();
                
        payment = pawnPaymentRepository.save(payment);
        pawnRepository.save(pawn);
        
        return pawnMapper.toPawnPaymentRequest(payment, pawn.getGlobalId(), employee.getGlobalId());
    }

    @Override
    @Transactional
    @Auditable(action = "REGISTER_MISSED_INSTALLMENT")
    public PawnPaymentResponse registerMissedInstallment(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
        Employee employee = getCurrentEmployee();

        pawn.registerMissedInstallments();
        pawnRepository.save(pawn);
        
        PawnPayment payment = PawnPayment.builder()
                .pawnId(pawn.getId())
                .pawn(pawn)
                .paymentDate(LocalDate.now())
                .createdByEmployeeId(employee.getId())
                .missed(true)
                .notes("Cuota impagada")
                .build();
        
        payment = pawnPaymentRepository.save(payment);
        
        return pawnMapper.toPawnPaymentRequest(payment, pawn.getGlobalId(), employee.getGlobalId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PawnPaymentResponse> findPayments(UUID globalId, Pageable pageable) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
        
        Page<PawnPayment> payments = pawnPaymentRepository.findByPawnIdOrderByPaymentDateDesc(pawn.getId(), pageable);
        
        Page<PawnPaymentResponse> responses = payments.map(p -> 
            pawnMapper.toPawnPaymentRequest(p, pawn.getGlobalId(), null)
        );
        return PageResponse.from(responses);
    }

    @Override
    @Transactional
    @Auditable(action = "MARK_PAWN_RETURNED")
    public PawnResponse markAsReturned(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
                
        pawn.markAsReturned();
        pawn = pawnRepository.save(pawn);
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "MARK_PAWN_LOST")
    public PawnResponse markAsLost(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
                
        pawn.markAsLost();
        pawn = pawnRepository.save(pawn);
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "MARK_PAWN_EXPIRED")
    public PawnResponse markAsExpiredManually(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
                
        pawn.markAsExpired();
        pawn = pawnRepository.save(pawn);
        return pawnMapper.toResponse(pawn);
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE_PAWN")
    public void delete(UUID globalId) {
        Pawn pawn = pawnRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Empeño no encontrado"));
        pawnRepository.delete(pawn);
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 30000)
    public int expireOverduePawns() {
        return pawnRepository.expireOverduePawns();
    }
}
