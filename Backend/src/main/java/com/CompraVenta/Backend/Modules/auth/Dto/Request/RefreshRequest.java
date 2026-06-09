package com.CompraVenta.Backend.Modules.auth.Dto.Request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
