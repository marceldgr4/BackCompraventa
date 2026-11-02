package com.CompraVenta.Backend.Modules.Dashboard.Dto;

public record SystemHealthResponse(
    long pendingSyncs,
    boolean databaseUp,
    String status
) {}
