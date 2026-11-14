package com.CompraVenta.Backend.Modules.Dashboard.Controller;

import com.CompraVenta.Backend.Modules.Dashboard.Dto.*;
import com.CompraVenta.Backend.Modules.Dashboard.Service.DashboardService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name= "dashboard",description = "metricas y KPIs operativos")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @ModelAttribute Optional<DashboardDateRangeRequest> dateRange) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummary(dateRange)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getHealth() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSystemHealth()));
    }
}
