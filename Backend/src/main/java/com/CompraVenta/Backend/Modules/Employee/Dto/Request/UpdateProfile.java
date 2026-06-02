package com.CompraVenta.Backend.Modules.Employee.Dto.Request;

import jakarta.validation.constraints.Size;

public record UpdateProfile(
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        String fullName,

        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String newPassword,

        @Size(min = 8, message = "La confirmación debe tener al menos 8 caracteres")
        String confirmPassword) {
}
