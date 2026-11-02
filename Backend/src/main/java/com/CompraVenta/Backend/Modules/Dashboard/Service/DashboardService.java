package com.CompraVenta.Backend.Modules.Dashboard.Service;

import com.CompraVenta.Backend.Modules.Dashboard.Dto.*;
import java.util.Optional;

public interface DashboardService {
    DashboardSummaryResponse getSummary(Optional<DashboardDateRangeRequest> dateRange);
    SystemHealthResponse getSystemHealth();
}
