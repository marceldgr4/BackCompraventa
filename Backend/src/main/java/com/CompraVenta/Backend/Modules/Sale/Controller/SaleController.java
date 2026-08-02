package com.CompraVenta.Backend.Modules.Sale.Controller;

import com.CompraVenta.Backend.Modules.Sale.Dto.Request.CreateSaleRequest;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleResponse;
import com.CompraVenta.Backend.Modules.Sale.Service.SaleService;
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
import java.util.UUID;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Validated
@Tag(name = "Sale", description = "gestion de venta")
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @Operation(summary = "Lista ventas (admin ve todos, empleado solo las propias)")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> findAll(
            @RequestParam(required = false)UUID clienteGlobalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate dateFrom,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size
            ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                saleService.findAll(clienteGlobalId, dateFrom, dateTo, pageable)
        ));
    }
    @GetMapping("/{globalId}")
    @Operation(summary = "detalle de una venta")
    public ResponseEntity<ApiResponse<SaleResponse>> findByGlobalId(
            @PathVariable UUID globalId
    ){
        return ResponseEntity.ok(ApiResponse.ok(saleService.findByGlobalId(globalId)));

    }

    @PostMapping
    @Operation(summary = "registar nueva venta")
    public ResponseEntity<ApiResponse<SaleResponse>> create(
            @RequestBody
            @Valid CreateSaleRequest request
    ){
        SaleResponse created = saleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{globalId}")
                .buildAndExpand(created.globalId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));
    }
    @DeleteMapping("/{globalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "eliminar venta (solo administador)")
    public ResponseEntity<Void> delete(@PathVariable UUID globalId){
        saleService.delete(globalId);
        return ResponseEntity.noContent().build();
    }
}
