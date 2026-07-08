package com.CompraVenta.Backend.Modules.Pawns.Repository;

import com.CompraVenta.Backend.Modules.Pawns.Entity.PawnPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PawnPaymentRepository extends JpaRepository<PawnPayment, Long> {
    Page<PawnPayment> findByPawnIdOrderByPaymentDateDesc(Long pawnId, Pageable pageable);
}
