package com.CompraVenta.Backend.Modules.Purchases.Repository;

import com.CompraVenta.Backend.Modules.Purchases.Entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @EntityGraph(attributePaths = {"employee", "cliente", "article"})
    Optional<Purchase> findByGlobalId(UUID globalId);

    @EntityGraph(attributePaths = {"employee", "cliente", "article"})
    @Query("""
            SELECT p FROM Purchase p
            WHERE (:employeeId IS NULL OR p.employeeId = :employeeId)
            AND (:clienteId IS NULL OR p.clienteId = :clienteId)
            AND (:dateFrom IS NULL OR p.purchaseDate >= :dateFrom)
            AND (:dateTo IS NULL OR p.purchaseDate <= :dateTo)
            ORDER BY p.purchaseDate DESC
            """)
    Page<Purchase> findByFilters(
            @Param("employeeId") Long employeeId,
            @Param("clienteId") Long clienteId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
