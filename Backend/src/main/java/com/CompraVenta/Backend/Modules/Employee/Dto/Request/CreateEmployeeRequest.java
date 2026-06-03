package com.CompraVenta.Backend.Modules.Employee.Dto.Request;

import com.CompraVenta.Backend.Shared.enums.Role;
import jakarta.validation.constraints.*;


public record CreateEmployeeRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formto del email no es valido")
        @Size(max = 255, message = "El eamil no puede superar 255 caractres")
        String email,

         @NotBlank(message = "El nombre completo es obligatorio")
         @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
                 String fullName,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Role rol
) {}
