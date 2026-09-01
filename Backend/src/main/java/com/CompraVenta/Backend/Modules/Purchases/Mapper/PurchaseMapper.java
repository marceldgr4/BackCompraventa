package com.CompraVenta.Backend.Modules.Purchases.Mapper;

import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Response.PurchaseResponse;
import com.CompraVenta.Backend.Modules.Purchases.Entity.Purchase;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {

    public PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getGlobalId(),
                purchase.getEmployee() != null ? purchase.getEmployee().getGlobalId() : null,
                purchase.getEmployee() != null ? purchase.getEmployee().getFullName() : null,
                purchase.getCliente() != null ? purchase.getCliente().getGlobalId() : null,
                purchase.getCliente() != null ? buildClienteFullName(purchase.getCliente()) : null,
                purchase.getArticle() != null ? purchase.getArticle().getGlobalId() : null,
                purchase.getArticle() != null ? purchase.getArticle().getNameArticle() : null,
                purchase.getPurchasePrice(),
                purchase.getPurchaseDate(),
                purchase.getNotes(),
                purchase.getCreatedAt()
        );
    }

    private String buildClienteFullName(Cliente cliente) {
        String lastName = cliente.getLastName();
        return (lastName != null && !lastName.isBlank())
                ? cliente.getFirstName() + " " + lastName
                : cliente.getFirstName();
    }
}
