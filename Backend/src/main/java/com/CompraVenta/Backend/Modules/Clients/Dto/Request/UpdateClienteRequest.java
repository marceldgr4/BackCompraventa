package com.CompraVenta.Backend.Modules.Clients.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateClienteRequest(
        @Size(max = 10)
        String cedula,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^[+\\d\\s\\-()]{0,20}$", message = "El teléfono no tiene un formato válido")
        String phone,

        @Size(max = 255)
        String address,

        @Size(max = 50)
        String city
) {
}
