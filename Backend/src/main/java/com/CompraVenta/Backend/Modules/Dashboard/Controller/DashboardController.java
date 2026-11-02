package com.CompraVenta.Backend.Modules.Dashboard.Controller;

import com.CompraVenta.Backend.Modules.Dashboard.Dto.*;
import com.CompraVenta.Backend.Modules.Dashboard.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @ModelAttribute Optional<DashboardDateRangeRequest> dateRange) {
        return ResponseEntity.ok(dashboardService.getSummary(dateRange));
    }

    @GetMapping("/health")
    public ResponseEntity<SystemHealthResponse> getHealth() {
        return ResponseEntity.ok(dashboardService.getSystemHealth());
    }
}
