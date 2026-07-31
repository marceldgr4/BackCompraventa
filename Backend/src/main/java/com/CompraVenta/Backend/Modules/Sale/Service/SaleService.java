package com.CompraVenta.Backend.Modules.Sale.Service;

import com.CompraVenta.Backend.Modules.Sale.Dto.Request.CreateSaleRequest;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface SaleService {
    PageResponse<SaleResponse> findByAll(UUID clienteGlobalID, LocalDate dateFrom,
                                         LocalDate dateTo, Pageable pageable);
    SaleResponse findByGlobalId(UUID GlobalId);
    SaleResponse create(CreateSaleRequest request);
    void  delete(UUID globalId);
}
