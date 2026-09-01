# 📋 INFORME TÉCNICO INTEGRAL — Sistema CompraVenta Backend
**Versión:** 1.1.0 | **Fecha original:** 2026-05-26 | **Actualizado:** 2026-09-01 | **Clasificación:** Confidencial — Uso interno

> **Estado actual (septiembre 2026):** Auth, Employee, Clients, Articles, Pawns, Sales y Purchases están implementados. Compilación Maven OK. Docker/JWT/Audit/excepciones operativos. **Pendiente:** motor Sync (solo tabla), tests, Dashboard. El diagnóstico de mayo (módulos vacíos, UserDetails vacío, Dockerfile `top -b`) **ya no describe el repo**. Detalle vigente: `INFORME_ANALISIS_COMPRAVENTA.md`.

---

## TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Análisis de Arquitectura General](#2-análisis-de-arquitectura-general)
3. [Diagnóstico por Módulo](#3-diagnóstico-por-módulo)
4. [Correcciones Estructurales](#4-correcciones-estructurales)
5. [Historias de Usuario — Módulo AUTH](#5-historias-de-usuario--módulo-auth)
6. [Configuración Docker Optimizada](#6-configuración-docker-optimizada)
7. [Recomendaciones de Seguridad](#7-recomendaciones-de-seguridad)
8. [Estructura Final Propuesta del Proyecto](#8-estructura-final-propuesta-del-proyecto)
9. [Compatibilidad Desktop / Web / API](#9-compatibilidad-desktop--web--api)
10. [Roadmap de Mejoras](#10-roadmap-de-mejoras)

---

## 1. RESUMEN EJECUTIVO

### 1.1 Descripción del sistema

El proyecto **CompraVenta** es un sistema híbrido de gestión para casas de empeño y compraventa, compuesto por:

- **Backend API REST** — Spring Boot 3.x con PostgreSQL, Redis y Supabase
- **Frontend Web** — (en planificación)
- **Aplicación Desktop** — Java Swing (módulo separado)
- **Motor de Sincronización** — Arquitectura offline-first con outbox pattern

### 1.2 Estado general del proyecto

| Área | Estado | Severidad |
|---|---|---|
| Infraestructura Spring Boot | ✅ Sólida | — |
| Seguridad JWT | ✅ Correcta | — |
| Módulos de negocio | ✅ Auth, Employee, Clients, Articles, Pawns, Sales, Purchases | — |
| Excepciones personalizadas | ✅ Completas | — |
| AuditAspect, UserDetailsServiceImpl | ✅ Implementados | — |
| Docker / Containerización | ✅ Compose + Dockerfile | — |
| ResourceNotFoundException | ✅ Completa | — |
| Tests unitarios | 🔴 Ausentes | ALTO |
| Documentación API (OpenAPI) | ✅ Configurada | — |
| Sincronización Offline | 🟡 Entidad/tabla; falta servicio | MEDIO |

---

## 2. ANÁLISIS DE ARQUITECTURA GENERAL

### 2.1 Arquitectura actual (lo que existe)

```
┌─────────────────────────────────────────────────────────────────┐
│                      CLIENTE (Desktop/Web)                       │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/REST
┌─────────────────────────────▼───────────────────────────────────┐
│                 Spring Boot API (Puerto 8080)                     │
│  ┌───────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │ SecurityConfig│  │  JwtFilter   │  │  GlobalExceptionHandler│ │
│  └───────────────┘  └──────────────┘  └───────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                    Capas de Negocio                        │  │
│  │  Controller → Service → Repository → Entity               │  │
│  │  [INCOMPLETAS — solo infraestructura existe]               │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌──────────────┐  ┌───────────────┐  ┌────────────────────────┐ │
│  │  Redis Cache │  │  PostgreSQL   │  │  Supabase (Sync)        │ │
│  └──────────────┘  └───────────────┘  └────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Arquitectura objetivo recomendada

```
┌───────────────────────────────────────────────────────────────────────┐
│                           CAPA DE PRESENTACIÓN                         │
│    Desktop Swing (offline)          Web SPA (React / Angular)          │
└──────────────────────────────┬────────────────────────────────────────┘
                               │ JWT Bearer Token
┌──────────────────────────────▼────────────────────────────────────────┐
│                         API GATEWAY / NGINX                            │
│              Rate Limiting · SSL Termination · Load Balancing          │
└──────────────────────────────┬────────────────────────────────────────┘
                               │
┌──────────────────────────────▼────────────────────────────────────────┐
│                    SPRING BOOT REST API (v1)                            │
│                                                                        │
│  ┌─────────────┐ ┌──────────┐ ┌────────────┐ ┌──────┐ ┌──────────┐  │
│  │ AuthModule  │ │ Articles │ │   Pawns    │ │Sales │ │ Clients  │  │
│  │  /auth/**   │ │/articles │ │  /pawns    │ │/sales│ │/clients  │  │
│  └─────────────┘ └──────────┘ └────────────┘ └──────┘ └──────────┘  │
│  ┌─────────────┐ ┌──────────┐                                          │
│  │  Employees  │ │Purchases │  ← implementados 2026                    │
│  │ /employees  │ │/purchases│                                          │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │              INFRAESTRUCTURA TRANSVERSAL                         │ │
│  │  AuditAspect · SecurityContext · SyncEngine · CacheManager       │ │
│  └──────────────────────────────────────────────────────────────────┘ │
└───────────┬──────────────────────────────────────┬───────────────────┘
            │                                      │
┌───────────▼───────────┐              ┌───────────▼──────────────────┐
│  PostgreSQL LOCAL      │              │  Supabase (PostgreSQL Cloud)  │
│  (Primario)            │◄────Sync────►│  (Replica/Backup)             │
└───────────┬───────────┘              └──────────────────────────────┘
            │
┌───────────▼───────────┐
│    Redis Cache         │
│  (Sessions/Articles)   │
└───────────────────────┘
```

### 2.3 Evaluación de principios SOLID

| Principio | Estado | Observación |
|---|---|---|
| **S** — Single Responsibility | 🟡 Parcial | `CorsConfig` mezcla lectura de props con construcción de fuente CORS |
| **O** — Open/Closed | ✅ OK | Excepciones extienden `RuntimeException` correctamente |
| **L** — Liskov | ✅ OK | No hay herencias conflictivas en lo implementado |
| **I** — Interface Segregation | 🔴 Violado | Services sin interfaces definidas (sólo implementaciones) |
| **D** — Dependency Inversion | 🟡 Parcial | `JwtAuthenticationFilter` inyecta correctamente; otros módulos vacíos |

---

## 3. DIAGNÓSTICO POR MÓDULO

### 3.1 🔴 CRÍTICO — Clases vacías o incompletas

Los siguientes archivos existen en la estructura pero **no tienen implementación real**, lo cual causará fallo en arranque o comportamiento indefinido:

#### `AuditRepository.java`
```java
// ESTADO ACTUAL — causará error de compilación si se referencia
public class AuditRepository {
}
```
**Corrección requerida:**
```java
@Repository
public interface AuditRepository extends JpaRepository<AudLog, Long> {

    List<AudLog> findByEmployeeIdOrderByTimestampDesc(String employeeId);

    @Query("SELECT a FROM AudLog a WHERE a.timestamp BETWEEN :from AND :to")
    Page<AudLog> findByDateRange(
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable
    );
}
```

#### `AudiAspect.java`
```java
// ESTADO ACTUAL — Aspecto vacío, la anotación @Auditable es inerte
public class AudiAspect {
}
```
**Corrección requerida** (ver Sección 4.1 — Implementación completa del AuditAspect).

#### `ResourceNotFoundException.java`
```java
// ESTADO ACTUAL — NO extiende RuntimeException, no puede lanzarse
public class ResourceNotFoundException {
}
```
**Corrección requerida:**
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("Recurso '%s' no encontrado con id: %s", resource, id));
    }
}
```

#### `UserDetailsServiceImpl.java`
```java
// ESTADO ACTUAL — SecurityConfig inyecta UserDetailsService,
// pero la implementación está vacía → NullPointerException en login
public class UserDetailsServiceImpl {
}
```
**Corrección requerida** (ver Sección 4.2).

#### `CustomUserDetails.java`
```java
// ESTADO ACTUAL — Vacío, el sistema usa UserDetails de Spring directamente
public class CustomUserDetails {
}
```

#### `SecurityContext.java`
```java
// ESTADO ACTUAL — Vacío, no provee utilidades de contexto de seguridad
public class SecurityContext {
}
```

#### `ErrorResponse.java`
```java
// ESTADO ACTUAL — Vacío, el handler usa ApiResponse<Void> pero este DTO no está implementado
public class ErrorResponse {
}
```

---

### 3.2 🟠 ALTO — Problemas de diseño

#### Problema 1: `ApiResponse` tiene `ErrorDetail` anidado Y usa el `ErrorDetail` externo

En `GlobalExceptionHandler.java` línea:
```java
.error(ApiResponse.ErrorDetail.of("Error de validación.", "VALIDATION_ERROR"))
```
Pero `ApiResponse` no tiene un inner class `ErrorDetail` definido. Usa `ErrorDetail` del paquete `Shared.Dto`. Esto causará **error de compilación**.

**Corrección:**
```java
// En GlobalExceptionHandler, reemplazar:
.error(ApiResponse.ErrorDetail.of(...))  // ❌ No existe

// Por:
.error(ErrorDetail.of(...))  // ✅ Correcto - import Dto.Shared.com.compraventa.backend.ErrorDetail
```

#### Problema 2: `CorsConfig.java` — Typo en `@Value` que causa error de arranque

```java
// LÍNEA 20 — Falta la llave de cierre }
@Value("${cors.allowed-methods")   // ❌ Incorrecto — faltó la llave }
private String allowedMethods;

// Corrección:
@Value("${cors.allowed-methods}")  // ✅ Correcto
private String allowedMethods;
```

#### Problema 3: `BaseEntity.java` — Columna `delete` es palabra reservada SQL

```java
@Column(name = "delete", nullable = false)   // ❌ "delete" es reservada en SQL
private Boolean deleted = false;

// Corrección:
@Column(name = "is_deleted", nullable = false)  // ✅ Sin conflicto
private Boolean deleted = false;
```

#### Problema 4: `AudLog.java` — Typo en nombre de columna

```java
@Column(name = "emplooyee_id")   // ❌ Doble 'o'
private String emplooyeeId;

// Corrección:
@Column(name = "employee_id")    // ✅
private String employeeId;
```

#### Problema 5: `SyncOutbox.java` — Typo en nombre de campo

```java
@Column(name = "paypload", ...)   // ❌ Typo en nombre de columna
private String paypload;

// Corrección:
@Column(name = "payload", ...)    // ✅
private String payload;
```

#### Problema 6: `JwtService.java` — Método duplicado con typo

```java
// Existen DOS métodos de generación de access token:
public String generateAccesoToken(UserDetails userDetails) { ... }   // ❌ "Acceso" sin 's'
public String generateAccessoToken(Map<String,Object> extraClaims, ...) { ... }  // ❌ "Accesso"

// Corrección — unificar con naming consistente:
public String generateAccessToken(UserDetails userDetails) { ... }
public String generateAccessToken(Map<String,Object> extraClaims, UserDetails userDetails) { ... }
```

---

### 3.3 🟡 MEDIO — Mejoras de calidad

#### Problema 7: `DateSorceConfig.java` — Nombre de clase con typo

```
DateSorceConfig → debería ser DataSourceConfig
```

#### Problema 8: `GlobalEceptionHandler.java` — Nombre con typo

```
GlobalEceptionHandler → debería ser GlobalExceptionHandler
```

#### Problema 9: `RedisConfig.java` — Parámetro no utilizado en firma

```java
// RestClient.Builder builder no se usa dentro del método
public RedisTemplate<String, Object> redisTemplate(
    RedisConnectionFactory redisConnectionFactory,
    RestClient.Builder builder  // ❌ No se usa, crea dependencia innecesaria
) {
```

#### Problema 10: `Role.java` — Convención de nombres en enum

```java
public enum Role {
    Admin,    // ❌ No sigue la convención UPPER_CASE de Java para enums
    Empleado  // ❌
}

// Corrección:
public enum Role {
    ADMIN,     // ✅
    EMPLEADO   // ✅
}
```

---

### 3.4 Inventario de módulos de negocio faltantes

Los siguientes módulos están en el código (septiembre 2026). Dashboard sigue sin implementar.

| Módulo | Controller | Service | Repository | Entity | DTO |
|---|---|---|---|---|---|
| Auth | ✅ | ✅ | ✅ | ✅ | ✅ |
| Articles | ✅ | ✅ | ✅ | ✅ | ✅ |
| Pawns (Empeños) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Sales | ✅ | ✅ | ✅ | ✅ | ✅ |
| Purchases | ✅ | ✅ | ✅ | ✅ | ✅ |
| Clients | ✅ | ✅ | ✅ | ✅ | ✅ |
| Employees | ✅ | ✅ | ✅ | ✅ | ✅ |
| Dashboard | ❌ | ❌ | — | — | ❌ |

---

## 4. CORRECCIONES ESTRUCTURALES

### 4.1 Implementación del AuditAspect

```java
package com.CompraVenta.Backend.Audit.aspect;

import annotation.Audit.com.compraventa.backend.Auditable;
import entity.Audit.com.compraventa.backend.AudLog;
import com.CompraVenta.Backend.Audit.repositoy.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

/**
 * Aspecto de auditoría que intercepta métodos anotados con {@link Auditable}
 * y persiste un registro de la operación en la tabla audit_log.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String employeeId = extractEmployeeId();
        String ipAddress = extractIpAddress();
        String beforeValue = null;
        String afterValue = null;
        String errorMessage = null;

        Object result = null;

        try {
            // Capturar estado previo si hay argumentos
            if (joinPoint.getArgs().length > 0) {
                beforeValue = serializeSafely(joinPoint.getArgs()[0]);
            }

            result = joinPoint.proceed();

            // Capturar estado posterior
            if (result != null) {
                afterValue = serializeSafely(result);
            }
        } catch (Throwable ex) {
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            persistAuditLog(
                auditable.operation(),
                auditable.entity(),
                employeeId,
                ipAddress,
                beforeValue,
                afterValue,
                errorMessage
            );
        }

        return result;
    }

    private void persistAuditLog(
        String operation, String entityType, String employeeId,
        String ipAddress, String beforeValue, String afterValue, String errorMessage
    ) {
        try {
            AudLog log = AudLog.builder()
                .operation(operation)
                .entityType(entityType)
                .employeeId(employeeId)
                .ipAddress(ipAddress)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .errorMessage(errorMessage)
                .timestamp(Instant.now())
                .build();
            auditRepository.save(log);
        } catch (Exception e) {
            log.error("Error persisting audit log for operation {}: {}", operation, e.getMessage());
        }
    }

    private String extractEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    }

    private String extractIpAddress() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (IllegalStateException e) {
            return "UNKNOWN";
        }
    }

    private String serializeSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
```

### 4.2 Implementación de UserDetailsServiceImpl

```java
package com.CompraVenta.Backend.Security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de UserDetailsService para Spring Security.
 * Carga el empleado (usuario) por su nombre de usuario/email
 * desde la base de datos local para autenticación JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // Inyectar EmployeeRepository cuando esté implementado
    // private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario por username: {}", username);

        // TODO: Reemplazar con consulta real cuando Employee entity esté implementado
        // return employeeRepository.findByUsernameOrEmail(username, username)
        //     .map(CustomUserDetails::new)
        //     .orElseThrow(() -> new UsernameNotFoundException(
        //         "Usuario no encontrado: " + username
        //     ));

        throw new UsernameNotFoundException(
                "UserDetailsService no está completamente implementado aún. Username: " + username
        );
    }
}
```

### 4.3 Implementación de CustomUserDetails

```java
package com.CompraVenta.Backend.Security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementación de UserDetails para encapsular la información
 * del empleado autenticado en el contexto de Spring Security.
 * Adaptar los campos cuando la entidad Employee esté implementada.
 */
public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final String role;
    private final boolean enabled;

    // Constructor que aceptará Employee entity cuando esté disponible
    public CustomUserDetails(String username, String password, String role, boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
```

### 4.4 Implementación de SecurityContextHelper

```java
package com.CompraVenta.Backend.Security.context;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Utilidad estática para acceder al contexto de seguridad de Spring.
 * Centraliza el acceso al usuario autenticado actual.
 */
@UtilityClass
public class SecurityContextHelper {

    /**
     * Obtiene el nombre de usuario del empleado actualmente autenticado.
     * @return username del empleado autenticado, o "ANONYMOUS" si no hay sesión
     */
    public String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(principal -> {
                if (principal instanceof UserDetails ud) return ud.getUsername();
                return principal.toString();
            })
            .orElse("ANONYMOUS");
    }

    /**
     * Verifica si hay un usuario autenticado en el contexto actual.
     * @return true si hay sesión activa
     */
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Verifica si el usuario actual tiene el rol especificado.
     * @param role nombre del rol sin prefijo ROLE_ (ej. "ADMIN")
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
}
```

---

## 5. HISTORIAS DE USUARIO — MÓDULO AUTH

---

### HU-AUTH-01 — Login de empleado con JWT

| Campo | Detalle |
|---|---|
| **Código** | HU-AUTH-01 |
| **Módulo** | Autenticación |
| **Prioridad** | 🔴 ALTA |
| **Estimación** | 5 puntos |

**Título:** Inicio de sesión de empleado mediante credenciales y emisión de token JWT

**Descripción funcional:**
Como empleado de la casa de empeño, necesito poder iniciar sesión en el sistema con mi usuario y contraseña para acceder a las funciones que corresponden a mi rol (Administrador o Empleado). El sistema debe validar mis credenciales contra la base de datos local y emitir un token JWT de acceso junto con un token de refresco.

**Actor principal:** Empleado / Administrador

**Precondiciones:**
- El empleado debe estar registrado en la base de datos con estado `activo = true`
- La contraseña debe estar hasheada con BCrypt (strength 12)
- El servidor debe estar en ejecución y con conexión a PostgreSQL local
- Redis debe estar disponible para persistir la sesión activa

**Flujo principal:**
1. El empleado envía `POST /api/auth/login` con `{ "username": "...", "password": "..." }`
2. El sistema valida que los campos no estén vacíos
3. `AuthenticationManager` verifica las credenciales contra `UserDetailsServiceImpl`
4. Se valida que la cuenta esté activa (`enabled = true`)
5. `JwtService.generateAccessToken()` genera el token de acceso (TTL: 1 hora)
6. `JwtService.generateRefreshToken()` genera el token de refresco (TTL: 7 días)
7. La sesión activa se registra en Redis cache `sessions`
8. El sistema retorna `HTTP 200` con `AccessToken`, `RefreshToken`, y datos básicos del usuario
9. `AuditAspect` registra el evento `LOGIN_SUCCESS` en `audit_log`

**Flujos alternativos:**

*FA-01 — Credenciales incorrectas:*
1. En el paso 3, `BadCredentialsException` es lanzada
2. `GlobalExceptionHandler` intercepta y retorna `HTTP 401` con mensaje "Credenciales incorrectas."
3. `AuditAspect` registra `LOGIN_FAILED` con mensaje de error (sin exponer la contraseña)

*FA-02 — Cuenta desactivada:*
1. El usuario existe pero `enabled = false`
2. Spring Security lanza `DisabledException`
3. Retorna `HTTP 403` con mensaje "Cuenta desactivada. Contacte al administrador."

*FA-03 — Sistema offline (sin Redis):*
1. El login se completa con JWT sin persistencia en caché
2. Se loguea un warning: "Redis no disponible, sesión no cacheada"
3. El token es válido pero sin gestión de invalidación anticipada

**Reglas de negocio:**
- RN-01: El mismo usuario no puede tener más de 3 sesiones activas simultáneas
- RN-02: Después de 5 intentos fallidos consecutivos, la cuenta se bloquea por 15 minutos
- RN-03: Sólo empleados con `role = ADMIN` o `role = EMPLEADO` pueden iniciar sesión
- RN-04: El token de acceso no puede renovarse; sólo puede usarse el refresh token

**Validaciones:**
- `username`: no nulo, no vacío, máx 100 caracteres, sólo alfanuméricos y punto
- `password`: no nulo, no vacío, mín 8 caracteres

**Criterios de aceptación:**
- [x] `POST /api/auth/login` con credenciales válidas retorna `HTTP 200` con tokens
- [x] Credenciales inválidas retornan `HTTP 401` sin exponer información sensible
- [x] El token JWT puede decodificarse y contiene: `sub` (username), `iat`, `exp`
- [x] El refresh token tiene TTL de 7 días
- [x] Cada login exitoso genera registro en `audit_log`
- [x] La respuesta nunca incluye la contraseña hasheada

**Dependencias técnicas:**
- `JwtService` — generación de tokens
- `UserDetailsServiceImpl` — carga de usuario
- `BCryptPasswordEncoder` (strength=12)
- `AuditAspect` con anotación `@Auditable(operation="LOGIN")`
- Redis cache `sessions`
- Tabla `employees` (entidad a implementar)

**Consideraciones de seguridad:**
- El error de autenticación debe ser genérico (no revelar si el usuario existe)
- Las contraseñas nunca se loguean ni se incluyen en respuestas
- Los tokens deben transmitirse únicamente sobre HTTPS en producción
- El header `Authorization` debe ser `Bearer <token>` (case-sensitive)
- Implementar rate limiting: máx 10 requests/minuto por IP en `/auth/login`

**Manejo de excepciones:**

| Excepción | HTTP Status | Código de error |
|---|---|---|
| `BadCredentialsException` | 401 | `INVALID_CREDENTIALS` |
| `DisabledException` | 403 | `ACCOUNT_DISABLED` |
| `AccountExpiredException` | 403 | `ACCOUNT_EXPIRED` |
| `LockedException` | 423 | `ACCOUNT_LOCKED` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |

---

### HU-AUTH-02 — Renovación de token JWT (Refresh Token)

| Campo | Detalle |
|---|---|
| **Código** | HU-AUTH-02 |
| **Módulo** | Autenticación |
| **Prioridad** | 🔴 ALTA |
| **Estimación** | 3 puntos |

**Título:** Renovación silenciosa del token de acceso usando el refresh token

**Descripción funcional:**
Como sistema cliente (Desktop Swing o Web SPA), cuando el token de acceso expira (después de 1 hora), necesito poder obtener un nuevo token de acceso usando el refresh token sin requerir que el empleado ingrese nuevamente sus credenciales, garantizando una experiencia de uso continua y sin interrupciones.

**Actor principal:** Sistema cliente (Desktop/Web), operando en nombre del empleado autenticado

**Precondiciones:**
- El empleado tiene una sesión activa con un refresh token válido
- El refresh token no ha expirado (TTL: 7 días)
- El refresh token no ha sido revocado manualmente (logout)
- El empleado sigue activo en el sistema

**Flujo principal:**
1. El cliente detecta `HTTP 401` en cualquier request (token de acceso expirado)
2. El cliente envía `POST /api/auth/refresh` con el refresh token en el header `X-Refresh-Token`
3. `JwtService.extractUsername()` extrae el username del refresh token
4. Se verifica que el tipo de claim sea `"type": "refresh"`
5. `UserDetailsService.loadUserByUsername()` carga el empleado actual
6. Se verifica que la cuenta siga activa
7. Se genera un nuevo access token con TTL de 1 hora
8. (Opcional) Se emite un nuevo refresh token (sliding window)
9. Retorna `HTTP 200` con el nuevo `AccessToken` y opcionalmente el nuevo `RefreshToken`

**Flujos alternativos:**

*FA-01 — Refresh token expirado:*
1. `JwtService.isTokenExpired()` retorna `true`
2. Retorna `HTTP 401` con código `REFRESH_TOKEN_EXPIRED`
3. El cliente debe redirigir al empleado al formulario de login

*FA-02 — Token de tipo incorrecto (access token en lugar de refresh):*
1. El claim `type` no es `"refresh"`
2. Retorna `HTTP 401` con código `INVALID_TOKEN_TYPE`

*FA-03 — Empleado fue desactivado durante la sesión:*
1. El token es válido pero `employee.enabled = false`
2. Retorna `HTTP 403` con código `ACCOUNT_DISABLED`
3. El cliente debe cerrar la sesión local

**Reglas de negocio:**
- RN-01: Un refresh token sólo puede usarse una vez si se implementa rotación de tokens
- RN-02: Al revocar un refresh token (logout), todos los tokens derivados son inválidos
- RN-03: El nuevo access token tiene el mismo username y roles que el token original
- RN-04: No se puede usar un access token como refresh token

**Validaciones:**
- `X-Refresh-Token` header: no nulo, formato JWT válido
- El token debe tener claim `type = "refresh"`
- El token no debe estar expirado
- El username extraído debe existir en la base de datos

**Criterios de aceptación:**
- [x] `POST /api/auth/refresh` con refresh token válido retorna nuevo access token en `HTTP 200`
- [x] Refresh token expirado retorna `HTTP 401` con mensaje informativo
- [x] Access token enviado como refresh token retorna `HTTP 401`
- [x] Nuevo access token tiene TTL de 1 hora desde la emisión
- [x] El empleado desactivado no puede renovar tokens

**Dependencias técnicas:**
- `JwtService` — validación y extracción de claims
- `UserDetailsServiceImpl` — verificación de estado activo
- Header personalizado `X-Refresh-Token` (configurado en `CorsConfig.exposedHeaders`)

**Consideraciones de seguridad:**
- Los refresh tokens deben almacenarse de forma segura en el cliente (HttpOnly cookie o keychain)
- Implementar token rotation: cada uso del refresh token genera uno nuevo e invalida el anterior
- Considerar lista negra en Redis para tokens revocados antes de su expiración natural
- No incluir información sensible en los claims del token

**Manejo de excepciones:**

| Excepción | HTTP Status | Código de error |
|---|---|---|
| Token expirado | 401 | `REFRESH_TOKEN_EXPIRED` |
| Token inválido/malformado | 401 | `INVALID_TOKEN` |
| Tipo de token incorrecto | 401 | `INVALID_TOKEN_TYPE` |
| Empleado desactivado | 403 | `ACCOUNT_DISABLED` |
| Header ausente | 400 | `MISSING_REFRESH_TOKEN` |

---

### HU-AUTH-03 — Cierre de sesión (Logout)

| Campo | Detalle |
|---|---|
| **Código** | HU-AUTH-03 |
| **Módulo** | Autenticación |
| **Prioridad** | 🟠 MEDIA |
| **Estimación** | 2 puntos |

**Título:** Cierre de sesión seguro con invalidación de tokens activos

**Descripción funcional:**
Como empleado autenticado, necesito poder cerrar sesión de forma segura para que mis tokens sean invalidados inmediatamente, protegiendo el acceso al sistema cuando termino mi turno o cuando abandono un equipo compartido. El cierre de sesión debe funcionar aunque no haya conexión a internet.

**Actor principal:** Empleado autenticado (cualquier rol)

**Precondiciones:**
- El empleado tiene una sesión activa con un access token válido
- El token es enviado en el header `Authorization: Bearer <token>`

**Flujo principal:**
1. El empleado envía `POST /api/auth/logout` con el access token en `Authorization` header
2. `JwtAuthenticationFilter` valida y autentica el token normalmente
3. `JwtService.extractUsername()` obtiene el username
4. Se elimina la entrada de la sesión en Redis cache `sessions:<username>`
5. (Si se implementa blacklist) El token se agrega a la lista negra de Redis hasta su expiración natural
6. Se invalida el refresh token asociado si fue enviado en `X-Refresh-Token`
7. `SecurityContextHolder.clearContext()` limpia el contexto de seguridad
8. Retorna `HTTP 204 No Content`
9. `AuditAspect` registra el evento `LOGOUT` en `audit_log`

**Flujos alternativos:**

*FA-01 — Token ya expirado al hacer logout:*
1. El token está expirado pero la solicitud de logout es válida semánticamente
2. El sistema procesa el logout igualmente (idempotente)
3. Retorna `HTTP 204` sin error

*FA-02 — Sin conexión a Redis:*
1. La eliminación de caché falla silenciosamente (Redis no disponible)
2. El logout se registra localmente en log
3. El token expirará naturalmente según su TTL
4. Retorna `HTTP 204` con warning en logs internos

*FA-03 — Logout forzado por administrador:*
1. El administrador envía `POST /api/auth/logout/{employeeId}` con su propio token (ADMIN)
2. El sistema invalida todos los tokens activos del empleado indicado
3. Retorna `HTTP 200` con confirmación

**Reglas de negocio:**
- RN-01: El logout es una operación idempotente (hacer logout dos veces no da error)
- RN-02: Un administrador puede hacer logout forzado de cualquier empleado
- RN-03: Al hacer logout, se invalidan tanto el access token como el refresh token
- RN-04: El sistema funciona en modo offline: el logout local es suficiente si Redis no está disponible

**Validaciones:**
- El header `Authorization` debe estar presente y ser un Bearer token válido
- El token debe pertenecer al empleado que hace la solicitud (o ser admin para logout forzado)

**Criterios de aceptación:**
- [x] `POST /api/auth/logout` con token válido retorna `HTTP 204`
- [x] Después del logout, el mismo token no puede usarse para autenticar (blacklist)
- [x] El logout es idempotente (múltiples llamadas no causan error)
- [x] El logout queda registrado en `audit_log`
- [x] Admin puede hacer logout forzado de otro empleado

**Dependencias técnicas:**
- Redis cache `sessions` para invalidación inmediata
- `JwtService` para extracción del token
- `AuditAspect` con `@Auditable(operation="LOGOUT")`
- (Futuro) Lista negra de tokens en Redis

**Consideraciones de seguridad:**
- El endpoint debe requerir autenticación (no es público)
- Tras el logout, el cliente debe eliminar los tokens de almacenamiento local
- Implementar logout total (invalidar todos los dispositivos) como funcionalidad adicional
- No revelar en la respuesta si el token era válido o no (prevenir oracle de tokens)

**Manejo de excepciones:**

| Excepción | HTTP Status | Código de error |
|---|---|---|
| Token ausente | 401 | `UNAUTHORIZED` |
| Acceso denegado (logout de otro usuario sin ser admin) | 403 | `FORBIDDEN` |
| Error interno en invalidación | 500 | `INTERNAL_ERROR` (el logout se completa igualmente) |

---

### HU-AUTH-04 — Control de acceso por roles (Autorización)

| Campo | Detalle |
|---|---|
| **Código** | HU-AUTH-04 |
| **Módulo** | Autenticación / Autorización |
| **Prioridad** | 🔴 ALTA |
| **Estimación** | 4 puntos |

**Título:** Protección de endpoints por rol con `@PreAuthorize` y `@EnableMethodSecurity`

**Descripción funcional:**
Como sistema, necesito garantizar que cada endpoint de la API sólo sea accesible por empleados con el rol apropiado, de manera que un empleado regular no pueda acceder a funciones administrativas (gestión de empleados, configuración del sistema, reportes completos), mientras que un administrador tiene acceso total al sistema.

**Actor principal:** Sistema (Spring Security)

**Actores secundarios:** Empleado (rol EMPLEADO), Administrador (rol ADMIN)

**Precondiciones:**
- El empleado está autenticado con un JWT válido
- El token incluye las autoridades/roles del usuario en los claims o se cargan desde la BD
- `@EnableMethodSecurity(prePostEnabled = true)` está activo en `SecurityConfig`

**Modelo de roles:**

| Rol | Descripción | Permisos |
|---|---|---|
| `ROLE_ADMIN` | Administrador del sistema | Acceso total: empleados, reportes, configuración, auditoría |
| `ROLE_EMPLEADO` | Empleado de mostrador | Artículos, empeños, ventas, clientes (sin gestión de usuarios) |

**Matriz de permisos por endpoint:**

| Endpoint | ADMIN | EMPLEADO | ANÓNIMO |
|---|---|---|---|
| `POST /auth/login` | ✅ | ✅ | ✅ |
| `POST /auth/refresh` | ✅ | ✅ | ✅ |
| `POST /auth/logout` | ✅ | ✅ | ❌ |
| `GET /articles` | ✅ | ✅ | ❌ |
| `POST /articles` | ✅ | ✅ | ❌ |
| `DELETE /articles/{id}` | ✅ | ❌ | ❌ |
| `GET /pawns` | ✅ | ✅ | ❌ |
| `POST /pawns` | ✅ | ✅ | ❌ |
| `PUT /pawns/{id}/approve` | ✅ | ❌ | ❌ |
| `GET /employees` | ✅ | ❌ | ❌ |
| `POST /employees` | ✅ | ❌ | ❌ |
| `GET /audit-logs` | ✅ | ❌ | ❌ |
| `GET /dashboard` | ✅ | ✅ (limitado) | ❌ |

**Flujo principal:**
1. El cliente envía request con `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter` valida el token y puebla `SecurityContextHolder`
3. Spring Security ejecuta el chain de autorización
4. `@PreAuthorize("hasRole('ADMIN')")` en el método del controller evalúa el rol
5. Si tiene el rol requerido: la request procede normalmente
6. Si no tiene el rol: Spring lanza `AccessDeniedException`
7. `GlobalExceptionHandler.handleAccessDenied()` captura y retorna `HTTP 403`

**Flujos alternativos:**

*FA-01 — Token válido pero sin rol suficiente:*
1. El token es auténtico y no expirado
2. El empleado intenta acceder a un endpoint restringido a ADMIN
3. `AccessDeniedException` → `HTTP 403 Forbidden`
4. El mensaje no revela qué rol es necesario (seguridad por obscuridad)

*FA-02 — Token ausente o inválido:*
1. No hay header `Authorization` o el token es inválido
2. El filtro no autentica, la request llega sin autenticación al chain
3. `HTTP 401 Unauthorized`

*FA-03 — Acceso a datos de otro empleado:*
1. Un empleado intenta acceder al historial de otro empleado
2. El endpoint verifica `SecurityContextHelper.getCurrentUsername()` vs el recurso solicitado
3. Si no coincide y no es ADMIN: `HTTP 403`

**Reglas de negocio:**
- RN-01: Los roles son mutuamente exclusivos (un empleado tiene exactamente un rol)
- RN-02: Los permisos se evalúan en tiempo de request, no en el token (el token sólo lleva el rol)
- RN-03: Un empleado desactivado pierde acceso inmediatamente (no espera a que expire el token)
- RN-04: El administrador puede ver y hacer todo lo que un empleado puede hacer
- RN-05: Las operaciones de borrado físico son exclusivas del rol ADMIN

**Validaciones:**
- El rol debe ser exactamente `ROLE_ADMIN` o `ROLE_EMPLEADO` (case-sensitive en Spring Security)
- Un empleado no puede escalar privilegios manipulando el token (el secreto JWT garantiza esto)

**Criterios de aceptación:**
- [x] Empleado con `ROLE_ADMIN` puede acceder a todos los endpoints protegidos
- [x] Empleado con `ROLE_EMPLEADO` recibe `HTTP 403` al intentar acceder a endpoints de admin
- [x] Request sin token recibe `HTTP 401` en endpoints protegidos
- [x] El mensaje de error `HTTP 403` no revela el rol requerido
- [x] Los public endpoints (`/auth/login`, `/swagger-ui/**`) son accesibles sin token
- [x] La anotación `@PreAuthorize` funciona correctamente en los controllers

**Implementación de ejemplo en controller:**
```java
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<EmployeeResponse>> findAll(Pageable pageable) {
        // Sólo ADMIN puede listar empleados
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        // ADMIN puede ver cualquiera; empleado sólo puede verse a sí mismo
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid CreateEmployeeRequest req) {
        // Sólo ADMIN puede crear empleados
    }
}
```

**Dependencias técnicas:**
- `SecurityConfig` con `@EnableMethodSecurity(prePostEnabled = true)`
- `CustomUserDetails` con `getAuthorities()` correcto
- `GlobalExceptionHandler` para `AccessDeniedException`

**Consideraciones de seguridad:**
- Los roles deben verificarse en el servidor, nunca confiar en el cliente
- No exponer en errores qué roles son necesarios (mensaje genérico "No tiene permisos")
- Considerar implementar `@PostFilter` para filtrar listas de recursos por propietario
- Auditar todos los intentos de acceso no autorizado (403)

**Manejo de excepciones:**

| Excepción | HTTP Status | Código de error |
|---|---|---|
| `AccessDeniedException` | 403 | `FORBIDDEN` |
| Sin autenticación | 401 | `UNAUTHORIZED` |
| Token de rol manipulado (JwtException) | 401 | `INVALID_TOKEN` |

---

## 6. CONFIGURACIÓN DOCKER OPTIMIZADA

### 6.1 Análisis del Dockerfile actual

**Problema crítico con el Dockerfile existente:**
```dockerfile
# ACTUAL — Completamente inadecuado para producción
FROM ubuntu:latest      # ❌ ubuntu:latest es inestable y masivo (~29MB)
LABEL authors="Admin"
ENTRYPOINT ["top", "-b"]  # ❌ Solo ejecuta 'top' — no inicia la aplicación Java
```

### 6.2 Dockerfile corregido — Backend Spring Boot

```dockerfile
# ── Build stage ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de configuración del build primero (mejor uso de caché Docker)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src/ src/
RUN ./mvnw package -DskipTests -B \
    && java -Djarmode=layertools -jar target/*.jar extract

# ── Runtime stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar capas del JAR en orden de menor a mayor cambio (optimización de caché)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Directorio de logs
RUN mkdir -p /app/logs && chown appuser:appgroup /app/logs

# Cambiar a usuario no-root
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local}", \
    "org.springframework.boot.loader.launch.JarLauncher"]
```

### 6.3 docker-compose.yml — Entorno completo optimizado

```yaml
# ════════════════════════════════════════════════════════════════════════════
# CompraVenta — Docker Compose
# PostgreSQL y pgAdmin son REMOTOS (Supabase/Cloud) — no se definen aquí
# Este compose levanta: Backend API + Redis + (opcional) Nginx
# ════════════════════════════════════════════════════════════════════════════
version: "3.9"

# ── Redes ─────────────────────────────────────────────────────────────────────
networks:
  compraventa-net:
    driver: bridge
    name: compraventa-net

# ── Volúmenes ─────────────────────────────────────────────────────────────────
volumes:
  redis-data:
    name: compraventa-redis-data
  app-logs:
    name: compraventa-logs

# ── Servicios ─────────────────────────────────────────────────────────────────
services:

  # ── Redis Cache ─────────────────────────────────────────────────────────────
  redis:
    image: redis:7.2-alpine
    container_name: compraventa-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD}
      --maxmemory 256mb
      --maxmemory-policy allkeys-lru
      --appendonly yes
      --appendfilename redis-aof.aof
    ports:
      - "127.0.0.1:6379:6379"      # Solo accesible localmente, no expuesto externamente
    volumes:
      - redis-data:/data
    networks:
      - compraventa-net
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s

  # ── Backend Spring Boot ──────────────────────────────────────────────────────
  backend:
    build:
      context: .
      dockerfile: Dockerfile
      target: runtime
    image: compraventa-backend:latest
    container_name: compraventa-backend
    restart: unless-stopped
    depends_on:
      redis:
        condition: service_healthy
    environment:
      # Perfil de Spring
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-local}

      # Base de datos PostgreSQL LOCAL (el driver se conecta directamente, sin Docker)
      LOCAL_DB_URL: ${LOCAL_DB_URL}
      LOCAL_DB_USER: ${LOCAL_DB_USER}
      LOCAL_DB_PASSWORD: ${LOCAL_DB_PASSWORD}

      # Redis
      REDIS_HOST: redis          # Nombre del servicio Docker
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}

      # JWT
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION_MS: ${JWT_EXPIRATION_MS:-3600000}
      JWT_REFRESH_EXPIRATION_MS: ${JWT_REFRESH_EXPIRATION_MS:-604800000}

      # Supabase Sync
      SUPABASE_URL: ${SUPABASE_URL}
      SUPABASE_ANON_KEY: ${SUPABASE_ANON_KEY}
      SUPABASE_SERVICE_ROLE_KEY: ${SUPABASE_SERVICE_ROLE_KEY}
      SYNC_SIGNING_SECRET: ${SYNC_SIGNING_SECRET}
      SYNC_ENABLED: ${SYNC_ENABLED:-true}

      # CORS
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:3000}

      # JVM tuning
      JAVA_OPTS: >-
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -Djava.security.egd=file:/dev/./urandom
    ports:
      - "8080:8080"
    volumes:
      - app-logs:/app/logs
    networks:
      - compraventa-net
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider",
             "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      start_period: 60s
      retries: 3

  # ── Nginx Reverse Proxy (opcional, para producción) ──────────────────────────
  nginx:
    image: nginx:1.25-alpine
    container_name: compraventa-nginx
    restart: unless-stopped
    profiles:
      - production          # Solo activo con: docker compose --profile production up
    depends_on:
      backend:
        condition: service_healthy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    networks:
      - compraventa-net
```

### 6.4 Archivo `.env.example` — Variables de entorno documentadas

```bash
# ══════════════════════════════════════════════════════════════════════════════
# CompraVenta Backend — Variables de Entorno
# COPIAR como .env y completar los valores reales
# NUNCA commitear el archivo .env al repositorio
# ══════════════════════════════════════════════════════════════════════════════

# ── Perfil de Spring ─────────────────────────────────────────────────────────
SPRING_PROFILES_ACTIVE=local
# Opciones: local | production

# ── PostgreSQL LOCAL ──────────────────────────────────────────────────────────
# La base de datos PostgreSQL se administra remotamente (Supabase/Cloud)
# Esta URL apunta a la instancia local o a la conexión directa
LOCAL_DB_URL=jdbc:postgresql://localhost:5432/compraventa
LOCAL_DB_USER=compraventa_user
LOCAL_DB_PASSWORD=CHANGE_ME_STRONG_PASSWORD_HERE

# ── Redis ─────────────────────────────────────────────────────────────────────
REDIS_PASSWORD=CHANGE_ME_REDIS_PASSWORD

# ── JWT Security ──────────────────────────────────────────────────────────────
# Generar con: openssl rand -base64 64
JWT_SECRET=CHANGE_ME_MINIMUM_64_CHARACTER_SECRET_KEY_FOR_HMAC_SHA256_SECURITY
JWT_EXPIRATION_MS=3600000       # 1 hora en ms
JWT_REFRESH_EXPIRATION_MS=604800000  # 7 días en ms

# ── Supabase (Sincronización) ─────────────────────────────────────────────────
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here
SYNC_SIGNING_SECRET=CHANGE_ME_SYNC_SECRET
SYNC_ENABLED=true

# ── CORS ──────────────────────────────────────────────────────────────────────
# Separar múltiples origins con coma
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# ── Servidor ─────────────────────────────────────────────────────────────────
SERVER_PORT=8080
```

### 6.5 `.dockerignore` — Excluir archivos innecesarios

```
# Build artifacts
target/
*.jar
*.war

# IDE files
.idea/
.vscode/
*.iml
*.iws

# Secrets y configuración local
.env
.env.*
!.env.example

# Logs
*.log
logs/

# Tests
src/test/

# Git
.git/
.gitignore

# Docker
Dockerfile
docker-compose*.yml
```

---

## 7. RECOMENDACIONES DE SEGURIDAD

### 7.1 SQL Injection

**Estado actual:** ✅ Protegido — Spring Data JPA usa Prepared Statements por defecto

**Verificación adicional requerida para queries nativas:**
```java
// ❌ NUNCA hacer esto con queries nativas
@Query(value = "SELECT * FROM employees WHERE username = '" + username + "'", nativeQuery = true)

// ✅ Siempre usar parámetros nombrados
@Query(value = "SELECT * FROM employees WHERE username = :username", nativeQuery = true)
public Optional<Employee> findByUsername(@Param("username") String username);
```

### 7.2 Exposición de credenciales

**Acciones requeridas:**
1. Agregar `.env` al `.gitignore` (debe estar allí ANTES del primer commit)
2. Usar `@JsonIgnore` en campos de contraseña en entidades
3. Nunca loguear el header `Authorization` completo
4. Usar variables de entorno para TODOS los secretos (ver `.env.example`)

### 7.3 Seguridad del JWT

```java
// Longitud mínima del secreto JWT para HMAC-SHA256: 256 bits = 32 bytes
// Recomendado: 512 bits = 64 bytes (base64)
// Generar con: openssl rand -base64 64
@Value("${jwt.secret}")
private String secret;

// Validar longitud en @PostConstruct:
@PostConstruct
public void validateSecret() {
    if (secret.length() < 32) {
        throw new IllegalStateException(
            "JWT secret must be at least 32 characters. Current length: " + secret.length()
        );
    }
}
```

### 7.4 Rate Limiting (no implementado — requerido)

Agregar dependencia y configuración:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

```java
// Configuración de rate limiting para endpoints de auth
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {

        if (request.getRequestURI().contains("/auth/login")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = cache.computeIfAbsent(ip, this::newBucket);

            if (!bucket.tryConsume(1)) {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("{\"error\":\"Demasiados intentos. Espere 1 minuto.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private Bucket newBucket(String ip) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();
    }
}
```

### 7.5 Headers de seguridad HTTP

Agregar a `SecurityConfig`:
```java
.headers(headers -> headers
    .frameOptions(frame -> frame.sameOrigin())
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
    .xssProtection(xss -> xss.disable())  // CSP es más moderno que X-XSS-Protection
)
```

### 7.6 Checklist de seguridad para producción

```
□ JWT_SECRET tiene mínimo 64 caracteres (generado con openssl)
□ .env está en .gitignore antes del primer push
□ Contraseñas de PostgreSQL no están en código fuente
□ Redis tiene contraseña configurada
□ CORS permite sólo origins conocidos (no "*")
□ Rate limiting activo en /auth/login
□ HTTPS obligatorio en producción (no HTTP)
□ Logs no contienen tokens JWT ni contraseñas
□ Usuarios de BD con permisos mínimos (no usar superuser)
□ Auditoría activa en operaciones sensibles
□ Flyway migrations en lugar de ddl-auto=create
□ Headers de seguridad HTTP configurados
□ Actuator endpoints protegidos (no exponer /actuator/* públicamente)
```

---

## 8. ESTRUCTURA FINAL PROPUESTA DEL PROYECTO

```
Backend/
├── src/
│   ├── main/
│   │   ├── java/com/CompraVenta/Backend/
│   │   │   │
│   │   │   ├── 📁 Audit/                    ← Auditoría transversal
│   │   │   │   ├── annotation/Auditable.java
│   │   │   │   ├── aspect/AuditAspect.java  ← IMPLEMENTAR
│   │   │   │   ├── entity/AudLog.java
│   │   │   │   └── repository/AuditRepository.java  ← IMPLEMENTAR
│   │   │   │
│   │   │   ├── 📁 Config/                   ← Configuración técnica
│   │   │   │   ├── CorsConfig.java          ← CORREGIR typo @Value
│   │   │   │   ├── DataSourceConfig.java    ← RENOMBRAR DateSorceConfig
│   │   │   │   ├── JacksonConfig.java       ← RENOMBRAR JackSonConfig
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── RedisConfig.java         ← CORREGIR parámetro no usado
│   │   │   │   └── SchedulingConfig.java
│   │   │   │
│   │   │   ├── 📁 Exception/                ← Manejo global de errores
│   │   │   │   ├── custom/
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   ├── ResourceNotFoundException.java  ← IMPLEMENTAR extends RuntimeException
│   │   │   │   │   └── UnauthorizedException.java
│   │   │   │   └── handler/
│   │   │   │       └── GlobalExceptionHandler.java    ← RENOMBRAR GlobalEceptionHandler
│   │   │   │
│   │   │   ├── 📁 Security/                 ← Seguridad y JWT
│   │   │   │   ├── context/SecurityContextHelper.java  ← IMPLEMENTAR (renombrar SecurityContext)
│   │   │   │   ├── filter/JwtAuthenticationFilter.java
│   │   │   │   ├── model/CustomUserDetails.java        ← IMPLEMENTAR
│   │   │   │   └── service/
│   │   │   │       ├── JwtService.java                 ← CORREGIR métodos duplicados
│   │   │   │       └── UserDetailsServiceImpl.java     ← IMPLEMENTAR
│   │   │   │
│   │   │   ├── 📁 Shared/                   ← Componentes compartidos
│   │   │   │   ├── constants/AppConstants.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   ├── ErrorDetail.java
│   │   │   │   │   └── PageResponse.java
│   │   │   │   ├── entity/BaseEntity.java   ← CORREGIR columna "delete" → "is_deleted"
│   │   │   │   └── enums/Role.java          ← CORREGIR Admin→ADMIN, Empleado→EMPLEADO
│   │   │   │
│   │   │   ├── 📁 Sync/                     ← Motor de sincronización offline
│   │   │   │   ├── SyncOutbox.java          ← CORREGIR "paypload" → "payload"
│   │   │   │   ├── SyncStatus.java
│   │   │   │   ├── service/SyncService.java          ← IMPLEMENTAR
│   │   │   │   └── scheduler/SyncScheduler.java      ← IMPLEMENTAR
│   │   │   │
│   │   │   ├── 📁 Auth/                     ← Módulo de autenticación (NUEVO)
│   │   │   │   ├── controller/AuthController.java
│   │   │   │   ├── service/AuthService.java (interface)
│   │   │   │   ├── service/AuthServiceImpl.java
│   │   │   │   └── dto/
│   │   │   │       ├── LoginRequest.java
│   │   │   │       ├── LoginResponse.java
│   │   │   │       └── RefreshTokenRequest.java
│   │   │   │
│   │   │   ├── 📁 Employees/                ← Módulo empleados (NUEVO)
│   │   │   │   ├── controller/EmployeeController.java
│   │   │   │   ├── service/EmployeeService.java
│   │   │   │   ├── service/EmployeeServiceImpl.java
│   │   │   │   ├── repository/EmployeeRepository.java
│   │   │   │   ├── entity/Employee.java
│   │   │   │   └── dto/
│   │   │   │       ├── CreateEmployeeRequest.java
│   │   │   │       └── EmployeeResponse.java
│   │   │   │
│   │   │   ├── 📁 Articles/                 ← Módulo artículos (NUEVO)
│   │   │   ├── 📁 Pawns/                    ← Módulo empeños (NUEVO)
│   │   │   ├── 📁 Clients/                  ← Módulo clientes (NUEVO)
│   │   │   ├── 📁 Sales/                    ← Módulo ventas (NUEVO)
│   │   │   └── BackendApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-production.yml
│   │       └── db/
│   │           └── migration/              ← Scripts Flyway (CREAR)
│   │               ├── V1__create_base_schema.sql
│   │               ├── V2__create_employees.sql
│   │               ├── V3__create_articles.sql
│   │               ├── V4__create_pawns.sql
│   │               ├── V5__create_sync_outbox.sql
│   │               └── V6__create_audit_log.sql
│   │
│   └── test/
│       └── java/com/CompraVenta/Backend/
│           ├── Auth/AuthControllerTest.java
│           ├── Security/JwtServiceTest.java
│           └── Sync/SyncServiceTest.java
│
├── nginx/
│   └── nginx.conf                          ← Config de proxy inverso
├── Dockerfile                              ← REEMPLAZAR completamente
├── docker-compose.yml                      ← NUEVO — completo
├── .env.example                            ← NUEVO — documentado
├── .dockerignore                           ← NUEVO
└── .gitignore                              ← Incluir .env
```

---

## 9. COMPATIBILIDAD DESKTOP / WEB / API

### 9.1 Estrategia de comunicación por plataforma

| Plataforma | Protocolo | Auth | Offline | Sincronización |
|---|---|---|---|---|
| Desktop Swing | HTTP/REST | JWT Bearer | ✅ Nativo | SyncOutbox → Supabase |
| Web SPA | HTTP/REST | JWT Bearer | Service Worker | Automática por API |
| Móvil (futuro) | HTTP/REST | JWT Bearer | Cache local | Automática por API |

### 9.2 Consideraciones para el cliente Desktop (Swing)

El cliente Desktop debe implementar:

```
Desktop Swing Architecture:
┌─────────────────────────────────────────────────────────────┐
│                    Java Swing Application                    │
│                                                             │
│  ┌──────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │  UI Layer    │  │  Controller Layer│  │  Sync Engine │  │
│  │  (JPanel,    │  │  (SwingWorker   │  │  (Outbox     │  │
│  │   JFrame)    │  │   para async)   │  │   Pattern)   │  │
│  └──────┬───────┘  └────────┬────────┘  └──────┬───────┘  │
│         └──────────────────►│◄───────────────────┘         │
│                             │                               │
│  ┌──────────────────────────▼──────────────────────────┐   │
│  │                  Service Layer                        │   │
│  │  ApiClient (OkHttp/HttpClient) — JWT interceptor     │   │
│  │  LocalDb (SQLite/H2) — datos offline                 │   │
│  │  TokenManager — refresh automático                   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         │ Online                         │ Offline
         ▼                                ▼
  Backend REST API                 Local SQLite/H2
  (Spring Boot)                    (sincronización posterior)
```

### 9.3 Manejo offline-first

La arquitectura Outbox Pattern ya está parcialmente definida:
- `SyncOutbox` entity existe con estados `PENDING, SYNCING, SYNCED, CONFLICT, FAILED`
- El scheduler en `SchedulingConfig` puede ejecutar la sincronización cada N segundos
- La estrategia `remote-wins` está configurada en `application.yml`

**Flujo de sincronización:**
```
Operación local → Guardar en PostgreSQL local → Agregar a sync_outbox (PENDING)
                                                        ↓
              Scheduler cada 30s → Detectar PENDING → Intentar sync con Supabase
                                                        ↓
                                           ✅ SYNCED | ❌ FAILED → retry
                                                        ↓
                                   Si CONFLICT → aplicar estrategia remote-wins
```

---

## 10. ROADMAP DE MEJORAS

### Sprint 1 — Correcciones críticas (1-2 semanas)

| # | Tarea | Prioridad |
|---|---|---|
| 1 | ~~Corregir `ResourceNotFoundException` (extends RuntimeException)~~ | ✅ COMPLETADO |
| 2 | ~~Implementar `UserDetailsServiceImpl` con Employee entity~~ | ✅ COMPLETADO |
| 3 | ~~Implementar `AuditRepository` e `AuditAspect`~~ | ✅ COMPLETADO |
| 4 | ~~Corregir typo `@Value("${cors.allowed-methods")`~~ | ✅ COMPLETADO |
| 5 | ~~Implementar `CustomUserDetails`~~ | ✅ COMPLETADO |
| 6 | ~~Crear `Employee` entity + repository + migration Flyway~~ | ✅ COMPLETADO |
| 7 | ~~Crear `AuthController` + `AuthServiceImpl`~~ | ✅ COMPLETADO |
| 8 | ~~Corregir `ApiResponse.ErrorDetail` vs `ErrorDetail` externo~~ | ✅ COMPLETADO |

### Sprint 2 — Estabilización (2-3 semanas)

| # | Tarea | Prioridad |
|---|---|---|
| 9 | ~~Renombrar clases con typos (DateSorceConfig, etc.)~~ | ✅ COMPLETADO |
| 10 | ~~Implementar `SecurityContext` (helper de usuario autenticado)~~ | ✅ COMPLETADO |
| 11 | ~~Corrección columna `"delete"` → `"is_deleted"` en BaseEntity~~ | ✅ COMPLETADO |
| 12 | ~~Unificar métodos `generateAccesoToken`/`generateAccessoToken`~~ | ✅ COMPLETADO |
| 13 | ~~Corregir Role enum a UPPER_CASE~~ | ✅ COMPLETADO |
| 14 | **Implementar `SyncService` + `SyncScheduler`** | 🟠 Alto — **sigue pendiente** |
| 15 | ~~Crear Dockerfile y docker-compose optimizados~~ | ✅ COMPLETADO |

### Sprint 3 — Módulos de negocio (3-4 semanas)

| # | Módulo | HUs relacionadas |
|---|---|---|
| 16 | ~~Módulo Articles (CRUD completo)~~ | ✅ HU-ART-01 a HU-ART-05 |
| 17 | ~~Módulo Clients~~ | ✅ HU-CLI-01 |
| 18 | ~~Módulo Pawns (Empeños)~~ | ✅ HU-PAW-01 a HU-PAW-07 |
| 19 | ~~Módulo Sales~~ | ✅ HU-SAL-01 a HU-SAL-03 |
| 19b | ~~Módulo Purchases~~ | ✅ HU-PUR-01 |
| 20 | Dashboard/Reportes | HU-DASH-01 a HU-DASH-03 — pendiente |

### Sprint 4 — Calidad y producción (2 semanas)

| # | Tarea |
|---|---|
| 21 | Tests unitarios con JUnit 5 + Mockito (cobertura mínima 70%) |
| 22 | Tests de integración con Testcontainers |
| 23 | Rate limiting con Bucket4j |
| 24 | Security headers HTTP |
| 25 | CI/CD pipeline (GitHub Actions) |
| 26 | Configuración Nginx para producción |

---

## APÉNDICE — Resumen de correcciones inmediatas requeridas

| Archivo | Tipo | Descripción del problema |
|---|---|---|
| `ResourceNotFoundException.java` | ✅ COMPLETADO | ~~No extiende `RuntimeException`~~ |
| `UserDetailsServiceImpl.java` | ✅ COMPLETADO | ~~Clase vacía — Spring Security falla en runtime~~ |
| `AudiAspect.java` | ✅ COMPLETADO | ~~Aspecto vacío — `@Auditable` es inerte~~ |
| `AuditRepository.java` | ✅ COMPLETADO | ~~No es `JpaRepository` — no puede inyectarse~~ |
| `CustomUserDetails.java` | ✅ COMPLETADO | ~~Vacío — `JwtFilter` no puede construir UserDetails~~ |
| `CorsConfig.java` (línea 20) | ✅ COMPLETADO | ~~Typo `${cors.allowed-methods` falta `}` → fallo de arranque~~ |
| `GlobalExceptionHandler.java` | ✅ COMPLETADO | ~~Usa `ApiResponse.ErrorDetail` que no existe como inner class~~ |
| `BaseEntity.java` | ✅ COMPLETADO | ~~Columna `"delete"` es palabra reservada SQL~~ |
| `AudLog.java` | ✅ COMPLETADO | ~~Typo `emplooyeeId` vs columna `emplooyee_id`~~ |
| `SyncOutbox.java` | ✅ COMPLETADO | ~~Typo `paypload` en campo y columna~~ |
| `JwtService.java` | ✅ COMPLETADO | ~~Dos métodos con nombres similares incorrectos~~ |
| `Role.java` | ✅ COMPLETADO | ~~Convención UPPER_CASE no cumplida~~ |
| `DateSorceConfig.java` | ✅ COMPLETADO | ~~Nombre de clase con typo~~ |
| `GlobalEceptionHandler.java` | ✅ COMPLETADO | ~~Nombre de clase con typo~~ |
| `RedisConfig.java` | 🟡 MEDIO | **Parámetro `RestClient.Builder` no usado** |
| `Dockerfile` | ✅ COMPLETADO | ~~Sólo ejecuta `top -b`, no inicia la aplicación Java~~ |

---

*Documento generado el 2026-05-26 | Actualizado 2026-09-01 (módulos de dominio completos, Purchases incluido)*
*Siguiente foco: Sync Engine y tests*
