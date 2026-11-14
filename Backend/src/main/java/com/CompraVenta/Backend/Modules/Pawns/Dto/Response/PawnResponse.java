package com.CompraVenta.Backend.Modules.Pawns.Dto.Response;

import com.CompraVenta.Backend.Modules.Pawns.Enums.PawnStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PawnResponse(
        Long id,
        UUID globalId,

        UUID articleGlobalId,
        String articleName,

        UUID clienteGlobalId,
        String clienteFullName,

        UUID employeeGlobalId,
        Integer amount,
        BigDecimal price,
        BigDecimal wightGrams,

        Integer installmentCount,
        Integer installmentsPaid,
        Integer installmentsMissed,

        LocalDate pawnDate,
        LocalDate returnDate,

        PawnStatus status,
        boolean canAcceptPayments,
        boolean isTerminalState,

        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
