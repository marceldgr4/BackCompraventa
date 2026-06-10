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
                employee.getGlobalId(),
                employee.getEmail(),
                employee.getFullName(),
                employee.getRol(),
                employee.isActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
    public Employee toEntity(CreateEmployeeRequest request){
        Employee employee = new Employee();
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setFullName(request.fullName().trim());
        employee.setPasswordHash("");
        employee.setRol(request.rol());
        employee.setActive(true);
        return employee;
    }
    public void applyUpdates(Employee employee, UpdateEmployee request) {
        if(request.fullName() !=null){
            employee.setFullName(request.fullName().trim());
        }
        if (request.rol() !=null){
            employee.setRol(request.rol());
        }
    }
}
