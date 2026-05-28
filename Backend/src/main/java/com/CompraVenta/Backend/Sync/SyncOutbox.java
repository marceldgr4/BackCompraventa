package com.CompraVenta.Backend.Sync;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_outbox",schema = "public",
indexes = {
        @Index(name = "idx_sync_outbox_status", columnList = "status"),
        @Index(name = "idx_sync_outbox_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SyncOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false,length = 50)
    private String entityType;

    @Column(name = "entity_id",nullable = false)
    private UUID entityId;

    @Column(name = "operation",nullable = false, length = 10)
    private String operation;

    @Column(name = "payload",nullable = false,columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(name = "local_version",nullable = false)
    @Builder.Default
    private long localVersion = 1L;

    @Column(name = "cloud_version")
    private long cloudVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 20)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    @Column(name = "retry_count",nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "error_message",columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at",nullable = false,updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "synced_at")
    private Instant syncedAt;


}
