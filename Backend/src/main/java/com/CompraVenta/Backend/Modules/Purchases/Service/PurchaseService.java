package com.CompraVenta.Backend.Modules.Purchases.Service;

import com.CompraVenta.Backend.Modules.Purchases.Dto.Request.CreatePurchaseRequest;
import com.CompraVenta.Backend.Modules.Purchases.Dto.Response.PurchaseResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseService {
    PageResponse<PurchaseResponse> findAll(UUID clienteGlobalId,
                                           LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    PurchaseResponse findByGlobalId(UUID globalId);

    List<PurchaseResponse> create(CreatePurchaseRequest request);
    void delete(UUID globalId);
}
