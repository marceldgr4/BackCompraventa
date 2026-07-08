package com.CompraVenta.Backend.Modules.Pawns.Service;

import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreateAgilePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreatePawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.PawnPaymentRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnPaymentResponse;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnResponse;
import com.CompraVenta.Backend.Modules.Pawns.Enums.PawnStatus;

import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface PawnService {
    Page<PawnResponse> findAll(PawnStatus status, UUID clienteGlobalId, UUID employeeGlobalId,
                               LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    PawnResponse findByGlobalId(UUID globalId);
    PawnResponse create(CreatePawnRequest request);
    PawnResponse createAgile(CreateAgilePawnRequest request);
    PawnPaymentResponse registerPayment(UUID globalId, PawnPaymentRequest request);
    PawnPaymentResponse registerMissedInstallment(UUID globalId);
    PageResponse<PawnPaymentResponse> findPayments(UUID globalId, Pageable pageable);
    PawnResponse markAsReturned(UUID globalId);
    PawnResponse markAsLost(UUID globalId);
    PawnResponse markAsExpiredManually(UUID globalId);
    void delete(UUID globalId);
    int expireOverduePawns();
}
