package com.CompraVenta.Backend.Sync.service;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Sync.Dto.SyncRunResponse;
import com.CompraVenta.Backend.Sync.Repository.SyncOutboxRepository;
import com.CompraVenta.Backend.Sync.SyncOutbox;
import com.CompraVenta.Backend.Sync.SyncStatus;
import com.CompraVenta.Backend.Sync.client.SupabaseSyncClient;
import com.CompraVenta.Backend.Sync.client.SyncConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncEngineService — Tests Unitarios")
class SyncEngineServiceTest {

    @Mock
    private SyncOutboxRepository syncOutboxRepository;

    @Mock
    private SupabaseSyncClient supabaseSyncClient;

    @InjectMocks
    private SyncEngineService syncEngineService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(syncEngineService, "enabled", true);
        ReflectionTestUtils.setField(syncEngineService, "batchSize", 100);
        ReflectionTestUtils.setField(syncEngineService, "maxRetries", 5);
    }

    @Test
    @DisplayName("triggerManual falla si sync está deshabilitado")
    void triggerManual_whenDisabled_throws() {
        ReflectionTestUtils.setField(syncEngineService, "enabled", false);

        assertThatThrownBy(() -> syncEngineService.triggerManual())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("deshabilitado");
    }

    @Test
    @DisplayName("runCycle no toca el outbox si Supabase no está configurado")
    void runCycle_whenRemoteNotConfigured_skips() {
        when(supabaseSyncClient.isConfigured()).thenReturn(false);

        SyncRunResponse response = syncEngineService.runCycle();

        assertThat(response.processed()).isZero();
        verify(syncOutboxRepository, never()).findByStatusOrderByCreatedAtAsc(any(), any());
    }

    @Test
    @DisplayName("runCycle marca SYNCED cuando el push remoto funciona")
    void runCycle_whenPushSucceeds_marksSynced() {
        when(supabaseSyncClient.isConfigured()).thenReturn(true);
        SyncOutbox item = SyncOutbox.builder()
                .id(1L)
                .entityType("articles")
                .entityId(UUID.randomUUID())
                .operation("INSERT")
                .payload("{}")
                .status(SyncStatus.PENDING)
                .retryCount(0)
                .build();
        when(syncOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(SyncStatus.PENDING), any(Pageable.class)))
                .thenReturn(List.of(item));

        SyncRunResponse response = syncEngineService.runCycle();

        assertThat(response.processed()).isEqualTo(1);
        assertThat(response.ok()).isEqualTo(1);
        assertThat(item.getStatus()).isEqualTo(SyncStatus.SYNCED);
        assertThat(item.getSyncedAt()).isNotNull();
        verify(supabaseSyncClient).push(item);
    }

    @Test
    @DisplayName("runCycle marca CONFLICT cuando Supabase responde 409")
    void runCycle_whenConflict_marksConflict() {
        when(supabaseSyncClient.isConfigured()).thenReturn(true);
        SyncOutbox item = SyncOutbox.builder()
                .id(2L)
                .entityType("clientes")
                .entityId(UUID.randomUUID())
                .operation("INSERT")
                .payload("{}")
                .status(SyncStatus.PENDING)
                .retryCount(0)
                .build();
        when(syncOutboxRepository.findByStatusOrderByCreatedAtAsc(eq(SyncStatus.PENDING), any(Pageable.class)))
                .thenReturn(List.of(item));
        org.mockito.Mockito.doThrow(new SyncConflictException("duplicado"))
                .when(supabaseSyncClient).push(item);

        SyncRunResponse response = syncEngineService.runCycle();

        assertThat(response.errors()).isEqualTo(1);
        assertThat(item.getStatus()).isEqualTo(SyncStatus.CONFLICT);
    }
}
