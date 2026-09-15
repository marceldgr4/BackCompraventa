package com.CompraVenta.Backend.Sync.Dto;

public record SyncRunResponse(
        int processed,
        int ok,
        int errors,
        int skipped
) {
}
