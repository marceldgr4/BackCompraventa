package com.CompraVenta.Backend.Exception.handler;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Exception.custom.UnauthorizedException;
import com.CompraVenta.Backend.Shared.Dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.CompraVenta.Backend.Shared.Dto.ErrorDetail;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

            @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
            log.warn("Recurso no encontrado: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage(), "NOT_FOUND"));
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
            log.warn("Regla de negocio violada: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(ex.getMessage(), "BUSINESS_RULE_VIOLATION"));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
            log.warn("Acceso no autorizado: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
        }

        @ExceptionHandler(com.CompraVenta.Backend.Exception.custom.DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<java.util.Map<String, String>>> handleDuplicateResource(com.CompraVenta.Backend.Exception.custom.DuplicateResourceException ex) {
            log.warn("Recurso duplicado detectado: {} con ID existente: {}", ex.getMessage(), ex.getExistingGlobalId());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.<java.util.Map<String, String>>builder()
                            .success(false)
                            .data(java.util.Map.of("existingGlobalId", ex.getExistingGlobalId()))
                            .error(com.CompraVenta.Backend.Shared.Dto.ErrorDetail.of(ex.getMessage(), "DUPLICATE_RESOURCE"))
                            .build());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Credenciales incorrectas.", "INVALID_CREDENTIALS"));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tiene permisos para esta operación.", "FORBIDDEN"));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
            Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                            (existing, replacement) -> existing
                    ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, String>>builder()
                            .success(false)
                            .data(errors)
                            .error(ErrorDetail.of("Error de validación.", "VALIDATION_ERROR"))
                            .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
            log.error("Error interno inesperado", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno del servidor.", "INTERNAL_ERROR"));
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ApiResponse<Void>> handleLocked(LockedException ex) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body((ApiResponse.error(ex.getMessage(), "ACCOUNT_LOCKED")));
        }

        @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("cuenta desactivada. contantar el admin", "ACCOUNT_DISABLED"));
        }

    }

