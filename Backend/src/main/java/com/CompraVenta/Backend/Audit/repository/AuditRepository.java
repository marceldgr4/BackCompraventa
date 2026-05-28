package com.CompraVenta.Backend.Audit.repository;

import com.CompraVenta.Backend.Audit.entity.AudLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AudLog, Long> {
    List<AudLog> findByEmployeeIdOrderByTimestampDesc(String employeeId);
    @Query("SELECT a FROM AudLog a WHERE a.timestamp BETWEEN :from AND :to")
    Page<AudLog>findByDateRange(
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

}
