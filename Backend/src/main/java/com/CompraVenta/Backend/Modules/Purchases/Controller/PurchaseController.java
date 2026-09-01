package com.CompraVenta.Backend.Modules.Purchases.Controller;

import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.CreatePurchaseRequest;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Response.PurchaseResponse;
import com.CompraVenta.Backend.Modules.Purchases.Service.PurchaseService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@Validated
@Tag(name = "Purchases", description = "Gestión de compras a proveedores")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    @Operation(summary = "Lista compras (admin ve todas, empleado solo las propias)")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseResponse>>> findAll(
            @RequestParam(required = false) UUID clienteGlobalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                purchaseService.findAll(clienteGlobalId, dateFrom, dateTo, pageable)
        ));
    }

    @GetMapping("/{globalId}")
    @Operation(summary = "Detalle de una compra")
    public ResponseEntity<ApiResponse<PurchaseResponse>> findByGlobalId(
            @PathVariable UUID globalId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.findByGlobalId(globalId)));
    }

    @PostMapping
    @Operation(summary = "Registrar nueva compra (crea artículos en inventario)")
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> create(
            @RequestBody @Valid CreatePurchaseRequest request
    ) {
        List<PurchaseResponse> created = purchaseService.create(request);
        UUID firstGlobalId = created.isEmpty() ? null : created.getFirst().globalId();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{globalId}")
                .buildAndExpand(firstGlobalId)
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));
    }

    @DeleteMapping("/{globalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Anular compra (solo administrador)")
    public ResponseEntity<Void> delete(@PathVariable UUID globalId) {
        purchaseService.delete(globalId);
        return ResponseEntity.noContent().build();
    }
}
