# Informe de Análisis Integral — CompraVenta Backend
## Spring Boot 3.4.5 · Java 21 · PostgreSQL 16 · Redis

> **Generado:** Junio 2026  
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
| Módulo Sales              | 0%     |
| Módulo Purchases          | 0%     |
| Motor Sync                | 15%    |
| Tests                     | 5%     |
| **TOTAL GLOBAL**          | **~75%** |

### Resumen Ejecutivo

El proyecto tiene una **base de infraestructura sólida y bien construida**. Los módulos transversales (Config, Security, Audit, Exception, Shared) están completos y con calidad alta. Los módulos Auth, Employee y Clients están implementados con buenas prácticas y lógica de negocio correcta.

Sin embargo, **2 de los 7 módulos de dominio principal están ausentes**: Sales y Purchases. El motor de sincronización offline solo tiene la entidad `SyncOutbox` sin ningún servicio operacional. Los tests prácticamente no existen más allá de los stubs generados por Spring Initializr.

El proyecto está **listo para continuar el desarrollo** sobre una base limpia. No hay deudas técnicas críticas pendientes en los módulos ya implementados (los bugs previamente detectados en versiones anteriores han sido corregidos según las memorias del proyecto).

### Nivel de Preparación para Continuar

✅ Infraestructura lista  
✅ Seguridad JWT operacional  
✅ Patrones de referencia establecidos (Employee y Articles como módulos base)  
✅ Migraciones de BD completas para todas las tablas  
⚠️ Motor sync incompleto (no bloquea el desarrollo de módulos de dominio)  
❌ Sin tests unitarios ni de integración reales  
❌ 2 módulos de negocio core ausentes  

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
| JacksonConfig | `Config/JackSonConfig.java` | ✅ Completo |
| DataSourceConfig | `Config/DataSourceConfig.java` | ✅ (mínimo necesario) |
| application.yml | `resources/Application.yml` | ✅ Completo |
| Docker Compose | `Docker-Compose.yml` | ✅ Completo |
| Dockerfile | `Dockerfile` | ✅ Completo |

**Observaciones técnicas:**
- `JackSonConfig.java` tiene nombre con mayúscula intermedia incorrecta (convención sería `JacksonConfig.java`) — cosmético, no funcional.
- `DataSourceConfig.java` solo tiene `@EnableTransactionManagement`; aceptable ya que la configuración real está en `Application.yml`.
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
- El campo `entityId` en `AudLog` nunca se popula en `AuditAspect` — siempre queda `null`. Mejora pendiente (baja prioridad).

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

---

### 🤝 Módulo: Pawns (Empeños)

**Estado: ✅ 100% COMPLETO**

**HUs cubiertas:** HU-PAW-01 a HU-PAW-07

**Funcionalidades implementadas:**
- Transacciones atómicas seguras: `INSERT pawn` + `UPDATE stock article`.
- Empeño ágil: Creación de cliente rápido, artículo y empeño en una sola transacción unificada.
- Registro de pagos de cuota y cuotas impagadas, con transiciones de estado automatizadas (a FINALIZADO o PERDIDO).
- Expiración automática vía `@Scheduled` llamando a la función nativa `fn_expire_overdue_pawns`.
- Máquina de estados validada internamente en la entidad `Pawn` para estados inmutables.
- Marcado manual como devuelto/retirado.
- Capas de repositorio, servicio e integración de API REST completas con `@PreAuthorize`.

---

### 💰 Módulo: Sales (Ventas)

**Estado: ❌ NO IMPLEMENTADO (0%)**

**HUs pendientes:** HU-SAL-01, HU-SAL-02, HU-SAL-03

No existe ningún archivo en `Modules/Sales/`. La tabla `sales` y `sales_details` están en la migración. El stored procedure `register_sale()` está implementado en `V1__schema_completo.sql`.

**Dependencias necesarias:**
- Módulo Articles (para validar stock)

---

### 🛒 Módulo: Purchases (Compras)

**Estado: ❌ NO IMPLEMENTADO (0%)**

**HUs pendientes:** HU-PUR-01

No existe ningún archivo en `Modules/Purchases/`. La tabla `purchases` está en la migración.

**Dependencias necesarias:**
- Módulo Articles (crea artículo al registrar compra)
- Módulo Clients (opcional, para asociar proveedor)

---

### 🔄 Módulo: Sync Engine

**Estado: ⚠️ 15% IMPLEMENTADO**

| Componente | Estado |
|---|---|
| `SyncOutbox` entity | ✅ |
| `SyncStatus` enum | ✅ |
| `SyncOutboxRepository` | ❌ No existe |
| `SyncEngineService` | ❌ No existe |
| `ConflictResolver` | ❌ No existe |
| `NetworkMonitor` | ❌ No existe |
| `SupabaseHttpClient` | ❌ No existe |
| `SyncScheduler` | ❌ No existe |

La tabla `sync_outbox` existe en BD con triggers que capturan cambios automáticamente. Los cambios se registrarán en `sync_outbox` desde que se use cualquier módulo, pero no hay servicio que los procese.

---

### 🧪 Tests

**Estado: ❌ 5% (solo stubs de Initializr)**

| Componente | Estado |
|---|---|
| `BackendApplicationTests` | ⚠️ Solo verifica que el contexto carga |
| `TestcontainersConfiguration` | ✅ Configurado para Redis |
| Tests de Service | ❌ Ninguno |
| Tests de Controller | ❌ Ninguno |
| Tests de Repository | ❌ Ninguno |
| Tests de integración | ❌ Ninguno |

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
| HU-SAL-01 | Registrar venta | ❌ Pendiente | SP en BD listo |
| HU-SAL-02 | Filtrar ventas | ❌ Pendiente | |
| HU-SAL-03 | Eliminar venta (Admin) | ❌ Pendiente | |
| HU-PUR-01 | Registrar compra | ❌ Pendiente | |
| HU-CLI-01 | CRUD clientes | ✅ Completo | Soft delete abierto, search incluye phone |
| HU-EMP-01 | Gestión empleados (Admin) | ✅ Completo | |
| HU-EMP-02 | Actualizar propio perfil | ✅ Completo | |
| HU-SYNC-01 | Ver estado sync | ❌ Pendiente | |
| HU-SYNC-02 | Forzar sync manual | ❌ Pendiente | |

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
| RF-04.1..7 | Módulo Sales | ❌ Pendiente |
| RF-05.1..5 | Módulo Purchases | ❌ Pendiente |
| RF-06.1 | Tipos COMPLETO/RAPIDO | ✅ |
| RF-06.2 | Promover RAPIDO → COMPLETO | ✅ |
| RF-06.3 | Unicidad cédula y teléfono | ✅ |
| RF-06.4 | Empleado solo ve ACTIVO | ✅ |
| RF-06.5 | Soft delete | ✅ |
| RF-06.6 | Hard delete sin operaciones | ⚠️ Delega a FK constraint |
| RF-07.1..4 | Módulo Employees | ✅ |
| RF-08.1..8 | Motor Sync | ❌ Pendiente (solo tabla) |
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

**Bug 3 — `AudLog.entityId` siempre null**
`AuditAspect` construye el `AudLog` pero nunca popula `entityId`. Para operaciones que retornan un ID, podría extraerse del resultado. (Baja prioridad, pendiente)

**Bug 4 — `AuthResponse.mode` hardcodeado como `"online"` — ✅ CORREGIDO**
Ahora `AuthServiceImpl` y `TokenServiceImpl.buildAuthResponse()` retornan `mode: "local"`, reflejando correctamente que la autenticación se realiza contra BD local.

### 🟠 Inconsistencias de Arquitectura

**Inconsistencia 1 — `Employee.id` expuesto en `buildExtraClaims()` — ✅ CORREGIDO**
```java
// En TokenServiceImpl (CORREGIDO):
"employeeId", employee.getGlobalId().toString()
// En AuthServiceImpl (CORREGIDO): employeeId ahora incluido con globalId
```

**Inconsistencia 2 — Convención de nombres de archivos**
- `JackSonConfig.java` debería ser `JacksonConfig.java`
- `Application.yml` debería ser `application.yml` (lowercase) — en Linux, Flyway/Spring puede ser case-sensitive
- `Application-production.yml` ídem

**Inconsistencia 3 — `ErrorResponse.java` clase muerta — ✅ CORREGIDO**
Clase `Exception/Dto/ErrorResponse.java` eliminada. `GlobalExceptionHandler` usa `ApiResponse<Void>` directamente.

**Inconsistencia 4 — `BaseEntity.isDeleted` vs uso en módulos**
`Cliente` usa `ClienteStatus.ELIMINADO` para soft delete **en lugar de** `BaseEntity.isDeleted`. Esto causa que el campo `is_deleted` en la tabla `clientes` nunca se use para el soft delete real. El soft delete se gestiona via `status = ELIMINADO`. No es un bug, pero es una dualidad confusa.

### 🟡 Riesgos Técnicos

**Riesgo 1 — Sin tests unitarios ni de integración**
Las capas Service no tienen ninguna prueba. Un cambio inadvertido en `ClienteServiceImpl.resolveEffectiveStatus()` o en la lógica de Rate Limiting podría introducir regresiones sin ser detectado.

**Riesgo 2 — `SyncOutbox` acumula registros sin procesador**
Desde que se active cualquier módulo (Articles, Clients, etc.), los triggers de BD empezarán a insertar en `sync_outbox`. Sin `SyncEngineService`, esa tabla crecerá indefinidamente.

**Riesgo 3 — JWT secret en `.env` con valor placeholder**
`JWT_SECRET=CHANGE_ME_MINIMUM_64_CHARACTER_SECRET_KEY_FOR_HMAC_SHA256_SECURITY` — si se usa accidentalmente este valor en cualquier entorno, los tokens serán inseguros.

**Riesgo 4 — `Application.yml` con `SUPABASE_SERVICE_ROLE_KEY` requerido**
Si esta variable de entorno no está seteada y el sync está habilitado, el contexto de Spring puede fallar al arrancar. Debería tener un valor por defecto vacío para entornos de desarrollo.

### 🔵 Mejoras Recomendadas (no críticas)

1. **Agregar `@JsonProperty` o renombrar** en `ClienteResponse` para consistencia de nomenclatura en JSON.
2. **Implementar `Dashboard` endpoint** — retorna KPIs básicos. Útil para validar integración end-to-end.
3. **Agregar validación de `JWT_SECRET` al arranque** — verificar que tenga mínimo 32 caracteres en `PostConstruct`.
4. **Agregar `@Cacheable` en `findAll` de Employees y Clients** — ya existe `CacheManager` con TTLs configurados, pero no se usa en ningún service.
5. **`AuditRepository.findByDateRange()`** tiene una query JPQL correcta pero nunca hay endpoint que la exponga.

---

## 5. Correcciones Necesarias por Prioridad

### 🔴 CRÍTICAS (corregir antes de continuar)

| # | Corrección | Archivo | Impacto |
|---|---|---|---|
| C1 | ~~`@DeleteMapping` soft delete sin `@PreAuthorize`~~ | `ClienteController.java` | ✅ CORREGIDO |
| C2 | `Application.yml` y `Application-production.yml` — renombrar a lowercase | Configuración | En sistemas Linux (Docker), Spring no encuentra el archivo en producción |

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
| M1 | Poblar `AudLog.entityId` en `AuditAspect` | Trazabilidad incompleta |
| M2 | Agregar `@JsonProperty` para nombres de campos consistentes en `ClienteResponse` | API inconsistente |
| M3 | Validar `JWT_SECRET` length en startup | Seguridad |
| M4 | `SUPABASE_SERVICE_ROLE_KEY` con valor por defecto en yml | Startup en dev sin Supabase |

### 🔵 BAJAS (nice-to-have)

| # | Corrección | Impacto |
|---|---|---|
| B1 | Renombrar `JackSonConfig.java` → `JacksonConfig.java` | Convención |
| B2 | Activar `@Cacheable` en listados frecuentes | Performance |
| B3 | Agregar endpoint `GET /dashboard/metrics` mínimo | Trazabilidad |
| B4 | Agregar `@SuppressWarnings` o limpiar raw types en `RedisConfig` | Limpieza |

---

## 6. Próxima Ruta de Desarrollo

### Justificación del Orden Recomendado

```
Articles → Pawns → Sales → Purchases → Sync Engine → Tests
```

**¿Por qué Articles primero?**
- Es la entidad central del sistema. Pawns, Sales y Purchases tienen FK hacia `articles`.
- Sin Articles no se pueden probar ninguno de los otros módulos de dominio.
- El patrón `Employee` como referencia está listo para replicar.
- La tabla y todos los tipos enum ya existen en BD.

**¿Por qué Pawns segundo?**
- Es el módulo más complejo y el corazón del negocio de empeño.
- Depende de Articles y Clients (ambos listos).
- Tiene la lógica más crítica: transacciones atómicas, estados de máquina, expiración automática.
- Requiere la mayor atención de diseño.

**¿Por qué Sales tercero?**
- Depende de Articles.
- El stored procedure `register_sale()` ya está implementado en BD.
- Es una de las operaciones más frecuentes del negocio.

**¿Por qué Purchases cuarto?**
- Depende de Articles y opcionalmente de Clients.
- Es más simple que Pawns y Sales.

**¿Por qué Sync Engine después de módulos de dominio?**
- Los triggers de BD ya capturan cambios desde el momento 0.
- El sync no es bloqueante para el desarrollo de módulos.
- Implementarlo después permite entender mejor qué entidades necesitan sincronizarse.

**¿Por qué Tests al final?**
- El proyecto ya tiene patrones establecidos. Con los módulos completos, los tests pueden hacerse de forma más estratégica.
- Sin embargo, **se recomienda escribir tests de Articles en paralelo** para establecer el patrón.

---

## 7. Plan de Trabajo Inmediato

### Fase 0 — Correcciones Previas (1-2 horas)

**Tarea 0.1 — Corregir bug de autorización en ClienteController**
```java
// Eliminar @PreAuthorize del soft delete:
@DeleteMapping("/{globalId}")
// @PreAuthorize("hasRole('ADMIN')")  ← ELIMINAR
@Operation(summary = "Eliminación lógica del cliente")
public ResponseEntity<Void> delete(@PathVariable UUID globalId) { ... }
```

**Tarea 0.2 — Renombrar archivos de configuración**
- `Application.yml` → `application.yml`
- `Application-production.yml` → `application-production.yml`
- `JackSonConfig.java` → `JacksonConfig.java` (opcional, bajo impacto)

**Tarea 0.3 — Corregir `globalId` en JWT claims**
```java
// En TokenServiceImpl.buildExtraClaims():
"employeeId", employee.getGlobalId().toString()  // no getId()
```

---

### Fase 1 — Módulo Articles (estimado: 3-4 horas)

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

### Fase 2 — Módulo Pawns (estimado: 6-8 horas)

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

### Fase 3 — Módulo Sales (estimado: 3-4 horas)

> Requiere Phase 1 completa. El stored procedure `register_sale()` ya está en BD.

**Tarea 3.1 — Entities**
```
Modules/Sales/Entity/Sale.java
Modules/Sales/Entity/SaleDetail.java
```

**Tarea 3.2 — Service: invocar stored procedure**
```java
@Transactional
public SaleResponse create(CreateSaleRequest request) {
    // Llamar a register_sale() via JPA native query
    // Retornar la venta creada
}
```

**Tarea 3.3 — Controller con RBAC**
- Empleado: solo sus propias ventas
- Admin: todas las ventas

---

### Fase 4 — Módulo Purchases (estimado: 2-3 horas)

> Requiere Phases 1 completa.

**Tarea 4.1 — Entity, Service, Controller**
Patrón idéntico a Sales pero más simple:
- Transacción: opcionalmente crea/usa cliente + crea artículo + registra compra
- Alerta si `purchasePrice >= price` (pero no bloquea)

---

### Fase 5 — Sync Engine básico (estimado: 4-5 horas)

**Tarea 5.1 — SyncOutboxRepository**
```java
List<SyncOutbox> findByStatusOrderByCreatedAt(SyncStatus status, Pageable pageable);
```

**Tarea 5.2 — SyncEngineService mínimo viable**
```java
@Scheduled(fixedDelay = 30_000)
@ConditionalOnProperty(name = "sync.enabled", havingValue = "true")
public void syncCycle() {
    // Upload pending changes to Supabase
    // Download remote changes
}
```

**Tarea 5.3 — SyncController**
```
GET  /sync/status   → registros PENDING/FAILED/SYNCED
POST /sync/trigger  → dispara ciclo manual (Admin)
```

---

### Fase 6 — Tests (en paralelo desde Fase 1)

**Tarea 6.1 — Tests unitarios de ArticleServiceImpl**
```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {
    // should_throw_when_remove_stock_exceeds_available()
    // should_create_article_with_default_source_type()
    // should_filter_available_articles_for_empleado_role()
}
```

**Tarea 6.2 — Tests de integración para Auth**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class AuthControllerIT {
    // should_return_401_on_invalid_credentials()
    // should_return_token_on_valid_login()
    // should_block_after_5_failed_attempts()
}
```

---

## Resumen Ejecutivo para Toma de Decisiones

| Aspecto | Estado | Acción |
|---|---|---|
| Base lista para producción | ✅ Auth + Employee + Clients + Articles | Puede demostrarse ya |
| Próximo módulo crítico | ❌ Sales (Ventas) | Implementar inmediatamente |
| Bug bloqueante activo | ⚠️ Permisos soft delete | Corregir en 5 minutos |
| Riesgo mayor | ❌ Sin tests | Agregar en paralelo con los nuevos módulos |
| BD completamente lista | ✅ V1 + V2 | Sin migraciones pendientes |
| Motor sync | ⚠️ Tabla lista, sin servicio | No urgente hasta tener módulos de dominio |

---

*Informe generado con base en revisión estática del repositorio — Junio 2026*
