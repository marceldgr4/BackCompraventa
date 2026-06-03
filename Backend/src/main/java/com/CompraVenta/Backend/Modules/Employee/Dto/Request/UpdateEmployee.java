package com.CompraVenta.Backend.Modules.Employee.Dto.Request;

import com.CompraVenta.Backend.Shared.enums.Role;
import jakarta.validation.constraints.Size;

public record UpdateEmployee(
        @Size(min = 10, max = 255,
        message = "El nombre debe tener entre 10 y 255 caractres")
        String fullName,
        Role rol
) {
}
