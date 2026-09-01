package com.CompraVenta.Backend.Modules.Purchases.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Enums.RegistrationType;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.CreatePurchaseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteResolutionService {

    private final ClienteRepository clienteRepository;

    public Cliente resolveOrCreate(CreatePurchaseRequest request) {
        if (request.clienteGlobalId() != null) {
            return findExistingCliente(request.clienteGlobalId());
        }
        if (request.clienteFirstName() != null && !request.clienteFirstName().isBlank()) {
            return createQuickCliente(request);
        }
        return null;
    }

    private Cliente findExistingCliente(UUID clienteGlobalId) {
        Cliente cliente = clienteRepository.findByGlobalId(clienteGlobalId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteGlobalId));

        if (!cliente.isActive()) {
            throw new BusinessException("El cliente no existe o no está activo");
        }
        return cliente;
    }

    private Cliente createQuickCliente(CreatePurchaseRequest request) {
        String cedula = request.clienteCedula() != null ? request.clienteCedula().trim() : null;
        if (cedula != null && !cedula.isBlank()) {
            return clienteRepository.findByCedula(cedula)
                    .map(existing -> {
                        if (!existing.isActive()) {
                            throw new BusinessException("El cliente no existe o no está activo");
                        }
                        return existing;
                    })
                    .orElseGet(() -> saveQuickCliente(request, cedula));
        }
        return saveQuickCliente(request, null);
    }

    private Cliente saveQuickCliente(CreatePurchaseRequest request, String cedula) {
        Cliente cliente = Cliente.builder()
                .firstName(request.clienteFirstName().trim())
                .cedula(cedula)
                .phone(request.clientePhone())
                .registrationType(RegistrationType.RAPIDO)
                .status(ClienteStatus.ACTIVO)
                .build();
        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente rápido creado para compra: id={}, nombre={}", saved.getId(), saved.getFirstName());
        return saved;
    }

    public Long resolveClienteIdOrNull(UUID clienteGlobalId) {
        if (clienteGlobalId == null) {
            return null;
        }
        return clienteRepository.findByGlobalId(clienteGlobalId)
                .map(Cliente::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteGlobalId));
    }
}
