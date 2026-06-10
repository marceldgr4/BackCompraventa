package com.CompraVenta.Backend.Modules.Clients.Mapper;

import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Enums.RegistrationType;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;

import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public Cliente toEntity(CreateClienteRequest request) {
        RegistrationType type = hasLastName(request.lastName())
                ? RegistrationType.COMPLETO
                : RegistrationType.RAPIDO;

        return Cliente.builder()
                .cedula(request.cedula())
                .firstName(request.firstName().trim())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .registrationType(type)
                .build();
    }

    private boolean hasLastName(String lastName) {
        return lastName != null && !lastName.isBlank();
    }

    public ClienteResponse toClienteResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getGlobalId(),
                cliente.getCedula(),
                cliente.getFirstName(),
                cliente.getLastName(),
                cliente.getEmail(),
                cliente.getPhone(),
                cliente.getAddress(),
                cliente.getCity(),
                cliente.getStatus(),
                cliente.getRegistrationType(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }

    public void applyUpdates(Cliente cliente, UpdateClienteRequest request) {
        if (request.cedula() != null) cliente.setCedula(request.cedula());
        if (request.firstName() != null) cliente.setFirstName(request.firstName().trim());
        if (request.lastName() != null) cliente.setLastName(request.lastName());
        if (request.email() != null) cliente.setEmail(request.email());
        if (request.phone() != null) cliente.setPhone(request.phone());
        if (request.address() != null) cliente.setAddress(request.address());
        if (request.city() != null) cliente.setCity(request.city());
        cliente.promoteToComplete();

    }
}
