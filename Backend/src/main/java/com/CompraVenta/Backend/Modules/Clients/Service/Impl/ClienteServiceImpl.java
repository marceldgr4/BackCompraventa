package com.CompraVenta.Backend.Modules.Clients.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Mapper.ClienteMapper;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Clients.Service.ClienteService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del servicio de clientes.
 *
 * Correcciones aplicadas respecto a la versión anterior:
 * - RF-06.3: validación de phone duplicado en create() y update().
 * - RF-06.4: findAll() filtra automáticamente por ACTIVO para rol Empleado.
 * - RF-06.5/6: delete() hace soft-delete a ELIMINADO; hardDelete() hace físico.
 * - BUG-7: import muerto de Status eliminado (en Cliente.java).
 * - Renombrado promoteComplete() → promoteToComplete() para consistencia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> findAll(ClienteStatus status, Pageable pageable) {
        /*
         * RF-06.4: Un Empleado solo puede ver clientes ACTIVO,
         * independientemente del filtro que envíe en el request.
         * Un Admin puede filtrar libremente o ver todos.
         */
        ClienteStatus effectiveStatus = resolveEffectiveStatus(status);

        Page<ClienteResponse> page = (effectiveStatus != null)
                ? clienteRepository.findAllByStatus(effectiveStatus, pageable).map(clienteMapper::toClienteResponse)
                : clienteRepository.findAll(pageable).map(clienteMapper::toClienteResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByGlobalId(UUID globalId) {
        return clienteRepository.findByGlobalId(globalId)
                .map(clienteMapper::toClienteResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", globalId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> search(String term, ClienteStatus status, Pageable pageable) {
        ClienteStatus effectiveStatus = resolveEffectiveStatus(status);
        return PageResponse.from(
                clienteRepository.searchByTerm(term, effectiveStatus, pageable)
                        .map(clienteMapper::toClienteResponse)
        );
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(operation = "CREATE_CLIENTE", entity = "clientes")
    public ClienteResponse create(CreateClienteRequest request) {
        validateUniqueCedulaForCreate(request.cedula());
        validateUniquePhoneForCreate(request.phone());

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente saved   = clienteRepository.save(cliente);

        log.info("Cliente creado: id={}, globalId={}, tipo={}",
                saved.getId(), saved.getGlobalId(), saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "UPDATE_CLIENTE", entity = "clientes")
    public ClienteResponse update(UUID globalId, UpdateClienteRequest request) {
        Cliente cliente = findEntityOrThrow(globalId);

        validateUniqueCedulaForUpdate(request.cedula(), cliente.getId());
        validateUniquePhoneForUpdate(request.phone(), cliente.getId());

        clienteMapper.applyUpdates(cliente, request);
        Cliente saved = clienteRepository.save(cliente);

        log.info("Cliente actualizado: globalId={}, tipo={}", globalId, saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "DELETE_CLIENTE", entity = "clientes")
    public void delete(UUID globalId) {
        Cliente cliente = findEntityOrThrow(globalId);
        cliente.setStatus(ClienteStatus.ELIMINADO);
        clienteRepository.save(cliente);
        log.info("Cliente eliminado (soft): globalId={}", globalId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(operation = "HARD_DELETE_CLIENTE", entity = "clientes")
    public void hardDelete(UUID globalId) {
        Cliente cliente = findEntityOrThrow(globalId);

        /*
         * RF-06.6: No se puede hacer hard delete si el cliente tiene
         * operaciones asociadas. La comprobación se delega a la constraint
         * de FK en BD (ON DELETE RESTRICT por defecto). Si existe alguna
         * relación, PostgreSQL lanzará DataIntegrityViolationException que
         * el GlobalExceptionHandler convierte en 422.
         *
         * Si se requiere un mensaje de error más descriptivo, agregar aquí
         * queries de conteo antes del delete.
         */
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado (hard): globalId={}", globalId);
    }

    // ── Validaciones privadas ─────────────────────────────────────────────────

    private void validateUniqueCedulaForCreate(String cedula) {
        if (hasValue(cedula)
                && clienteRepository.existsByCedulaAndStatusNot(cedula, ClienteStatus.ELIMINADO)) {
            throw new BusinessException(
                    "Ya existe un cliente activo con la cédula: " + cedula);
        }
    }

    private void validateUniquePhoneForCreate(String phone) {
        if (hasValue(phone)
                && clienteRepository.existsByPhoneAndStatusNot(phone, ClienteStatus.ELIMINADO)) {
            throw new BusinessException(
                    "Ya existe un cliente activo con el teléfono: " + phone);
        }
    }

    private void validateUniqueCedulaForUpdate(String cedula, Long currentId) {
        if (hasValue(cedula)
                && clienteRepository.existsByCedulaAndIdNotAndStatusNot(cedula, currentId, ClienteStatus.ELIMINADO)) {
            throw new BusinessException(
                    "Ya existe otro cliente con la cédula: " + cedula);
        }
    }

    private void validateUniquePhoneForUpdate(String phone, Long currentId) {
        if (hasValue(phone)
                && clienteRepository.existsByPhoneAndIdNotAndStatusNot(phone, currentId, ClienteStatus.ELIMINADO)) {
            throw new BusinessException(
                    "Ya existe otro cliente con el teléfono: " + phone);
        }
    }

    /**
     * RF-06.4: Los empleados solo ven clientes ACTIVO.
     * Los admins pueden pasar cualquier filtro (o null para ver todos).
     */
    private ClienteStatus resolveEffectiveStatus(ClienteStatus requested) {
        boolean isAdmin = SecurityContext.hasRole("ADMIN");
        if (!isAdmin) {
            return ClienteStatus.ACTIVO;
        }
        return requested;
    }

    private Cliente findEntityOrThrow(UUID globalId) {
        return clienteRepository.findByGlobalId(globalId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", globalId));
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
