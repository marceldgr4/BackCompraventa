package com.CompraVenta.Backend.Security.service;

import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;
import com.CompraVenta.Backend.Security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(()-> {
                    log.warn("Authentication attempt for unknow email: {}", email);
                    return new UsernameNotFoundException("Authentication employees attempt for unknow email: " + email);
                });
        log.debug("Loading user: email={}, rol ={}, active ={}", employee.getEmail(), employee.getRole(), employee.isActive());
        return new CustomUserDetails(
                employee.getEmail(),
                employee.getPasswordHash(),
                employee.getRole().name(),
                Boolean.TRUE.equals(employee.isActive())
        );
    }
}
