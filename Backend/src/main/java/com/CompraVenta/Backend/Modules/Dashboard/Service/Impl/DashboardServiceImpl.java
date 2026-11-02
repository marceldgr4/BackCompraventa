package com.CompraVenta.Backend.Modules.Dashboard.Service.Impl;

import com.CompraVenta.Backend.Modules.Dashboard.Dto.*;
import com.CompraVenta.Backend.Modules.Dashboard.Repository.DashboardRepository;
import com.CompraVenta.Backend.Modules.Dashboard.Service.DashboardService;
import com.CompraVenta.Backend.Sync.Repository.SyncOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final SyncOutboxRepository syncOutboxRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Optional<DashboardDateRangeRequest> dateRange) {
        LocalDate start = dateRange.map(DashboardDateRangeRequest::getStartDate).orElse(LocalDate.now().minusMonths(1));
        LocalDate end = dateRange.map(DashboardDateRangeRequest::getEndDate).orElse(LocalDate.now());

        BigDecimal revenue = dashboardRepository.getTotalRevenue(start, end);

        List<KpiMetricDto> distribution = dashboardRepository.getPawnDistribution().stream()
                .map(obj -> new KpiMetricDto((String) obj[0], (Long) obj[1]))
                .toList();

        List<InventoryAlertDto> alerts = dashboardRepository.getLowStockArticles().stream()
                .map(obj -> new InventoryAlertDto((String) obj[0], (Integer) obj[1]))
                .toList();

        return new DashboardSummaryResponse(
                revenue != null ? revenue : BigDecimal.ZERO,
                distribution,
                alerts
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        long pendingSyncs = syncOutboxRepository.countByStatus(com.CompraVenta.Backend.Sync.SyncStatus.PENDING);

        return new SystemHealthResponse(
                pendingSyncs,
                true,
                "OK"
        );
    }
}
