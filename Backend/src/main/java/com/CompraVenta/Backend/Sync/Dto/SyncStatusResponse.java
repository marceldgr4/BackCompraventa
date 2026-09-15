package com.CompraVenta.Backend.Sync.Dto;

public record SyncStatusResponse(
        boolean enabled,
        boolean remoteConfigured,
        long pending,
        long syncing,
        long synced,
        long failed,
        long conflict
) {
}
