package com.CompraVenta.Backend.Modules.Dashboard.Dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
    BigDecimal totalRevenue,
    List<KpiMetricDto> pawnDistribution,
    List<InventoryAlertDto> lowStockAlerts
) {}
