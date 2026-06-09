package com.CompraVenta.Backend.Modules.Clients.Dto.Response;

import com.CompraVenta.Backend.Modules.Clients.Emus.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Emus.RegistrationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
        Long id,
        UUID GlobalId,
        String cedula,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        ClienteStatus status,
        RegistrationType registrationType,
        LocalDateTime createAt,
        LocalDateTime updateAt

) {
}
