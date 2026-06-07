package com.CompraVenta.Backend.Modules.auth.Dto.Response;

import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Shared.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        EmployeeInfo employee,
        String modo
) {
    public record EmployeeInfo(
            UUID id,
            String email,
            String fullName,
            Role role
    ){}
}
