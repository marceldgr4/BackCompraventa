package com.CompraVenta.Backend.Modules.Employee.Controller;

import com.CompraVenta.Backend.Modules.Employee.Dto.Request.CreateEmployeeRequest;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateEmployee;
import com.CompraVenta.Backend.Modules.Employee.Dto.Request.UpdateProfile;
import com.CompraVenta.Backend.Modules.Employee.Dto.Response.EmployeeResponse;
import com.CompraVenta.Backend.Modules.Employee.Service.EmployeeService;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import com.CompraVenta.Backend.Shared.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Validated
@Tag(name = "Employees", description = "Gestion de empleados-solo admin")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista empleados con paginacion y filtros (admin")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Role rol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size,
            @RequestParam(defaultValue = "fullName") String sortBy
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.findAll(search,active,rol,pageable)
        ));

    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener empleados por ID (admin)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> findById(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.ok(employeeService.findById(id)));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar nuevo empleado Admin")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @RequestBody
            @Valid
            CreateEmployeeRequest request){
        EmployeeResponse created = employeeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actulizar nombre o rol de un empleado (admin)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable UUID id,
            @RequestBody
            @Valid
            UpdateEmployee request
    ){
        return ResponseEntity.ok(ApiResponse.ok(employeeService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar cuenta de empelados (admin)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> setStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ){
        return ResponseEntity.ok(ApiResponse.ok(employeeService.setActive(id,active)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar empleado (admin)- falla si tiene operaciones")
    public  ResponseEntity<Void> delete(@PathVariable UUID id){
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/me")
    @Operation(summary = "Actulziar propio perfil (nombre o contraseña)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateMyProfile(
            @RequestBody
            @Valid UpdateProfile request,
            Authentication authentication
            ){
        String email = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(employeeService.updateMyProfile(email, request),
        "Perfil actulizado correctamente"
        ));
    }


}
