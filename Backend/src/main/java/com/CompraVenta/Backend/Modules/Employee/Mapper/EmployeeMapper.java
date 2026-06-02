package com.CompraVenta.Backend.Modules.Employee.Mapper;

import com.CompraVenta.Backend.Modules.Employee.Dto.Request.CreateEmployeeRequest;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateEmployee;
import com.CompraVenta.Backend.Modules.Employee.Dto.Response.EmployeeResponse;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toEmployeeResponse(Employee employee){
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmail(),
                employee.getFullName(),
                employee.getRole(),
                employee.isActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
    public Employee toEntity(CreateEmployeeRequest request){
        return Employee.builder()
                .email(request.email().trim().toLowerCase())
                .fullName(request.fullName().trim())
                .passwordHash("")
                .role(request.rol())
                .active(true)
                .build();
    }
    public void applyUpdates(Employee employee, UpdateEmployee request) {
        if(request.fullName() !=null){
            employee.setFullName(request.fullName().trim());
        }
        if (request.rol() !=null){
            employee.setRole(request.rol());
        }
    }
}
