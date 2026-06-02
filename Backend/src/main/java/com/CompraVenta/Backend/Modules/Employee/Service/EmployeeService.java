package com.CompraVenta.Backend.Modules.Employee.Service;

import com.CompraVenta.Backend.Modules.Employee.Dto.Request.CreateEmployeeRequest;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateEmployee;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateProfile;
import com.CompraVenta.Backend.Modules.Employee.Dto.Response.EmployeeResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import com.CompraVenta.Backend.Shared.enums.Role;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface EmployeeService {
    PageResponse<EmployeeResponse> findAll(String search, Boolean active, Role rol, Pageable pageable);

    EmployeeResponse findById(UUID id);

    EmployeeResponse create(CreateEmployeeRequest request);

    EmployeeResponse update(UUID id, UpdateEmployee request);

    EmployeeResponse setActive(UUID id, Boolean active);
    void delete(UUID id);

    EmployeeResponse updateMyProfile(String currentEmail, UpdateProfile request);
}
