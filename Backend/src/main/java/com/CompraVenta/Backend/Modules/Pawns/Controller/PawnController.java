package com.CompraVenta.Backend.Modules.Pawns.Controller;

import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreateAgilePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreatePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.PawnPaymentRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnPaymentResponse;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnResponse;
import com.CompraVenta.Backend.Modules.Pawns.Enums.PawnStatus;
import com.CompraVenta.Backend.Modules.Pawns.Service.PawnService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/pawns")
@RequiredArgsConstructor
@Tag(name = "Pawns", description = "Endpoints para la gestión de empeños")
public class PawnController {

    private final PawnService pawnService;

    @GetMapping
    @Operation(summary = "Listar empeños con filtros y paginación")
    public ResponseEntity<ApiResponse<PageResponse<PawnResponse>>> findAll(
            @RequestParam(required = false) PawnStatus status,
            @RequestParam(required = false) UUID clienteGlobalId,
            @RequestParam(required = false) UUID employeeGlobalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable
    ) {
        Page<PawnResponse> page = pawnService.findAll(status, clienteGlobalId, employeeGlobalId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Listado de empeños"));
    }

    @GetMapping("/{globalId}")
    @Operation(summary = "Obtener detalle de un empeño por ID")
    public ResponseEntity<ApiResponse<PawnResponse>> findByGlobalId(@PathVariable UUID globalId) {
        PawnResponse response = pawnService.findByGlobalId(globalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Detalle del empeño obtenido"));
    }

    @PostMapping
    @Operation(summary = "Crear un empeño normal")
    public ResponseEntity<ApiResponse<PawnResponse>> create(@Valid @RequestBody CreatePawnRequest request) {
        PawnResponse response = pawnService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Empeño creado exitosamente"));
    }

    @PostMapping("/agile")
    @Operation(summary = "Crear un empeño ágil (cliente, artículo y empeño)")
    public ResponseEntity<ApiResponse<PawnResponse>> createAgile(@Valid @RequestBody CreateAgilePawnRequest request) {
        PawnResponse response = pawnService.createAgile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Empeño ágil creado exitosamente"));
    }

    @PostMapping("/{globalId}/payments")
    @Operation(summary = "Registrar pago de cuota")
    public ResponseEntity<ApiResponse<PawnPaymentResponse>> registerPayment(
            @PathVariable UUID globalId,
            @Valid @RequestBody PawnPaymentRequest request) {
        PawnPaymentResponse response = pawnService.registerPayment(globalId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Pago registrado exitosamente"));
    }

    @PostMapping("/{globalId}/missed-installments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar cuota impagada manualmente (solo ADMIN)")
    public ResponseEntity<ApiResponse<PawnPaymentResponse>> registerMissedInstallment(@PathVariable UUID globalId) {
        PawnPaymentResponse response = pawnService.registerMissedInstallment(globalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cuota impagada registrada"));
    }

    @GetMapping("/{globalId}/payments")
    @Operation(summary = "Listar pagos de un empeño")
    public ResponseEntity<ApiResponse<PageResponse<PawnPaymentResponse>>> findPayments(
            @PathVariable UUID globalId,
            Pageable pageable) {
        PageResponse<PawnPaymentResponse> response = pawnService.findPayments(globalId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Listado de pagos del empeño"));
    }

    @PatchMapping("/{globalId}/return")
    @Operation(summary = "Marcar un empeño como devuelto / retirado")
    public ResponseEntity<ApiResponse<PawnResponse>> markAsReturned(@PathVariable UUID globalId) {
        PawnResponse response = pawnService.markAsReturned(globalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Empeño marcado como devuelto"));
    }

    @PatchMapping("/{globalId}/lost")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar un empeño como perdido manualmente (solo ADMIN)")
    public ResponseEntity<ApiResponse<PawnResponse>> markAsLost(@PathVariable UUID globalId) {
        PawnResponse response = pawnService.markAsLost(globalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Empeño marcado como perdido"));
    }
}
