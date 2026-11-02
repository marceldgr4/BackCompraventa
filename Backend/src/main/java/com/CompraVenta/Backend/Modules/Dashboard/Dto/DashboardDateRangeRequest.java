package com.CompraVenta.Backend.Modules.Dashboard.Dto;

import java.time.LocalDate;

public record DashboardDateRangeRequest(LocalDate startDate, LocalDate endDate) {}
