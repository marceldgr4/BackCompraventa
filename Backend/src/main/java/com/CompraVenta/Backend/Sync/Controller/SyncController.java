package com.CompraVenta.Backend.Sync.Controller;

import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Sync.Dto.SyncRunResponse;
import com.CompraVenta.Backend.Sync.Dto.SyncStatusResponse;
import com.CompraVenta.Backend.Sync.service.SyncEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Motor de sincronización con Supabase")
public class SyncController {

    private final SyncEngineService syncEngineService;

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estado del outbox de sincronización")
    public ResponseEntity<ApiResponse<SyncStatusResponse>> status() {
        return ResponseEntity.ok(ApiResponse.ok(syncEngineService.status()));
    }

    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disparar un ciclo de sincronización manual")
    public ResponseEntity<ApiResponse<SyncRunResponse>> trigger() {
        return ResponseEntity.ok(ApiResponse.ok(syncEngineService.triggerManual(), "Ciclo de sincronización ejecutado"));
    }
}
