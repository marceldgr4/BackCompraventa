package com.CompraVenta.Backend.Modules.Sale.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Modules.Articles.Repository.ArticleRepository;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.Sale.Dto.Response.SaleResponse;
import com.CompraVenta.Backend.Modules.Sale.Entity.Sale;
import com.CompraVenta.Backend.Modules.Sale.Mapper.SaleMapper;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleDetailRepository;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleProcedureRepository;
import com.CompraVenta.Backend.Modules.Sale.Repository.SaleRepository;
import com.CompraVenta.Backend.Modules.Sale.Service.SaleService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private  final SaleProcedureRepository saleProcedureRepository;
    private final ArticleRepository articleRepository;
    private  final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private  final SaleMapper saleMapper;

    private Employee getCurrentEmployee() {
        String email = SecurityContext.getCurrentUsername();
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(()-> new BusinessException(" No se encontro el empleado autenticado"));

    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> findAll(UUID clienteGlobalId, LocalDate dateFrom,
                                              LocalDate dateTo, Pageable pageable){
        Long clienteId = resolveClienteId(clienteGlobalId);
        Long employeeId = SecurityContext.hasRole("ADMIN") ? null : getCurrentEmployee().getId();
        LocalDateTime from = dateFrom !=null ? dateFrom.atStartOfDay():null;
        LocalDateTime to = dateTo !=null ? dateTo.atTime(LocalTime.MAX):null;

        Page<Sale> sales = saleRepository.findByFilters(employeeId,clienteId,from,to,pageable);
        return PageResponse.from(sales.map(this:: toResponseWithDetails));
    }
    


}
