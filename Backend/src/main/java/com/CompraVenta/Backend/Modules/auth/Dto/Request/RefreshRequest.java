package com.CompraVenta.Backend.Modules.auth.Dto.Request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "el refresh token es obligatio")
        String refreshToken
) {
}
