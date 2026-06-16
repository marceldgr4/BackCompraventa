# Informe de Auditoría Técnica — CompraVenta Backend
**Fecha:** Junio 2026 | **Stack:** Java 21 · Spring Boot 3.4.5 · PostgreSQL 16 · Redis · Flyway

---

## 1. Elementos Verificados

| Área | Archivos revisados |
|---|---|
| Módulo Clientes | Entity, Enums, DTOs, Repository, Mapper, Service, ServiceImpl, Controller |
| Módulo Empleados | Entity, Mapper, Repository, Service, ServiceImpl, Controller |
| Módulo Auth | AuthController, AuthServiceFacade, AuthenticationServiceImpl, TokenServiceImpl, LoginRateLimitService |
| Seguridad | JwtAuthenticationFilter, JwtService, CustomUserDetails, UserDetailsServiceImpl, SecurityContext |
| Infraestructura | BaseEntity, ApiResponse, PageResponse, ErrorDetail, GlobalExceptionHandler |
| Configuración | SecurityConfig, RedisConfig, JacksonConfig, CorsConfig, SchedulingConfig, application.yml |
| Base de datos | V1__initial_schema.sql, V2__add_sync_triggers.sql, V3__seed_default_admin.sql, V4__add_missing.sql |
| Auditoría | AuditAspect, AuditLog, AuditRepository, @Auditable |
| Sync | SyncOutbox, SyncStatus |

---

## 2. Aspectos que Cumplen Correctamente ✅

- **Auth module**: patrón Facade bien aplicado (AuthServiceFacade, AuthenticationServiceImpl, TokenServiceImpl), separación correcta de responsabilidades.
- **JWT**: generación, validación, refresh token rotation y blacklist en Redis funcionan correctamente.
- **Brute-force protection**: LoginRateLimitService con TTL en Redis, manejo correcto de degradación cuando Redis no está disponible.
- **EmployeeModule**: CRUD completo, @Auditable en operaciones críticas, @PreAuthorize bien aplicado, SecurityContext usado correctamente en setActive().
- **GlobalExceptionHandler** (hardler/): maneja todos los casos relevantes incluyendo LockedException y DisabledException.
- **ApiResponse / PageResponse**: wrappers genéricos bien diseñados, usados consistentemente en Employee y Auth.
- **SecurityConfig**: JWT stateless, @EnableMethodSecurity, endpoints públicos correctamente declarados.
- **JwtAuthenticationFilter**: verifica blacklist en Redis antes de autenticar, degradación si Redis no disponible.
- **Flyway migrations**: V1 crea todo el schema, V2 agrega triggers de sync y SP register_sale, V3 seed admin, V4 índices adicionales.
- **SchedulingConfig**: ThreadPoolTaskScheduler separado del thread principal, no bloquea HTTP.
- **Docker Compose**: stack completo con health checks, PostgreSQL 16, Redis, pgAdmin.
- **AuditAspect**: captura before/after, IP, employeeId con AOP — no contamina lógica de negocio.

---

## 3. Problemas Detectados y Correcciones

### 🔴 CRÍTICO — Impiden compilar o bloquean el arranque

---

#### BUG-01: `@GetMapping("/{globalId"`) — brace de cierre faltante
**Archivo:** `ClienteController.java`
**Problema:** `@GetMapping("/{globalId")` es un string inválido para Spring MVC — falta el `}`.  
**Efecto:** Error de arranque (`IllegalStateException`) al registrar los request mappings.  
**Corrección:** `@GetMapping("/{globalId}")`

---

#### BUG-02: `findByGloblaId()` — typo en el nombre del método
**Archivo:** `ClienteRepository.java` y `ClienteServiceImpl.java`  
**Problema:** Spring Data JPA genera la query a partir del nombre del método. `findByGloblaId` no coincide con ningún campo de la entidad → error en startup al validar el repositorio.  
**Corrección:** `findByGlobalId(UUID globalId)`

---

#### BUG-03: `exitsByCedulaAndIdNot()` — typo, método inexistente
**Archivo:** `ClienteRepository.java`  
**Problema:** `exits` (salir) en lugar de `exists`. Spring Data no puede resolver el método → startup failure.  
**Corrección:** `existsByCedulaAndIdNot(String cedula, Long id)`

---

#### BUG-04: JPQL `CONCAT('%', term, '%')` — falta el colon del parámetro
**Archivo:** `ClienteRepository.java` — método `searchByTerm`  
**Problema:** `term` sin `:` es tratado como literal string, no como parámetro vinculado. La query siempre busca el string literal "term" en lugar del valor real.  
**Corrección:** `CONCAT('%', :term, '%')`

---

#### BUG-05: `BaseEntity` tiene `@Column(name = "deleted")` — columna inexistente en el schema
**Archivo:** `BaseEntity.java`  
**Problema:** La tabla `clientes` (y todas las heredadas de BaseEntity) en V1 NO tiene columna `deleted`. Con `ddl-auto: validate`, Hibernate lanza `SchemaManagementException: Missing column: deleted in table clientes` bloqueando el arranque.  
**Corrección:** Eliminar el campo `deleted` de `BaseEntity`. El soft delete de cada entidad se maneja con su propio campo de estado (`ClienteStatus`, `PawnStatus`, etc.).

---

#### BUG-06: `@Column(name = "addres")` — typo, columna incorrecta
**Archivo:** `Cliente.java`  
**Problema:** El nombre de columna tiene un typo. Hibernate intentará mapear al campo `addres` que no existe en la tabla → `SchemaManagementException`.  
**Corrección:** `@Column(name = "address", length = 255)`

---

#### BUG-07: `@Column(name = "register_type")` — nombre de columna incorrecto
**Archivo:** `Cliente.java`  
**Problema:** En V1 la columna se llama `registration_type`, no `register_type`. Hibernate no encuentra la columna → startup failure.  
**Corrección:** `@Column(name = "registration_type", nullable = false, length = 20)`

---

#### BUG-08: Dos `GlobalExceptionHandler` en paquetes distintos
**Archivos:** `Exception/handler/GlobalExceptionHandler.java` y `Exception/hardler/GlobalExceptionHandler.java`  
**Problema:** Hay dos clases con `@RestControllerAdvice` del mismo tipo. Spring detecta un bean duplicado al arrancar → `NoUniqueBeanDefinitionException`.  
**Corrección:** Eliminar `Exception/handler/GlobalExceptionHandler.java` (versión incompleta). Conservar `Exception/hardler/GlobalExceptionHandler.java` (versión completa con LockedException y DisabledException).  
**Acción adicional:** Renombrar el paquete `hardler` → `handler` para corregir el typo (sin urgencia de compilación, pero afecta mantenibilidad).

---

### 🟠 ALTO — Bugs de lógica que producen comportamiento incorrecto

---

#### BUG-09: `create()` con `@Transactional(readOnly = true)`
**Archivo:** `ClienteServiceImpl.java`  
**Problema:** Con `readOnly = true`, el contexto de persistencia de JPA no rastrea cambios para flush. El `clienteRepository.save()` no persiste nada — el cliente se "guarda" solo en memoria y se descarta al finalizar la transacción.  
**Corrección:** `@Transactional` (sin readOnly)

---

#### BUG-10: `ClienteMapper.toEntity()` es `static` pero se llama como instancia en el Service
**Archivo:** `ClienteMapper.java` y `ClienteServiceImpl.java`  
**Problema:** `toEntity()` es `static`, pero `ClienteServiceImpl` lo llama como `ClienteMapper.toEntity(request)` (estático). Esto viola el patrón de inyección de dependencias — si en el futuro `toEntity()` necesita algún servicio inyectado, se rompe. Además es inconsistente: los otros métodos del mapper no son estáticos.  
**Corrección:** Eliminar `static` de `toEntity()` y llamarlo como `clienteMapper.toEntity(request)`.

---

#### BUG-11: `promoteToComplete()` — lógica invertida
**Archivo:** `Cliente.java`  
**Problema:** La condición original era `this.lastName.isBlank()`. Promueve a COMPLETO cuando el apellido está **vacío**, que es exactamente lo contrario de lo que debería hacer.  
**Corrección:** `!this.lastName.isBlank()`

---

#### BUG-12: `ClienteResponse` tiene campo `GlobalId` en PascalCase
**Archivo:** `ClienteResponse.java`  
**Problema:** El record define `UUID GlobalId`. El accessor generado es `GlobalId()` y Jackson serializa la clave JSON como `"GlobalId"` (con mayúscula), en lugar de `"globalId"`. El `ClienteController` llama `created.GlobalId()` que compila pero produce JSON no-estándar.  
**Corrección:** Renombrar a `globalId` — accessor pasa a ser `globalId()` y JSON produce `"globalId"`.  
También corregir `createAt`/`updateAt` → `createdAt`/`updatedAt` para coincidir con getters de `BaseEntity`.

---

#### BUG-13: `ClienteController` sin `@RequestMapping` path
**Archivo:** `ClienteController.java`  
**Problema:** `@RequestMapping` sin path. Todos los endpoints del módulo (GET /, GET /{globalId}, POST /, etc.) se registran en la raíz `/api/*` sin el prefijo `/clientes`. Colisiona con otros controladores y rompe el contrato REST documentado.  
**Corrección:** `@RequestMapping("/clientes")`

---

### 🟡 MEDIO — Inconsistencias que afectan correctitud o mantenibilidad

---

#### BUG-14: `existsByPhoneNumber()` — campo inexistente
**Archivo:** `ClienteRepository.java`  
**Problema:** El campo en la entidad se llama `phone`, no `phoneNumber`. Spring Data no puede resolver el método → startup failure.  
**Corrección:** `existsByPhone(String phone)`

---

#### BUG-15: `@Column(name = "phone", length = 255)` — length incorrecto
**Archivo:** `Cliente.java`  
**Problema:** V1 define `phone TEXT` con índice. Aunque en PostgreSQL TEXT es ilimitado, la entidad debería declarar `length = 20` para consistencia con las validaciones del DTO (`@Pattern` limita a 20 chars) y el schema conceptual.  
**Corrección:** `@Column(name = "phone", length = 20)`

---

#### BUG-16: V1 `DEFAULT 'Activo'` inconsistente con enum `('ACTIVO','INACTIVO')`
**Archivo:** `V1__initial_schema.sql`  
**Problema:** El tipo enum PostgreSQL define `('ACTIVO','INACTIVO')` pero el `DEFAULT` de la columna usa `'Activo'` (PascalCase). PostgreSQL lanzará error de constraint en cada INSERT sin status explícito.  
**Corrección:** Migración `V5__fix_cliente_status_default.sql` que cambia el DEFAULT a `'ACTIVO'` y corrige registros existentes.

---

#### BUG-17: `@Auditable` ausente en operaciones críticas de Clientes
**Archivo:** `ClienteServiceImpl.java`  
**Problema:** `create()`, `update()` y `delete()` no tienen `@Auditable`, aunque `EmployeeServiceImpl` sí los tiene. Viola la regla RF-09.1: "todas las operaciones críticas se registran en audit_log".  
**Corrección:** Agregar `@Auditable(operation = "CREATE_CLIENTE", entity = "clientes")` etc.

---

#### BUG-18: `LoginRateLimitService` sin `@Slf4j`
**Archivo:** `LoginRateLimitService.java`  
**Problema:** El método `checkLoginAttempts()` llama `log.warn()`, pero sin `@Slf4j` el campo `log` no existe → `NullPointerException` en runtime cuando Redis no está disponible.  
**Corrección:** Agregar `@Slf4j` en la clase + aplicar el mismo patrón de resilencia a `incrementAttempts()` y `resetAttempts()`.

---

### 🔵 BAJO — Deuda técnica y buenas prácticas

---

#### BUG-19: Paquete `Emus` en lugar de `Enums`
**Archivos:** todos los archivos en `Modules/Clients/Emus/`  
**Problema:** Typo en el nombre del paquete. No impide la compilación pero genera confusión.  
**Corrección recomendada:** Renombrar a `Enums` (requiere refactoring de todos los imports — hacer en una sola sesión).

---

#### BUG-20: `SyncOutbox` y `SyncStatus` en paquete raíz `Sync`
**Problema:** Deberían estar en subpaquetes siguiendo la convención del proyecto (`Sync/Entity`, `Sync/Repository`).  
**Corrección recomendada:** Mover en la siguiente iteración, no urgente.

---

#### BUG-21: `Employee.role` es `getRol()` en Java pero `role` en algunos lugares
**Problema menor:** `Employee.java` tiene el campo anotado `@Column(name = "rol")` y el getter es `getRol()`. `AuthenticationServiceImpl` y `TokenServiceImpl` llaman `employee.getRol()` ✓. Esto es consistente internamente pero poco idiomático en Java (debería ser `getRole()`). No produce bugs, pero dificulta la lectura.  
**Prioridad:** Baja — renombrar en futura iteración con refactoring cuidadoso.

---

#### BUG-22: `CreateClienteRequest` tiene `@Size(max=10)` en cédula y `@Size(max=20)` en nombre
**Problema:** Las cédulas colombianas tienen hasta 10 dígitos ✓. Pero `@Size(max=20)` en `firstName` es muy restrictivo — nombres como "María de los Ángeles" tienen más de 20 caracteres.  
**Corrección recomendada:** `@Size(max = 100)` en `firstName` y `lastName`, consistente con la definición de columna en V1 y las validaciones de Employee.

---

## 4. Resumen de Correcciones por Archivo

| Archivo | Bugs corregidos | Prioridad |
|---|---|---|
| `Cliente.java` (entity) | BUG-06, BUG-07, BUG-11, BUG-15 | 🔴 Crítico |
| `ClienteRepository.java` | BUG-02, BUG-03, BUG-04, BUG-14 | 🔴 Crítico |
| `ClienteServiceImpl.java` | BUG-09, BUG-10, BUG-17 | 🔴/🟠 |
| `ClienteController.java` | BUG-01, BUG-13 | 🔴 Crítico |
| `ClienteResponse.java` | BUG-12 | 🟠 Alto |
| `ClienteMapper.java` | BUG-10 | 🟠 Alto |
| `BaseEntity.java` | BUG-05 | 🔴 Crítico |
| `LoginRateLimitService.java` | BUG-18 | 🟠 Alto |
| `V5__fix_cliente_status_default.sql` | BUG-16 | 🟡 Medio |
| `GlobalExceptionHandler` duplicado | BUG-08 | 🔴 Crítico |

---

## 5. Próximos Pasos Sugeridos

### Inmediato (antes de intentar levantar la app)
1. Aplicar los 7 archivos corregidos del módulo Clientes
2. Eliminar `Exception/handler/GlobalExceptionHandler.java` (duplicado)
3. Agregar `V5__fix_cliente_status_default.sql` a las migraciones
4. Verificar que la app arranca con `./mvnw spring-boot:run`

### Corto plazo (próximo módulo)
5. Implementar módulo **Articles** (siguiente en la secuencia de dominio)
6. Renombrar paquete `Emus` → `Enums` como parte del scaffolding del módulo Articles
7. Corregir `@Size` en DTOs de clientes (max=100 en firstName/lastName)

### Mediano plazo
8. Módulos **Pawns**, **Sales**, **Purchases** siguiendo el mismo patrón
9. Implementar `SyncEngineService` con el `@Scheduled` y el cliente HTTP de Supabase
10. Implementar Dashboard endpoint con métricas KPI
11. Tests unitarios para `ClienteServiceImpl` y `AuthenticationServiceImpl`

### Arquitectura futura
12. Mover `SyncOutbox`/`SyncStatus` a subpaquetes adecuados
13. Considerar renombrar `Employee.rol` → `Employee.role` en una iteración dedicada de refactoring