package com.CompraVenta.Backend.Modules.Pawns.Service;

import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreateAgilPawnRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.CreatePawnResquest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Request.PawnPaymentRequest;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnPaymentResponse;
import com.CompraVenta.Backend.Modules.Pawns.Dto.Response.PawnResponse;
import com.CompraVenta.Backend.Modules.Pawns.Emuns.PawnStatus;

import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface PanwService {
    Page<PawnResponse> findAll(PawnStatus status, UUID clienteGlobalId, UUID employeeGlobalId,
                               LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    PawnResponse findByGlobalId(UUID globalId);
    PawnResponse create(CreatePawnResquest resquest);
    PawnResponse createAgile(CreateAgilPawnRequest request);
    PawnPaymentResponse registerPayment(UUID globalId, PawnPaymentRequest request);
    PawnPaymentResponse registerMissedIntallment(UUID globalId);
    PageResponse<PawnPaymentResponse> findPaymants(UUID globalId, Pageable pageable);
    PawnResponse markAsReturned(UUID globalId);
    PawnResponse markAslost(UUID globalId);
    PawnResponse markAsExpiredManually(UUID globalId);
    void delete(UUID globalId);
    int expireOverdiePawns();
}
