package com.CompraVenta.Backend.Modules.auth.Controller;

import com.CompraVenta.Backend.Modules.Employee.Dto.Request.CreateEmployeeRequest;
import com.CompraVenta.Backend.Modules.Employee.Dto.Response.EmployeeResponse;
import com.CompraVenta.Backend.Modules.Employee.Service.EmployeeService;
import com.CompraVenta.Backend.Modules.auth.Dto.Request.LoginRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Request.RefreshRequest;
import com.CompraVenta.Backend.Modules.auth.Dto.Response.AuthResponse;
import com.CompraVenta.Backend.Modules.auth.Service.AuthService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name ="Auth",description = "Autenticacion y gestion de usuario")

public class AuthController {
    private final AuthService authService;
    private final EmployeeService employeeService;

    @PostMapping("/login")
    @Operation(summary = "inicio de sesion")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody
            @Valid
            LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.login(request),
                "sesion iniciada correctamente."
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody
            @Valid
            RefreshRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request),
                "Token renovado correctamente."));
    }

    @PostMapping("/logout")
    @Operation(summary = "cerrar session en invalidad token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authoorization") String authoorization) {
        String token = extractBearerToken(authoorizationHeader);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sesion cerrada correctamente."));
    }
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "resgistro de nuenvo empleado(solo admin)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> register(
            @RequestBody
            @Valid
            CreateEmployeeRequest request){
        return ResponseEntity.status(201).body(ApiResponse.ok(
                employeeService.create(request),
                "empleado registrado correctamente."
        ));
    }
    private String extractBearerToken(String headers){
        if(header !=null && header.startsWith("Bearer")){
            return header.substring(7);
        }
        return "";
    }
}


