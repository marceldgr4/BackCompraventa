package com.CompraVenta.Backend.Modules.Employee.Service.Impl;

import com.CompraVenta.Backend.Audit.annotation.Auditable;
import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.CreateEmployeeRequest;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateEmployee;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateProfile;
import com.CompraVenta.Backend.Modules.Employee.Dto.Response.EmployeeResponse;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Mapper.EmployeeMapper;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.Employee.Service.EmployeeService;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import com.CompraVenta.Backend.Shared.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class EmployeeServiceImpl implements EmployeeService {

    private  final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findAll(String search, Boolean active, Role rol, Pageable pageable){
        return PageResponse.from(
                employeeRepository.findByFilters(search,active,rol,pageable)
                        .map(employeeMapper::toEmployeeResponse)
        );
    }
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id){
        return employeeRepository.findById(id)
                .map(employeeMapper::toEmployeeResponse)
                .orElseThrow(()-> new ResourceNotFoundException("Employee not found",id));
    }
    @Override
    @Transactional
    @Auditable(operation = "CREATE_EMPLOYEE", entity = "employees")
    public EmployeeResponse create(CreateEmployeeRequest request){
        if(employeeRepository.existsByEmailIgnoreCase(request.email())){
            throw new BusinessException("Employee already exists"+ request.email());
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setPasswordHash(passwordEncoder.encode(request.password()));
        Employee saved = employeeRepository.save(employee);

        log.info("Empleado creado con ID={}, emial={}, rol={}",saved.getId(),saved.getEmail(),saved.getRol());
        return employeeMapper.toEmployeeResponse(saved);
    }





    @Override
    @Transactional
    @Auditable(operation = "UPDATE_EMPLOYEE",entity = "employees")
    public EmployeeResponse update(UUID id, UpdateEmployee request) {
        Employee employee = getEmployeeOrThrow(id);
        employeeMapper.applyUpdates(employee,request);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }


    @Override
    @Transactional
    @Auditable(operation = "SET_EMPLOYEE_STATUS", entity = "employees")
    public EmployeeResponse setActive(UUID id, Boolean active) {
        Employee employee = getEmployeeOrThrow(id);

        if(!active) {
            String currentEmail = SecurityContext.getCurrentUsername();
            if (employee.getEmail().equalsIgnoreCase(currentEmail)) {
                throw new BusinessException("El administrador no puede desactivar su propia cuenta.");
            }
        }
        employee.setActive(active);
        String action = active ? "ACTIVO" : "DESACTIVADO";
        log.info("Employee {} : id = {}",action,id);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    @Auditable(operation = "DELECT_EMPLOYEE", entity = "employees")
    public void delete(UUID id) {
        if(!employeeRepository.existsById(id)){
            throw new ResourceNotFoundException("Employee not found",id);
        }
        employeeRepository.deleteById(id);
        log.info("Employee  delect: id = {}",id);
    }

    @Override
    @Transactional
    @Auditable(operation = "UPDATE_MY_PROFILE",entity = "employees")
    public EmployeeResponse updateMyProfile(String currentEmail, UpdateProfile request) {
        Employee employee = employeeRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Employee not found",currentEmail));
        if(request.fullName() != null){
            employee.setFullName(request.fullName().trim());
        }
        if(request.newPassword() != null){
            if(!request.newPassword().equals(request.confirmPassword())){
                throw new BusinessException("Passwords don't match");
            }
            employee.setPasswordHash(passwordEncoder.encode(request.newPassword()));
            log.info("Password update for employees: email ={}",currentEmail);
        }
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }


    private Employee getEmployeeOrThrow(UUID id){
        return employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Employee not found",id));
    }
}
