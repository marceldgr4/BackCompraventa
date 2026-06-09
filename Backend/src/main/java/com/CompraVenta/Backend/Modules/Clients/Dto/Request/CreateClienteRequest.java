package com.CompraVenta.Backend.Modules.Clients.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClienteRequest(
        @Size(max =10, message = "la cedula no puede supera 10 caracters")
        String cedula,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max= 20, message = "el nombre no puede supera 20 caractres")
        String firstName,

        @Size(max = 20, message = "el apellido no puede suerpar 20 caractres")
        String lastName,

        @Email(message = "el correo no tiene formato valido")
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^[+\\d\\s\\-()]{0,20}$", message = "El teléfono no tiene un formato válido")
        String phone,

        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String address,

        @Size(max = 50, message = "La ciudad no puede superar 50 caracteres")
        String city

) {}
