package com.CompraVenta.Backend.Modules.Sale.Repository;

import com.CompraVenta.Backend.Modules.Sale.Entity.SaleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetails, Long> {
    List<SaleDetails> findBySaleIdOrderById(Long saleId);
}
