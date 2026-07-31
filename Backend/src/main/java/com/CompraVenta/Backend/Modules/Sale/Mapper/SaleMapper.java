package com.CompraVenta.Backend.Modules.Sale.Mapper;

import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleDetailResponse;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleResponse;
import com.CompraVenta.Backend.Modules.Sale.Entity.Sale;
import com.CompraVenta.Backend.Modules.Sale.Entity.SaleDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale sale, List<SaleDetails>details){
        List<SaleDetailResponse> detailResponses = details.stream()
                .map(this::toDetailResponse)
                .toList();

        BigDecimal total = SaleDetailResponse.stream()
                .map(SaleDetailResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal:: add);

        return new SaleResponse(
                sale.getId(),
                sale.getGlobalId(),
                sale.getEmployeeId() !=null ? sale.getEmployee().getGlobalId():null,
                sale.getEmployee() != null ? sale.getEmployee().getFullName(): null,
                sale.getCliente() != null ? sale.getCliente().getGlobalId(): null,
                sale.getCliente() !=null ? buildClienteFullName(sale.getCliente()) : null,
                sale.getClienteNombreAnon(),
                sale.getSaleDate(),
                sale.getNotes(),
                total,
                detailResponses,
                sale.getCreatedAt()
        );
    }

    private SaleDetailResponse toDetailResponse(SaleDetails details){
        return new SaleDetailResponse(
                details.getId(),
                details.getArticle() !=null ? details.getArticle().getGlobalId(): null,
                details.getArticle() !=null ? details.getArticle().getNameArticle(): null,
                details.getAmount(),
                details.getUnitPrice(),
                details.getSubTotal()
        );
    }

    private String buildClienteFullName(Cliente cliente){
        String lastName = cliente.getLastName();
        return (lastName !=null && !lastName.isBlank())
                ? cliente.getFirstName() +" "+ lastName: cliente.getFirstName();
    }

}
