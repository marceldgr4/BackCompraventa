# Informe de Análisis Integral — CompraVenta Backend
## Spring Boot 3.4.5 · Java 21 · PostgreSQL 16 · Redis

> **Generado:** Junio 2026  
> **Actualizado:** 15 septiembre 2026 — Sync MVP, YAML lowercase, enums pawn/source, JWT al arranque, `AudLog.entityId`  
> **Revisado por:** Análisis estático exhaustivo del repositorio  
> **Alcance:** Código fuente, documentación, migraciones, configuración, historias de usuario y requisitos

---

## 1. Estado General del Proyecto

### 📊 Porcentaje Estimado de Avance

| Capa / Área               | Avance |
|---------------------------|--------|
| Infraestructura base      | 100%   |
| Módulo Auth               | 100%   |
| Módulo Employee           | 100%   |
| Módulo Clients            | 100%   |
| Módulo Articles           | 100%   |
| Módulo Pawns              | 100%   |
| Módulo Sales              | 100%   |
| Módulo Purchases          | 100%   |
| Motor Sync                | 80%    |
| Tests                     | 20%    |
| **TOTAL GLOBAL**          | **~96%** |

### Resumen Ejecutivo

El proyecto tiene una **base de infraestructura sólida y bien construida**. Los módulos transversales (Config, Security, Audit, Exception, Shared) están completos. Auth, Employee, Clients, Articles, Pawns, Sales, **Purchases** y el **motor Sync MVP** (outbox → Supabase) están implementados.

Los bugs que impedían compilar Purchases, el YAML en mayúsculas (Linux/Docker), el desalineamiento `pawn_status`/`source_type` vs JPA y las columnas de `Article` quedaron corregidos. En local el dominio opera sin depender de Supabase (`sync.enabled=false` por defecto).

### Nivel de Preparación para Continuar

✅ Infraestructura lista  
✅ Seguridad JWT operacional (validación de secret al arranque)  
✅ Patrones de referencia establecidos (Employee, Articles, Sale, Purchases)  
✅ Migraciones de BD: V1–V5 (V4/V5 alinean enums y expiración de empeños)  
✅ Motor sync MVP (scheduler + `/sync/status` + `/sync/trigger`)  
⚠️ Tests: unitarios de Clients y Sync; faltan integración/Auth/Articles  
✅ Los 7 módulos de dominio core están presentes  

---

## 2. Inventario de Módulos

---

### 🏗️ Módulo: Infraestructura / Config

**Estado: ✅ COMPLETO**

| Componente | Archivo | Estado |
|---|---|---|
| SecurityConfig | `Config/SecurityConfig.java` | ✅ Completo |
| CorsConfig | `Config/CorsConfig.java` | ✅ Completo |
| RedisConfig | `Config/RedisConfig.java` | ✅ Completo |
| SchedulingConfig | `Config/SchedulingConfig.java` | ✅ Completo |
| OpenApiConfig | `Config/OpenApiConfig.java` | ✅ Completo |
| JacksonConfig | `Config/JacksonConfig.java` | ✅ Completo |
| DataSourceConfig | `Config/DataSourceConfig.java` | ✅ (mínimo necesario) |
| JwtStartupValidator | `Config/JwtStartupValidator.java` | ✅ Completo |
| application.yml | `resources/application.yml` | ✅ Completo (lowercase) |
| Docker Compose | `docker-compose.yml` | ✅ Completo (lowercase) |
| Dockerfile | `Dockerfile` | ✅ Completo |

**Observaciones técnicas:**
- `JacksonConfig.java`, `application.yml` y `docker-compose.yml` usan nombres en minúsculas para Linux/Docker.
- `DataSourceConfig.java` solo tiene `@EnableTransactionManagement`; aceptable ya que la configuración real está en `application.yml`.
- El `RedisConfig` usa `LaissezFaireSubTypeValidator` con `NON_FINAL` — potencialmente peligroso en producción con datos no confiables, pero aceptable para el contexto actual.

---

### 🔐 Módulo: Security (Transversal)

**Estado: ✅ COMPLETO**

| Componente | Archivo | Estado |
|---|---|---|
| JwtService | `Security/service/JwtService.java` | ✅ Completo |
| JwtAuthenticationFilter | `Security/filter/JwtAuthenticationFilter.java` | ✅ Completo |
| UserDetailsServiceImpl | `Security/service/UserDetailsServiceImpl.java` | ✅ Completo |
| CustomUserDetails | `Security/model/CustomUserDetails.java` | ✅ Completo |
| SecurityContext | `Security/context/SecurityContext.java` | ✅ Completo |

**Observaciones técnicas:**
- `JwtService` implementa correctamente generación, validación, extracción de claims y cálculo de TTL restante.
- `JwtAuthenticationFilter` verifica blacklist en Redis con fallback graceful si Redis no está disponible. Correcto.
- `SecurityContext` usa `@UtilityClass` de Lombok — apropiado para una clase de utilidades estáticas.
- El filtro JWT procesa el blacklist antes de validar el token: orden correcto para performance.

---

### 🔎 Módulo: Audit (Transversal)

**Estado: ✅ COMPLETO**

| Componente | Estado |
|---|---|
| `@Auditable` annotation | ✅ |
| `AuditAspect` | ✅ |
| `AudLog` entity | ✅ |
| `AuditRepository` | ✅ |

**Observaciones técnicas:**
- El aspecto captura correctamente args (before) y result (after).
- La persistencia del audit log no interrumpe el flujo principal (try-catch interno).
- El campo `entityId` en `AudLog` se extrae del resultado (`globalId` / `id`) en `AuditAspect`.

---

### ⚠️ Módulo: Exception (Transversal)

**Estado: ✅ COMPLETO**

| Componente | Estado |
|---|---|
| `GlobalExceptionHandler` | ✅ |
| `BusinessException` | ✅ |
| `ResourceNotFoundException` | ✅ |
| `UnauthorizedException` | ✅ |
| `DuplicateResourceException` | ✅ |
| `ErrorResponse` | ✅ (existe pero GlobalExceptionHandler usa `ApiResponse` directamente) |

**Observaciones técnicas:**
- El handler maneja 8 tipos de excepciones con códigos HTTP correctos.
- `ErrorResponse.java` en `Exception/Dto/` existe pero no se usa en `GlobalExceptionHandler` — se usa `ApiResponse<Void>` directamente. Clase muerta, puede eliminarse.
- Manejo de `LockedException` retorna `423 LOCKED` — correcto para cuentas bloqueadas por rate limiting.

---

### 📦 Módulo: Shared

**Estado: ✅ COMPLETO**

| Componente | Estado |
|---|---|
| `ApiResponse<T>` | ✅ |
| `PageResponse<T>` | ✅ |
| `ErrorDetail` | ✅ |
| `BaseEntity` | ✅ |
| `Role` enum | ✅ |
| `AppConstants` | ✅ |

**Observaciones técnicas:**
- `BaseEntity` tiene campo `isDeleted` (boolean con nombre `is_deleted` en BD). Correcto.
- `ApiResponse` usa patrón Builder con factory methods estáticos — consistente y limpio.
- `PageResponse.from(Page<T>)` es un wrapper conveniente y bien implementado.

---

### 🔑 Módulo: Auth

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-AUTH-01 ✅, HU-AUTH-02 ✅, HU-AUTH-03 ✅, HU-AUTH-04 ✅

| Componente | Estado |
|---|---|
| `AuthController` | ✅ |
| `AuthService` / `AuthServiceImpl` | ✅ |
| `TokenService` / `TokenServiceImpl` | ✅ |
| `LoginRateLimitService` | ✅ |
| `LoginRequest` / `RefreshRequest` | ✅ |
| `AuthResponse` | ✅ |

**Funcionalidades implementadas:**
- Login con autenticación vía BCrypt + JWT
- Refresh token con rotación
- Logout con blacklist en Redis
- Registro de empleado (delegado a `EmployeeService`)
- Rate limiting: 5 intentos → bloqueo 15 min en Redis
- Fallback graceful cuando Redis no está disponible

**Funcionalidades corregidas (Junio 2026):**
- ✅ `buildExtrateClaims()` ahora incluye `employeeId` usando `globalId` (no PK interna).
- ✅ Campo `mode` en `AuthResponse` ahora retorna `"local"` — refleja correctamente que la autenticación es siempre contra BD local.
- ✅ `TokenServiceImpl.buildExtraClaims()` corregido: usa `getGlobalId()` en lugar de `getId()`.
- ✅ Clase muerta `ErrorResponse.java` eliminada.

---

### 👔 Módulo: Employee

**Estado: ✅ 100% COMPLETO — Módulo de Referencia**

**HUs cubiertas:** HU-EMP-01 ✅, HU-EMP-02 ✅

| Componente | Estado |
|---|---|
| `EmployeeController` | ✅ |
| `EmployeeService` / `EmployeeServiceImpl` | ✅ |
| `EmployeeRepository` | ✅ |
| `Employee` entity | ✅ |
| `EmployeeMapper` | ✅ |
| DTOs (Create, Update, UpdateProfile, Response) | ✅ |

**Funcionalidades implementadas:**
- CRUD completo con paginación y filtros
- Activar/desactivar cuenta (con protección self-deactivation)
- Actualizar propio perfil (nombre + contraseña con confirmación)
- Auditoría en todas las operaciones de escritura
- RBAC: `@PreAuthorize("hasRole('ADMIN')")` donde corresponde

**Observaciones técnicas:**
- `EmployeeMapper.toEntity()` inicializa `passwordHash` con `""` — correcto, se sobreescribe inmediatamente con BCrypt en el service.
- `findByFilters()` en el repository usa JPQL correcta con parámetros opcionales.
- `updateMyProfile()` no requiere `@PreAuthorize` — cualquier autenticado puede actualizar su propio perfil. Correcto según RF-07.3.

---

### 👥 Módulo: Clients

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-CLI-01 ✅ (mayormente)

| Componente | Estado |
|---|---|
| `ClienteController` | ✅ |
| `ClienteService` / `ClienteServiceImpl` | ✅ |
| `ClienteRepository` | ✅ |
| `Cliente` entity | ✅ |
| `ClienteMapper` | ✅ |
| DTOs completos | ✅ |
| Enums (`ClienteStatus`, `RegistrationType`) | ✅ |

**Funcionalidades implementadas:**
- CRUD completo con paginación y filtros por status
- Búsqueda por nombre, apellido, cédula, email
- Validación unicidad de cédula y teléfono (create y update)
- Auto-detección de tipo COMPLETO/RAPIDO según datos enviados
- `promoteToComplete()` al actualizar
- Soft delete (`ELIMINADO`) y hard delete
- RBAC: Empleado ve solo `ACTIVO`, Admin filtra libremente (RF-06.4)

**Correcciones aplicadas (Junio 2026):**
- ✅ `searchByTerm()` ahora incluye búsqueda por teléfono (`c.phone`) en la query JPQL.
- ✅ `@PreAuthorize("hasRole('ADMIN')")` removido del soft delete — ahora tanto ADMIN como EMPLEADO pueden hacer eliminación lógica.
- ✅ Hard delete mantiene correctamente la restricción `@PreAuthorize("hasRole('ADMIN')")`.

---

### 📦 Módulo: Articles

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-ART-01 ✅, HU-ART-02 ✅, HU-ART-03 ✅, HU-ART-04 ✅, HU-ART-05 ✅

**Funcionalidades implementadas:**
- Entity `Article` extendiendo `BaseEntity`
- Enums: `ArticleCategory`, `SourceType`, `ItemState`
- Repository con queries de búsqueda, filtros y paginación
- DTOs y Mapper completo
- Service con lógica de stock (sin valores negativos)
- Controller con endpoints completos
- Integración con `@Auditable`
- Columnas JPA alineadas al schema: `source_type`, `item_state`, `purchase_price`

---

### 🤝 Módulo: Pawns (Empeños)

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-PAW-01 a HU-PAW-07

**Funcionalidades implementadas:**
- Transacciones atómicas seguras: `INSERT pawn` + `UPDATE stock article`.
- Empeño ágil: Creación de cliente rápido, artículo y empeño en una sola transacción unificada.
- Registro de pagos de cuota y cuotas impagadas, con transiciones de estado automatizadas (a FINALIZADO o PERDIDO).
- Expiración automática vía `@Scheduled` llamando a `fn_expire_overdue_pawns` (estados `ACTIVO`/`VENCIDO`, alineados con JPA; migraciones V4/V5).
- Máquina de estados validada internamente en la entidad `Pawn` para estados inmutables.
- Marcado manual como devuelto/retirado.
- Capas de repositorio, servicio e integración de API REST completas con `@PreAuthorize`.

---

### 💰 Módulo: Sales (Ventas)

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-SAL-01 ✅, HU-SAL-02 ✅, HU-SAL-03 ✅

| Componente | Estado |
|---|---|
| `SaleController` | ✅ |
| `SaleService` / `SaleServiceImpl` | ✅ |
| `SaleRepository` / `SaleProcedureRepository` | ✅ |
| `Sale` / `SaleDetails` entity | ✅ |
| `SaleMapper` | ✅ |
| DTOs completos | ✅ |

**Funcionalidades implementadas:**
- Integración con el stored procedure `register_sale()` en PostgreSQL.
- Soft delete de ventas y devolución automática de inventario a los artículos afectados.
- Paginación y filtros por rango de fechas y clientes.
- Control de roles (ADMIN / EMPLEADO) para listado y eliminación.

**Dependencias:**
- Requiere Módulo Articles (para gestionar stock).

---

### 🛒 Módulo: Purchases (Compras)

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-PUR-01 ✅

| Componente | Estado |
|---|---|
| `PurchaseController` | ✅ |
| `PurchaseService` / `PurchaseServiceImpl` | ✅ |
| `PurchaseRepository` | ✅ |
| `Purchase` entity (sin `BaseEntity`) | ✅ |
| `PurchaseMapper` | ✅ |
| DTOs (`CreatePurchaseRequest`, `PurchaseItemRequest`, `PurchaseResponse`) | ✅ |
| Auxiliares (`ArticleCreationService`, `ClienteResolutionService`, `EmployeeContextService`) | ✅ |

**Funcionalidades implementadas:**
- Registro de compra en una transacción: empleado autenticado, cliente existente / RAPIDO / anónimo.
- Un ítem del request = un `Article` (`SourceType.COMPRA`) + una fila en `purchases`.
- WARN si `purchasePrice >= salePrice` (no bloquea).
- Listado paginado; EMPLEADO ve las propias, ADMIN todas.
- Anulación física (ADMIN) y borrado del artículo si no tiene ventas ni empeños.
- `@Auditable` en create/delete.

**Dependencias:**
- Módulo Articles (crea inventario al comprar).
- Módulo Clients (opcional, proveedor).

**Correcciones al cerrar el módulo (septiembre 2026):**
- Servicio incompleto + typo `findByGloabalId`; se completó e inyectaron los auxiliares.
- Se añadió `PurchaseController`.
- Entidad: Lombok `@AllArgsConstructor`, índice `purchase_date`.
- `V3__align_base_entity_columns.sql` para que Hibernate valide `is_deleted`/`updated_at`.
- Procesador Lombok en Maven; `@Auditable(operation)` y `SourceType.EMPEÑO` en Pawns; alias `ApiResponse.success`.

---

### 🔄 Módulo: Sync Engine

**Estado: ✅ MVP OPERATIVO (~80%)**

| Componente | Estado |
|---|---|
| `SyncOutbox` entity | ✅ |
| `SyncStatus` enum | ✅ |
| `SyncOutboxRepository` | ✅ |
| `SyncEngineService` + `@Scheduled` | ✅ |
| `SupabaseSyncClient` | ✅ Upload INSERT/UPDATE/DELETE |
| `SyncController` (`/sync/status`, `/sync/trigger`) | ✅ ADMIN |
| Descarga remota / `NetworkMonitor` | ❌ No implementado (no bloquea el outbox local) |

La tabla `sync_outbox` sigue llena por triggers. Con `SYNC_ENABLED=true` y claves de Supabase reales, el scheduler procesa `PENDING`. En local el default es `sync.enabled=false` para no fallar sin nube.

**Endpoints:** `GET /api/sync/status`, `POST /api/sync/trigger` (ADMIN).

---

### 🧪 Tests

**Estado: ⚠️ 20% (unitarios de Clients y Sync)**

| Componente | Estado |
|---|---|
| `BackendApplicationTests` | ⚠️ Stub de contexto (Testcontainers) |
| `TestcontainersConfiguration` | ✅ Configurado para Redis |
| `ClienteServiceImplTest` | ✅ |
| `SyncEngineServiceTest` | ✅ |
| Tests de Controller | ❌ Ninguno |
| Tests de integración Auth | ❌ Ninguno |

---

## 3. Matriz de Cumplimiento

### Historias de Usuario

| ID | Historia | Estado | Notas |
|---|---|---|---|
| HU-AUTH-01 | Login online/offline | ✅ Completo | Auth local con `mode: "local"` — refleja correctamente el modo |
| HU-AUTH-02 | Refresh token | ✅ Completo | Rotación implementada |
| HU-AUTH-03 | Logout | ✅ Completo | Blacklist Redis |
| HU-AUTH-04 | Registro empleado (Admin) | ✅ Completo | |
| HU-ART-01 | Listar inventario | ✅ Completo | |
| HU-ART-02 | Crear artículo (Admin) | ✅ Completo | |
| HU-ART-03 | Editar artículo | ✅ Completo | |
| HU-ART-04 | Gestión de stock | ✅ Completo | |
| HU-ART-05 | Eliminar artículo (Admin) | ✅ Completo | |
| HU-PAW-01 | Registrar empeño | ✅ Completo | |
| HU-PAW-02 | Empeño ágil | ✅ Completo | |
| HU-PAW-03 | Registrar pago cuota | ✅ Completo | |
| HU-PAW-04 | Cuota impagada (Admin) | ✅ Completo | |
| HU-PAW-05 | Marcar empeño devuelto | ✅ Completo | |
| HU-PAW-06 | Expiración automática | ✅ Completo | Función BD conectada a `@Scheduled` |
| HU-PAW-07 | Filtrar empeños por estado | ✅ Completo | |
| HU-SAL-01 | Registrar venta | ✅ Completo | Usa SP `register_sale()` |
| HU-SAL-02 | Filtrar ventas | ✅ Completo | |
| HU-SAL-03 | Eliminar venta (Admin) | ✅ Completo | Soft delete y rollback de stock |
| HU-PUR-01 | Registrar compra | ✅ Completo | Crea artículos + N filas `purchases`; anulación ADMIN |
| HU-CLI-01 | CRUD clientes | ✅ Completo | Soft delete abierto, search incluye phone |
| HU-EMP-01 | Gestión empleados (Admin) | ✅ Completo | |
| HU-EMP-02 | Actualizar propio perfil | ✅ Completo | |
| HU-SYNC-01 | Ver estado sync | ✅ Completo | `GET /sync/status` ADMIN |
| HU-SYNC-02 | Forzar sync manual | ✅ Completo | `POST /sync/trigger` ADMIN |

### Requisitos Funcionales Críticos

| RF | Requisito | Estado |
|---|---|---|
| RF-01.1 | Auth vía Supabase Auth con conexión | ⚠️ No implementado (auth es siempre local) |
| RF-01.2 | Auth local con BCrypt sin internet | ✅ Funciona (es el único modo) |
| RF-01.3 | JWT 1h access / 7d refresh | ✅ |
| RF-01.4 | Refresh token rotation | ✅ |
| RF-01.5 | Bloqueo tras 5 intentos (15 min) | ✅ |
| RF-01.6 | Solo Admin registra empleados | ✅ |
| RF-01.7 | Logout invalida token en Redis | ✅ |
| RF-02.1..8 | Módulo Articles | ✅ Completo |
| RF-03.1..10 | Módulo Pawns | ✅ Completo |
| RF-04.1..7 | Módulo Sales | ✅ Completo |
| RF-05.1..5 | Módulo Purchases | ✅ Completo |
| RF-06.1 | Tipos COMPLETO/RAPIDO | ✅ |
| RF-06.2 | Promover RAPIDO → COMPLETO | ✅ |
| RF-06.3 | Unicidad cédula y teléfono | ✅ |
| RF-06.4 | Empleado solo ve ACTIVO | ✅ |
| RF-06.5 | Soft delete | ✅ |
| RF-06.6 | Hard delete sin operaciones | ⚠️ Delega a FK constraint |
| RF-07.1..4 | Módulo Employees | ✅ |
| RF-08.1..8 | Motor Sync | ✅ MVP upload outbox; falta download remoto |
| RF-09.1..4 | Auditoría AOP | ✅ |

---

## 4. Hallazgos Técnicos

### 🔴 Errores / Bugs Activos

**Bug 1 — `ClienteController`: soft delete solo para ADMIN — ✅ CORREGIDO**
```java
// CORREGIDO: @PreAuthorize removido del soft delete
// Ahora tanto ADMIN como EMPLEADO pueden hacer soft delete
@DeleteMapping("/{globalId}")
@Operation(summary = "eliminacion logica del cliente o cambiar el estado")
public ResponseEntity<Void> delete(...)

// El hard delete SÍ mantiene ADMIN:
@DeleteMapping("/{globalId}/hard")
@PreAuthorize("hasRole('ADMIN')")
```

**Bug 2 — `ClienteRepository.searchByTerm()`: no busca por teléfono — ✅ CORREGIDO**
```java
// CORREGIDO: phone agregado a la query JPQL
OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :term, '%'))
```

**Bug 3 — `AudLog.entityId` siempre null — ✅ CORREGIDO**
`AuditAspect` extrae `globalId` / `id` del resultado (incluye `ApiResponse.data`) y lo persiste en `AudLog.entityId`.

**Bug 4 — `AuthResponse.mode` hardcodeado como `"online"` — ✅ CORREGIDO**
Ahora `AuthServiceImpl` y `TokenServiceImpl.buildAuthResponse()` retornan `mode: "local"`, reflejando correctamente que la autenticación se realiza contra BD local.

### 🟠 Inconsistencias de Arquitectura

**Inconsistencia 1 — `Employee.id` expuesto en `buildExtraClaims()` — ✅ CORREGIDO**
```java
// En TokenServiceImpl (CORREGIDO):
"employeeId", employee.getGlobalId().toString()
// En AuthServiceImpl (CORREGIDO): employeeId ahora incluido con globalId
```

**Inconsistencia 2 — Convención de nombres de archivos — ✅ CORREGIDO**
- `JacksonConfig.java`
- `application.yml` / `application-production.yml`
- `docker-compose.yml`

**Inconsistencia 3 — `ErrorResponse.java` clase muerta — ✅ CORREGIDO**
Clase `Exception/Dto/ErrorResponse.java` eliminada. `GlobalExceptionHandler` usa `ApiResponse<Void>` directamente.

**Inconsistencia 4 — `BaseEntity.isDeleted` vs uso en módulos**
`Cliente` usa `ClienteStatus.ELIMINADO` para soft delete **en lugar de** `BaseEntity.isDeleted`. Esto causa que el campo `is_deleted` en la tabla `clientes` nunca se use para el soft delete real. El soft delete se gestiona via `status = ELIMINADO`. No es un bug, pero es una dualidad confusa.

### 🟡 Riesgos Técnicos

**Riesgo 1 — Cobertura de tests incompleta**
Hay unitarios de `ClienteServiceImpl` y `SyncEngineService`. Faltan Auth, Articles, Pawns, integración con Testcontainers.

**Riesgo 2 — Outbox sin procesar si sync está apagado**
Con `sync.enabled=false` (default local) `sync_outbox` puede crecer. Activar `SYNC_ENABLED=true` y claves reales de Supabase en el entorno que deba replicar.

**Riesgo 3 — JWT secret placeholder en `.env` local — ✅ MITIGADO**
`JwtStartupValidator` exige ≥ 32 caracteres y rechaza `CHANGE_ME` si el perfil es `production`. En local solo advierte.

**Riesgo 4 — `SUPABASE_SERVICE_ROLE_KEY` requerido — ✅ CORREGIDO**
`application.yml` usa valores por defecto vacíos. El motor no arranca contra Supabase si la clave es placeholder.

### 🔵 Mejoras Recomendadas (no críticas)

1. **Agregar `@JsonProperty` o renombrar** en `ClienteResponse` para consistencia de nomenclatura en JSON.
2. **Implementar `Dashboard` endpoint** — retorna KPIs básicos. Útil para validar integración end-to-end.
3. ~~**Agregar validación de `JWT_SECRET` al arranque**~~ — `JwtStartupValidator` ✅
4. **Agregar `@Cacheable` en `findAll` de Employees y Clients** — ya existe `CacheManager` con TTLs configurados, pero no se usa en ningún service.
5. **`AuditRepository.findByDateRange()`** tiene una query JPQL correcta pero nunca hay endpoint que la exponga.

---

## 5. Correcciones Necesarias por Prioridad

### 🔴 CRÍTICAS (corregir antes de continuar)

| # | Corrección | Archivo | Impacto |
|---|---|---|---|
| C1 | ~~`@DeleteMapping` soft delete sin `@PreAuthorize`~~ | `ClienteController.java` | ✅ CORREGIDO |
| C2 | ~~`application.yml` y `application-production.yml` lowercase~~ | Configuración | ✅ CORREGIDO |
| C3 | ~~Enums `pawn_status` / `source_type` vs JPA + columnas `Article`~~ | V1, V4, V5, `Article.java` | ✅ CORREGIDO |

### 🟠 ALTAS (corregir en el ciclo actual)

| # | Corrección | Archivo | Impacto |
|---|---|---|---|
| A1 | ~~Agregar búsqueda por teléfono en `searchByTerm()`~~ | `ClienteRepository.java` | ✅ CORREGIDO |
| A2 | ~~`employee.getId()` → `employee.getGlobalId()` en `buildExtraClaims()`~~ | `TokenServiceImpl.java` | ✅ CORREGIDO |
| A3 | ~~`AuthResponse.mode` retornar valor real~~ | `AuthServiceImpl.java` | ✅ CORREGIDO |
| A4 | ~~Eliminar `ErrorResponse.java` clase muerta~~ | `Exception/Dto/` | ✅ CORREGIDO |

### 🟡 MEDIAS (backlog técnico)

| # | Corrección | Impacto |
|---|---|---|
| M1 | ~~Poblar `AudLog.entityId` en `AuditAspect`~~ | ✅ CORREGIDO |
| M2 | Agregar `@JsonProperty` para nombres de campos consistentes en `ClienteResponse` | API inconsistente |
| M3 | ~~Validar `JWT_SECRET` length en startup~~ | ✅ CORREGIDO (`JwtStartupValidator`) |
| M4 | ~~`SUPABASE_SERVICE_ROLE_KEY` con valor por defecto en yml~~ | ✅ CORREGIDO |

### 🔵 BAJAS (nice-to-have)

| # | Corrección | Impacto |
|---|---|---|
| B1 | ~~Renombrar `JackSonConfig.java` → `JacksonConfig.java`~~ | ✅ CORREGIDO |
| B2 | Activar `@Cacheable` en listados frecuentes | Performance |
| B3 | Agregar endpoint `GET /dashboard/metrics` mínimo | Trazabilidad |
| B4 | Agregar `@SuppressWarnings` o limpiar raw types en `RedisConfig` | Limpieza |

---

## 6. Próxima Ruta de Desarrollo

### Justificación del Orden Recomendado

```
Articles → Pawns → Sales → Purchases → Sync Engine → Tests
     ✅         ✅       ✅         ✅           ✅ MVP      ⚠️
```

**Fases 1–5 (dominio + Sync MVP): completadas.**  
Siguiente trabajo: tests de integración/Auth/Articles y Dashboard opcional.

---

## 7. Plan de Trabajo Inmediato

### Fase 0 — Correcciones Previas (✅ COMPLETADA)

**Tarea 0.1 — Corregir bug de autorización en ClienteController**
```java
// Eliminar @PreAuthorize del soft delete:
@DeleteMapping("/{globalId}")
// @PreAuthorize("hasRole('ADMIN')")  ← ELIMINAR
@Operation(summary = "Eliminación lógica del cliente")
public ResponseEntity<Void> delete(@PathVariable UUID globalId) { ... }
```

**Tarea 0.2 — Renombrar archivos de configuración — ✅ COMPLETADA**
- `application.yml` / `application-production.yml`
- `JacksonConfig.java`
- `docker-compose.yml`

**Tarea 0.3 — Corregir `globalId` en JWT claims**
```java
// En TokenServiceImpl.buildExtraClaims():
"employeeId", employee.getGlobalId().toString()  // no getId()
```

---

### Fase 1 — Módulo Articles (✅ COMPLETADA)

**Patrón a seguir:** Employee module como referencia canónica.

**Tarea 1.1 — Crear enums**
```
Modules/Articles/Enums/
├── ArticleCategory.java   → {Electrodomesticos, Joyeria, Herramientas, Tecnologia, Otro}
├── SourceType.java        → {EMPENO, COMPRA, AJUSTE, OTRO}
└── ItemState.java         → {Excelente, Bueno, Regular, Malo}
```
> ⚠️ Los valores DEBEN coincidir exactamente con los enums PostgreSQL en `V1__schema_completo.sql`.

**Tarea 1.2 — Entity `Article`**
```
Modules/Articles/Entity/Article.java
```
- Hereda `BaseEntity`
- Campos: clienteId, nameArticle, description, category, sourceType, itemState, amount, price, purchasePrice
- Constraint: amount >= 0 (`@Min(0)`)
- `@Enumerated(EnumType.STRING)` en category, sourceType, itemState
- `columnDefinition` debe referenciar VARCHAR, no el tipo enum PG (para compatibilidad JPA)

**Tarea 1.3 — Repository**
```
Modules/Articles/Repository/ArticleRepository.java
```
Queries necesarias:
- `findByGlobalId(UUID)`
- `findByFilters(category, minStock, onlyAvailable, Pageable)` — JPQL con parámetros opcionales
- `findByNameArticleContainingIgnoreCaseOrderByNameArticleAsc(String)`
- `existsByNameArticleIgnoreCase(String)` — para evitar duplicados

**Tarea 1.4 — DTOs**
```
Modules/Articles/Dto/
├── Request/CreateArticleRequest.java   → @NotBlank, @NotNull, @Positive
├── Request/UpdateArticleRequest.java   → todos opcionales
└── Response/ArticleResponse.java       → incluye hasStock (calculado)
```

**Tarea 1.5 — Mapper**
```
Modules/Articles/Mapper/ArticleMapper.java
```
- `toEntity(CreateArticleRequest)` — defaults para sourceType e itemState
- `toResponse(Article)` — calcula `hasStock = amount > 0`
- `applyUpdates(Article, UpdateArticleRequest)` — solo campos no-null

**Tarea 1.6 — Service**
```
Modules/Articles/Service/ArticleService.java
Modules/Articles/Service/Impl/ArticleServiceImpl.java
```
Métodos con lógica crítica:
- `create()` — `@Transactional`, `@Auditable`
- `addStock(id, quantity)` — `@Transactional`, valida quantity > 0
- `removeStock(id, quantity)` — `@Transactional`, valida stock suficiente
- `delete(id)` — validar que no tenga ventas/empeños activos antes de eliminar
- `findAll(filters, pageable)` — Empleado solo ve disponibles (`amount > 0`)

**Tarea 1.7 — Controller**
```
Modules/Articles/Controller/ArticleController.java
```
Endpoints:
```
GET    /articles                → paginado con filtros
GET    /articles/{globalId}     → detalle
GET    /articles/search?term=   → búsqueda
GET    /articles/available      → solo con stock > 0
POST   /articles                → @PreAuthorize Admin
PUT    /articles/{globalId}     → @PreAuthorize Admin
PATCH  /articles/{globalId}/stock/add?quantity=
PATCH  /articles/{globalId}/stock/remove?quantity=
DELETE /articles/{globalId}     → @PreAuthorize Admin
```

---

### Fase 2 — Módulo Pawns (✅ COMPLETADA)

> Este es el módulo más complejo. Requiere Phase 1 completa.

**Tarea 2.1 — Enums y Entity**
```
Modules/Pawns/Enums/PawnStatus.java  → {Activo, Vencido, Finalizado, Retirado, Perdido, Vendido}
Modules/Pawns/Entity/Pawn.java
Modules/Pawns/Entity/PawnPayment.java
```

**Tarea 2.2 — Lógica de estados (State Machine)**
En `Pawn.java` agregar métodos de dominio:
```java
public boolean canAcceptPayments()      // Activo o Vencido
public boolean isTerminalState()        // Finalizado, Retirado, Perdido, Vendido
public boolean canBeMarkedReturned()    // Activo o Vencido
public void validateStateTransition(PawnStatus newStatus)
```

**Tarea 2.3 — Transacciones atómicas**
`PawnServiceImpl.create()` debe:
1. Validar stock del artículo
2. Reducir `article.amount` 
3. INSERT en pawns
Todo en `@Transactional` — si falla cualquier paso, rollback.

**Tarea 2.4 — Empeño ágil**
`PawnServiceImpl.createAgile()`:
1. Crear cliente (tipo RAPIDO)
2. Crear artículo
3. Crear empeño
Transacción única.

**Tarea 2.5 — Programar expiración automática**
```java
@Scheduled(fixedDelay = 30_000)
public void expireOverduePawns() {
    // llamar a fn_expire_overdue_pawns() via JPA native query
}
```

---

### Fase 3 — Módulo Sales (✅ COMPLETADA)

> Módulo de ventas completado exitosamente. Se implementó la integración nativa con PostgreSQL y el borrado lógico con reposición de inventario.

---

### Fase 4 — Módulo Purchases (✅ COMPLETADA)

> Compras implementadas: cliente opcional/RAPIDO/anónimo, alta de artículos `COMPRA`, una fila `purchases` por ítem, WARN de margen, anulación física ADMIN. Ver `PROMPT_MODULO_PURCHASES.md`.

---

### Fase 5 — Sync Engine básico (✅ COMPLETADA)

**Tarea 5.1 — SyncOutboxRepository** ✅ `findByStatusOrderByCreatedAtAsc`

**Tarea 5.2 — SyncEngineService** ✅ ciclo `@Scheduled`, skip si Supabase no está configurado, reintentos y CONFLICT.

**Tarea 5.3 — SyncController** ✅
```
GET  /sync/status   → PENDING/SYNCING/SYNCED/FAILED/CONFLICT
POST /sync/trigger  → ciclo manual (Admin)
```

Pendiente de producto (no bloquea dominio): descarga remota desde Supabase.

---

### Fase 6 — Tests (parcial)

**Tarea 6.1 — `ClienteServiceImplTest` y `SyncEngineServiceTest`** ✅  
**Pendiente:** `ArticleServiceImplTest`, Auth IT con Testcontainers.

---

## Resumen Ejecutivo para Toma de Decisiones

| Aspecto | Estado | Acción |
|---|---|---|
| Base lista para demostración | ✅ Auth + Employee + Clients + Articles + Pawns + Sales + Purchases | Flujo de negocio local |
| Motor sync | ✅ MVP upload | Activar `SYNC_ENABLED` + claves reales para nube |
| Bug bloqueante activo | ✅ Compilación Maven OK | YAML lowercase, enums pawn/source, columnas Article |
| Tests | ⚠️ Unitarios Clients + Sync | Ampliar Auth/Articles e integración |
| BD | ✅ V1–V5 | V4/V5 alinean enums y `fn_expire_overdue_pawns` |

---

*Informe generado con base en revisión estática del repositorio — Junio 2026; actualizado 15 septiembre 2026 (Sync MVP y correcciones críticas).*
