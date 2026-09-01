package com.CompraVenta.Backend.Modules.Purchases.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequest(
        UUID clienteGlobalId,
        @Size(max = 100, message = "el nombre no puede superar 100 caracteres")
        String clienteFirstName,

        @Size(max = 20, message = "La identificación no puede superar 20 caracteres")
        String clienteCedula,

        @Pattern(regexp = "^[+\\d\\s\\-()]{0,20}$", message = "El teléfono no tiene un formato válido")
        String clientePhone,

        @NotEmpty(message = "La compra debe tener al menos un artículo")
        @Valid
        List<PurchaseItemRequest> items,

        String notes
) {
}
