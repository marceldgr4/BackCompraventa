package com.CompraVenta.Backend.Modules.Dashboard.Repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardRepository extends org.springframework.data.repository.Repository<Object, Long> {

    @Query(value = "SELECT COALESCE(SUM(sd.amount * sd.unit_price), 0) FROM sales_details sd " +
                   "JOIN sales s ON sd.sale_id = s.id " +
                   "WHERE s.sale_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    BigDecimal getTotalRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT status as label, COUNT(*) as value FROM pawns GROUP BY status", nativeQuery = true)
    List<Object[]> getPawnDistribution();

    @Query(value = "SELECT name_article as articleName, amount as currentStock FROM articles WHERE amount < 5", nativeQuery = true)
    List<Object[]> getLowStockArticles();
}
