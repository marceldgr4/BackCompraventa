package com.CompraVenta.Backend.Modules.Pawns.Mapper;

import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreatePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.PawnPaymentRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnPaymentResponse;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnResponse;
import com.CompraVenta.Backend.Modules.Pawns.Entity.Pawn;
import com.CompraVenta.Backend.Modules.Pawns.Entity.PawnPayment;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class PawnMapper {
    public Pawn toEntity(CreatePawnRequest request, Article article, Cliente cliente, Employee employee) {
        return Pawn.builder()
                .articleId(article.getId())
                .clienteId(cliente.getId())
                .employeeId(employee.getId())
                .amount(request.amount())
                .price(request.price())
                .weightGrams(request.weightGrame())
                .installmentCount(request.installmentCount())
                .pawnDate(request.pawnDate())
                .returnDate(request.returnDate())
                .notes(request.notes())
                .build();

    }
    public PawnResponse toResponse(Pawn pawn) {
        return new PawnResponse(
                pawn.getId(),
                pawn.getGlobalId(),
                pawn.getArticle() != null ? pawn.getArticle().getGlobalId() : null,
                pawn.getArticle() != null ? pawn.getArticle().getNameArticle() : null,
                pawn.getCliente() != null ? pawn.getCliente().getGlobalId() : null,
                pawn.getCliente() != null ? buildFullName(pawn.getCliente()) : null,
                null,
                pawn.getAmount(),
                pawn.getPrice(),
                pawn.getWeightGrams(),
                pawn.getInstallmentCount(),
                pawn.getInstallmentsPaid(),
                pawn.getInstallmentsMissed(),
                pawn.getPawnDate(),
                pawn.getReturnDate(),
                pawn.getStatus(),
                pawn.canAcceptPayments(),
                pawn.isTerminalState(),
                pawn.getNotes(),
                pawn.getCreatedAt(),
                pawn.getUpdatedAt()
        );
    }
    private String buildFullName(Cliente cliente) {
        String lastName = cliente.getLastName();
            return  (lastName != null && !lastName.isBlank())
            ? cliente.getFirstName()+" "+lastName
            : cliente.getFirstName();
    }
    public PawnPaymentResponse toPawnPaymentRequest(PawnPayment payment, UUID pawnGlobalId, UUID employeeGlobalId) {
        return new PawnPaymentResponse(
                payment.getId(),
                pawnGlobalId,
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getNotes(),
                employeeGlobalId,
                payment.getMissed(),
                payment.getCreatedAt()
        );
    }
}
