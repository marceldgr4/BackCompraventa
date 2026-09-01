# Módulo Purchases (Compras) — Especificación e implementación

**Estado:** ✅ **IMPLEMENTADO Y COMPILANDO** (septiembre 2026)

**Contexto:** Backend CompraVenta — Spring Boot 3.4.5, Java 21, PostgreSQL 16.  
Los módulos `Clients`, `Articles`, `Pawns` y `Sales` ya estaban implementados. Purchases depende de `Articles` (cada compra crea inventario) y opcionalmente de `Clients` (proveedor). La tabla `purchases` existe desde `V1__schema_completo.sql`.

---

## Arquitectura real (lo construido)

Ubicación: `Modules/Purchases/`

| Capa | Archivos |
|---|---|
| Entidad | `Entity/Purchase.java` — **no** extiende `BaseEntity` (la tabla no tiene `updated_at` ni `is_deleted`) |
| Repositorio | `Repository/PurchaseRepository.java` — `findByGlobalId`, `findByFilters` + `@EntityGraph` |
| DTOs | `CreatePurchaseRequest`, `PurchaseItemRequest`, `PurchaseResponse` |
| Mapper | `Mapper/PurchaseMapper.java` |
| Servicio | `PurchaseService` / `PurchaseServiceImpl` |
| Auxiliares | `ArticleCreationService`, `ClienteResolutionService`, `EmployeeContextService` |
| Controlador | `Controller/PurchaseController.java` — base path `/purchases` |

**Nota de dominio:** 1 fila en `purchases` = 1 artículo comprado. Un `POST` con N ítems crea N artículos y N filas de compra, misma transacción, mismo empleado/cliente/fecha.

---

## Comportamiento implementado

### Registrar compra (`POST /purchases`)
- Resuelve el empleado autenticado (`SecurityContext` + `EmployeeContextService`).
- Cliente: por `clienteGlobalId`, o cliente RAPIDO (`clienteFirstName` + cédula/teléfono; reutiliza cédula existente), o compra anónima.
- Por cada ítem: crea `Article` con `SourceType.COMPRA`, `itemState`, cantidad y precios.
- Si `purchasePrice >= salePrice` → `WARN` en log; **no** aborta la transacción.
- `@Transactional` + `@Auditable(operation = "CREATE_PURCHASE", entity = "purchases")`.

### Listar (`GET /purchases`)
- Filtros opcionales: `clienteGlobalId`, `dateFrom`, `dateTo`, paginación.
- EMPLEADO ve solo las suyas; ADMIN ve todas.

### Detalle (`GET /purchases/{globalId}`)
- EMPLEADO solo si es el dueño; ADMIN siempre.

### Anular (`DELETE /purchases/{globalId}`)
- Solo ADMIN (`@PreAuthorize("hasRole('ADMIN')")`).
- Borrado físico de la compra (no hay `is_deleted` en la tabla).
- Elimina el artículo asociado si no tiene ventas ni empeños activos.
- `@Auditable(operation = "DELETE_PURCHASE", entity = "purchases")`.

Respuestas: `ApiResponse.ok(...)` / `ApiResponse.success(...)` (alias) y `PageResponse.from(...)`.

---

## Endpoints

| Método | Ruta | Notas |
|---|---|---|
| `GET` | `/purchases` | Paginado y filtros |
| `GET` | `/purchases/{globalId}` | Detalle de una fila (un artículo) |
| `POST` | `/purchases` | Lista de artículos → N compras + N artículos |
| `DELETE` | `/purchases/{globalId}` | Solo ADMIN |

---

## Correcciones aplicadas al implementar

- Servicio incompleto: faltaban helpers, `registerSingleItemPurchase` y el método coincidía mal con la interfaz (`findByGloabalId`).
- Faltaba `PurchaseController`.
- Entidad: `@Builder` sin `@AllArgsConstructor`; índice `puchases_date` → `purchase_date`.
- Tras el `save`, se asignan asociaciones (`employee`, `cliente`, `article`) para que el mapper no devuelva nulos.
- `Article.clienteId` nullable para compras anónimas.
- Migración `V3__align_base_entity_columns.sql` (`is_deleted` / `updated_at` en tablas que mapean `BaseEntity`).
- Procesador Lombok en `maven-compiler-plugin` (el proyecto no compilaba con Maven).
