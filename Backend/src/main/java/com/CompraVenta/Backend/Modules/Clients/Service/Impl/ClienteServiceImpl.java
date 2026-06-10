package com.CompraVenta.Backend.Modules.Clients.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.DuplicateResourceException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Mapper.ClienteMapper;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Clients.Service.ClienteService;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import com.CompraVenta.Backend.Audit.annotation.Auditable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    private boolean isEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_EMPLEADO"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> findAll(ClienteStatus status, Pageable pageable){
        if (isEmployee()) {
            status = ClienteStatus.ACTIVO;
        }
        return PageResponse.from(
                (status !=null
                ? clienteRepository.findAllByStatus(status,pageable)
                : clienteRepository.findAll(pageable))
                .map(clienteMapper::toClienteResponse)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByGlobalId(UUID globalId){
        return clienteRepository.findByGlobalId(globalId)
                .map(clienteMapper::toClienteResponse)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente",globalId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> search(String term, ClienteStatus status, Pageable pageable){
        if (isEmployee()) {
            status = ClienteStatus.ACTIVO;
        }
        return PageResponse.from(
                clienteRepository.searchByTerm(term,status,pageable)
                .map(clienteMapper::toClienteResponse)
        );
    }

    @Override
    @Transactional
    @Auditable(operation = "CREATE_CLIENTE", entity = "clientes")
    public ClienteResponse create(CreateClienteRequest request) {
        if(request.cedula() !=null && !request.cedula().isBlank()){
            clienteRepository.findByCedula(request.cedula()).ifPresent(existing -> {
                throw new DuplicateResourceException("Ya existe un cliente con número de cédula " + request.cedula(), existing.getGlobalId().toString());
            });
        }
        if(request.phone() !=null && !request.phone().isBlank()){
            clienteRepository.findByPhone(request.phone()).ifPresent(existing -> {
                throw new DuplicateResourceException("Ya existe un cliente con número de teléfono " + request.phone(), existing.getGlobalId().toString());
            });
        }
        Cliente cliente = clienteMapper.toEntity(request);
        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado id={}, globalId={}, tipo={}", saved.getId(),
                saved.getGlobalId(), saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    @Override
    @Transactional
    @Auditable(operation = "UPDATE_CLIENTE", entity = "clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse update(UUID globalId, UpdateClienteRequest request) {
        Cliente cliente = findEntityOrThrow(globalId);
                if(request.cedula() !=null && !request.cedula().isBlank()){
                    clienteRepository.findByCedula(request.cedula()).ifPresent(existing -> {
                        if (!existing.getId().equals(cliente.getId())) {
                            throw new DuplicateResourceException("Ya existe un cliente con la cédula " + request.cedula(), existing.getGlobalId().toString());
                        }
                    });
                }
                if(request.phone() != null && !request.phone().isBlank()){
                    clienteRepository.findByPhone(request.phone()).ifPresent(existing -> {
                        if (!existing.getId().equals(cliente.getId())) {
                            throw new DuplicateResourceException("Ya existe un cliente con el teléfono " + request.phone(), existing.getGlobalId().toString());
                        }
                    });
                }
                clienteMapper.applyUpdates(cliente, request);
                Cliente saved = clienteRepository.save(cliente);
                log.info("Cliente actulizaod: globalId={},tipo={}", globalId, saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    private Cliente findEntityOrThrow(UUID globalId) {
        return clienteRepository.findByGlobalId(globalId)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente",globalId));
    }

    @Override
    @Transactional
    @Auditable(operation = "DELETE_CLIENTE", entity = "clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID globalId) {
        Cliente cliente = findEntityOrThrow(globalId);
        cliente.setStatus(ClienteStatus.ELIMINADO);
        clienteRepository.save(cliente);
        log.info("Cliente eliminado: globalId={}", globalId);

    }

    @Override
    @Transactional
    @Auditable(operation = "HARD_DELETE_CLIENTE", entity = "clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDelete(UUID globalId) {
        Cliente cliente = findEntityOrThrow(globalId);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
            log.info("Cliente eliminado físicamente: globalId={}", globalId);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("El cliente tiene operaciones asociadas y no puede ser eliminado.");
        }
    }

}
