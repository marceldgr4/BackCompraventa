package com.CompraVenta.Backend.Modules.Sale.Dto.Request;

import com.CompraVenta.Backend.Modules.Sale.Entity.Sale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateSaleRequest(
        UUID clienteGlobalId,
        @Size(max = 225, message = "el nombre no puede superar 225 caractres")
        String clienteNombreAnon,

        @NotEmpty(message = "la venta debe tener al menos  un articulo")
        @Valid
        List<SaleItemRequest> items,
        String notes
) {
}
