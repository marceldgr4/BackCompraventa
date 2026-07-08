package com.CompraVenta.Backend.Modules.Pawns.Repository;

import com.CompraVenta.Backend.Modules.Pawns.Emuns.PawnStatus;
import com.CompraVenta.Backend.Modules.Pawns.Entity.Pawn;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PawnRepository extends JpaRepository<Pawn, Long> {
    Optional<Pawn> findByGlobalId(UUID globalId);
    boolean existsByGlobalId(UUID globalId);

    @Query("""
        SELECT p FROM Pawn p
        WHERE (:status IS NULL OR p.status = :status)
        AND (:employeeId IS NULL OR p.employeeId = :employeeId)
        AND (:clienteId IS NULL OR p.clienteId = :clienteId)
        AND (:dateFrom IS NULL OR p.pawnDate >= :dateFrom)
        AND (:dateTo IS NULL OR p.pawnDate <= :dateTo)
    """)
    Page<Pawn> findByFilters(
            @Param("status") PawnStatus status,
            @Param("employeeId") Long employeeId,
            @Param("clienteId") Long clienteId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    @Query(value = "SELECT public.fn_expire_overdue_pawns()", nativeQuery = true)
    int expireOverduePawns();
}
