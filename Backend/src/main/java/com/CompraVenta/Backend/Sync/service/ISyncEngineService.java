package com.CompraVenta.Backend.Sync.service;

import com.CompraVenta.Backend.Sync.Dto.SyncRunResponse;
import com.CompraVenta.Backend.Sync.Dto.SyncStatusResponse;

public interface ISyncEngineService {
    void scheduledSyncCycle();
    SyncRunResponse triggerManual();
    SyncRunResponse runCycle();
    SyncStatusResponse status();
}
