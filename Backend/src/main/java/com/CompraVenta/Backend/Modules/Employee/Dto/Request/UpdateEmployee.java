package com.CompraVenta.Backend.Modules.Employee.Dto.Request;

import com.CompraVenta.Backend.Shared.enums.Role;
import jakarta.validation.constraints.Size;

public record UpdateEmployee(
        @Size(min = 2, max = 255,
        message = "El nombre debe tener entre 2 y 255 caracteres")
        String fullName,
        Role rol
) {
}
