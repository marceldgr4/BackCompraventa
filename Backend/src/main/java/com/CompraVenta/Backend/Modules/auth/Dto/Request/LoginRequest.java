package com.CompraVenta.Backend.Modules.auth.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato del email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
