package com.CompraVenta.Backend.Sync.Repository;

import com.CompraVenta.Backend.Sync.SyncOutbox;
import com.CompraVenta.Backend.Sync.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncOutboxRepository extends JpaRepository<SyncOutbox, Long> {

    List<SyncOutbox> findByStatusOrderByCreatedAtAsc(SyncStatus status, Pageable pageable);

    long countByStatus(SyncStatus status);
}
