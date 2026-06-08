package com.CompraVenta.Backend.Modules.Employee.Dto.Response;

import com.CompraVenta.Backend.Shared.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String email,
        String fullName,
        Role rol,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {}
