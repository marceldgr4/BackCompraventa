package com.CompraVenta.Backend.Sync.service;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Sync.Dto.SyncRunResponse;
import com.CompraVenta.Backend.Sync.Dto.SyncStatusResponse;
import com.CompraVenta.Backend.Sync.Repository.SyncOutboxRepository;
import com.CompraVenta.Backend.Sync.SyncOutbox;
import com.CompraVenta.Backend.Sync.SyncStatus;
import com.CompraVenta.Backend.Sync.client.SupabaseSyncClient;
import com.CompraVenta.Backend.Sync.client.SyncConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncEngineService {

    private final SyncOutboxRepository syncOutboxRepository;
    private final SupabaseSyncClient supabaseSyncClient;

    @Value("${sync.enabled:false}")
    private boolean enabled;

    @Value("${sync.schedule.batch-size:100}")
    private int batchSize;

    @Value("${sync.schedule.max-retries:5}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${sync.schedule.interval-ms:30000}")
    public void scheduledSyncCycle() {
        if (!enabled) {
            return;
        }
        runCycle();
    }

    public SyncRunResponse triggerManual() {
        if (!enabled) {
            throw new BusinessException("El motor de sincronización está deshabilitado (sync.enabled=false)");
        }
        return runCycle();
    }

    @Transactional
    public SyncRunResponse runCycle() {
        if (!supabaseSyncClient.isConfigured()) {
            log.warn("Sync habilitado pero Supabase no está configurado. Se omiten registros PENDING.");
            return new SyncRunResponse(0, 0, 0, 0);
        }

        List<SyncOutbox> pending = syncOutboxRepository.findByStatusOrderByCreatedAtAsc(
                SyncStatus.PENDING, PageRequest.of(0, Math.max(batchSize, 1))
        );

        int ok = 0;
        int errors = 0;
        int skipped = 0;

        for (SyncOutbox item : pending) {
            if (item.getRetryCount() >= maxRetries) {
                item.setStatus(SyncStatus.FAILED);
                item.setErrorMessage("Se alcanzó el máximo de reintentos (" + maxRetries + ")");
                syncOutboxRepository.save(item);
                skipped++;
                continue;
            }

            item.setStatus(SyncStatus.SYNCING);
            syncOutboxRepository.saveAndFlush(item);

            try {
                supabaseSyncClient.push(item);
                item.setStatus(SyncStatus.SYNCED);
                item.setSyncedAt(Instant.now());
                item.setErrorMessage(null);
                ok++;
            } catch (SyncConflictException conflict) {
                item.setStatus(SyncStatus.CONFLICT);
                item.setErrorMessage(conflict.getMessage());
                errors++;
                log.warn("Conflicto de sync {} {}: {}", item.getEntityType(), item.getEntityId(), conflict.getMessage());
            } catch (Exception ex) {
                item.setRetryCount(item.getRetryCount() + 1);
                item.setErrorMessage(ex.getMessage());
                item.setStatus(item.getRetryCount() >= maxRetries ? SyncStatus.FAILED : SyncStatus.PENDING);
                errors++;
                log.error("Fallo sync {} {}: {}", item.getEntityType(), item.getEntityId(), ex.getMessage());
            }
            syncOutboxRepository.save(item);
        }

        if (!pending.isEmpty()) {
            log.info("Ciclo sync: procesados={}, ok={}, errores={}, omitidos={}", pending.size(), ok, errors, skipped);
        }
        return new SyncRunResponse(pending.size(), ok, errors, skipped);
    }

    @Transactional(readOnly = true)
    public SyncStatusResponse status() {
        return new SyncStatusResponse(
                enabled,
                supabaseSyncClient.isConfigured(),
                syncOutboxRepository.countByStatus(SyncStatus.PENDING),
                syncOutboxRepository.countByStatus(SyncStatus.SYNCING),
                syncOutboxRepository.countByStatus(SyncStatus.SYNCED),
                syncOutboxRepository.countByStatus(SyncStatus.FAILED),
                syncOutboxRepository.countByStatus(SyncStatus.CONFLICT)
        );
    }
}
