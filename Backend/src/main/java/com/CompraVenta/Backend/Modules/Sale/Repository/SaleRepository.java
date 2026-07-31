package com.CompraVenta.Backend.Modules.Sale.Repository;

import com.CompraVenta.Backend.Modules.Sale.Entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByGlobalId(UUID globalId);

    @Query("""
            SELECT s FROM Sale s
           WHERE (:employeeId IS NULL OR s.employeeId = :employeeId)
           AND (:clienteId IS NULL OR s.clienteId = :clienteId)
           AND (:dateFrom IS NULL OR s.saleDate >= :dateFrom)
           AND (:dateTo IS NULL OR s.saleDate <= :dateTo)
           ORDER BY s.saleDate DESC
           """)
    Page<Sale> findByFilters(
            @Param("employeeId") Long employeeId,
            @Param("clienteId") Long clienteId,
            @Param("dateFrom")LocalDateTime dateFrom,
            @Param("dataTo")LocalDateTime dateTo,
            Pageable pageable
            );

}
