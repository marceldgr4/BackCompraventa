package com.CompraVenta.Backend.Modules.Purchases.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Modules.Purchases.Entity.Purchase;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeContextService {

    private final EmployeeRepository employeeRepository;

    public Employee getCurrentEmployee() {
        String email = SecurityContext.getCurrentUsername();
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("No se encontró el empleado autenticado"));
    }

    public Long getCurrentEmployeeId() {
        return getCurrentEmployee().getId();
    }

    public boolean isAdmin() {
        return SecurityContext.hasRole("ADMIN");
    }

    public void validatePurchaseAccessOrThrow(Purchase purchase) {
        if (!isAdmin() && !purchase.getEmployeeId().equals(getCurrentEmployeeId())) {
            throw new BusinessException("No tiene permiso para ver esta compra");
        }
    }
}
