package com.CompraVenta.Backend.Modules.Clients.Service;

import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClienteService {
    PageResponse<ClienteResponse>findAll(ClienteStatus status, Pageable pageable);

    ClienteResponse findByGlobalId(UUID globalId);

    PageResponse<ClienteResponse> search(String term, ClienteStatus status, Pageable pageable);

    ClienteResponse create(CreateClienteRequest request);
    ClienteResponse update(UUID globalId, UpdateClienteRequest request);
    void delete(UUID globalId);

}
