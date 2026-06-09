package com.CompraVenta.Backend.Modules.Clients.Controller;

import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Service.ClienteService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "gestion de clientes del negocio")
public class ClienteController {
    private final ClienteService clienteService;

    //Get
    @GetMapping
    @Operation(summary = "Listar cliente con paginacion y filtros por estado")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> findAll(
            @RequestParam(required = false)ClienteStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size,
            @RequestParam(defaultValue = "firstName") String sortBy
            ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(ApiResponse.ok(clienteService.findAll(status, pageable)));
    }
    @GetMapping("/{globalId}")
    @Operation(summary = "obtener cliente por UUID public")
    public ResponseEntity<ApiResponse<ClienteResponse>> findByGlobalId(
            @PathVariable UUID globalId
    ){
        return ResponseEntity.ok(ApiResponse.ok(clienteService.findByGlobalId(globalId)));
    }
    @GetMapping("/search")
    @Operation(summary = "bsucar cliente pro nombre , apellido, cedula, email")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> findBySearch(
            @RequestParam String term,
            @RequestParam(required = false) ClienteStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(20) int size
    ){
                Pageable pageable= PageRequest.of(page,size);
                return ResponseEntity.ok(ApiResponse.ok(clienteService.search(term,status, pageable)));

    }
    //POST
    @PostMapping
    @Operation(summary = "Registar nuevos cliente (completo o rapdio segun datos provisto")
    public ResponseEntity<ApiResponse<ClienteResponse>> create(
            @RequestBody
            @Valid CreateClienteRequest request
    ){
        ClienteResponse created = clienteService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{globalId}")
                .buildAndExpand(created.globalId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));

    }
    //PUT
    @PutMapping("/{globalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "actulizar datos de un cliente")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable UUID globalId,
            @RequestBody @Valid UpdateClienteRequest request
    ){
        return ResponseEntity.ok(ApiResponse.ok(clienteService.update(globalId, request)));
    }

    //DELETE
    @DeleteMapping("/{globalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "eliminacion logica del cliente o cambiar el estado ")
    public ResponseEntity<Void> delete(
            @PathVariable UUID globalId
    ){
        clienteService.delete(globalId);
        return ResponseEntity.noContent().build();
    }

}
