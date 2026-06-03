package com.CompraVenta.Backend.Modules.auth.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email es Obligatorio")
        @Email(message = "Formato del email invalido")
        String email,

        @NotBlank(message = "La contraseña es obligatorio")
        String password
) {
}
