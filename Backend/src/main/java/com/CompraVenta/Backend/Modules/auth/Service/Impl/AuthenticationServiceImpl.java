package com.CompraVenta.Backend.Modules.auth.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;

import com.CompraVenta.Backend.Exception.custom.UnauthorizedException;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Modules.Employee.Repository.EmployeeRepository;

import com.CompraVenta.Backend.Modules.auth.Dto.Request.LoginRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;

import com.CompraVenta.Backend.Modules.auth.Service.AuthenticationService;

import com.CompraVenta.Backend.Security.model.CustomUserDetails;
import com.CompraVenta.Backend.Security.service.JwtService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import com.CompraVenta.Backend.Modules.auth.Service.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;
    private final TokenService tokenService;
    private final LoginRateLimitService rateLimitService;

    //--login

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        rateLimitService.checkLoginAttempts(email);

        Employee employee = employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    rateLimitService.incrementAttempts(email);
                    return new UnauthorizedException("Credenciales incorrectes");
                });
        if (!employee.canAuthenticate()) {
            throw new BusinessException("La cuenta esta desactivada. contactar el administrador.");

        }
        authenticationCredentials(email, request.password(), employee);
        rateLimitService.resetAttempts(email);
        CustomUserDetails userDetails = buildUserDetails(employee);
        String accessToken = jwtService.generateAccessToken(buildExtrateClaims(employee), userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        tokenService.storeRefreshToken(email, refreshToken);
        log.info("Login exitoso: email{},rol{}", email, employee.getRole());
        return buildAuthResponse(accessToken, refreshToken, employee);
    }
    private void authenticationCredentials(String email, String password, Employee employee) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        }catch (DisabledException e) {
            throw new BusinessException("la cuenta esta desactivada.");
        }
        catch (AuthenticationException ex){
            rateLimitService.incrementAttempts(email);
            throw new UnauthorizedException("Credenciales incorrectes");
        }
    }
    private CustomUserDetails buildUserDetails(Employee employee) {
        return new CustomUserDetails(
                employee.getEmail(),
                employee.getPasswordHash(),
                employee.getRole().name(),
                employee.isActive()
        );
    }
    private java.util.Map<String, Object> buildExtrateClaims(Employee employee) {
        return java.util.Map.of("role", employee.getRole().name());
    }
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, Employee employee) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                new AuthResponse.EmployeeInfo(
                        employee.getId(),
                        employee.getEmail(),
                        employee.getFullName(),
                        employee.getRole()
                ),
                "online"
        );
    }


}