# Módulo Sales (Ventas) — Especificación e implementación

**Estado:** ✅ **IMPLEMENTADO** (paquete real: `Modules/Sale/`, no `Sales/`)

**Contexto:** Backend CompraVenta — Spring Boot 3.4.5, Java 21, PostgreSQL 16.  
Depende de `Articles` para inventario. Tablas `sales` y `sales_details` + SP `register_sale()` en `V1__schema_completo.sql`.

El texto siguiente es la especificación original. Lo construido ya cubre esas capas.

---

## Lo que está en el código

| Capa | Ubicación real |
|---|---|
| Entidades | `Modules/Sale/Entity/Sale.java`, `SaleDetails.java` |
| Repositorios | `SaleRepository`, `SaleDetailRepository`, `SaleProcedureRepository` (`register_sale`) |
| DTOs | `CreateSaleRequest`, `SaleItemRequest`, `SaleResponse`, `SaleDetailResponse` |
| Mapper | `SaleMapper` |
| Servicio | `SaleService` / `SaleServiceImpl` |
| Controlador | `SaleController` — `/sales` |

**Comportamiento:**
- `create`: valida artículos, invoca `register_sale()`, rollback si falla.
- `findAll`: filtros por cliente y fechas. EMPLEADO ve las propias; ADMIN ve todas.
- `findByGlobalId`: detalle con líneas.
- `delete` (ADMIN): anulación lógica (`is_deleted` vía `V3`) y reposición de stock.

**Endpoints:** `GET /sales`, `GET /sales/{globalId}`, `POST /sales`, `DELETE /sales/{globalId}` (ADMIN).  
Respuestas: `ApiResponse.ok(...)`.

---

### Especificación original (referencia)

#### 1. Entidades
`Sale` y `SaleDetail` extendiendo `BaseEntity`. `Sale` → `Employee` y `Cliente` opcional. `SaleDetail` → `Sale` y `Article`, cantidad y precio unitario.

#### 2. Repositorios
`SaleRepository` con filtros paginados. Método nativo hacia `register_sale()`.

#### 3. DTOs
`CreateSaleRequest` + `SaleItemRequest` (UUID artículo, cantidad). `SaleResponse` con total y detalles.

#### 4. Servicio
`@Transactional` y `@Auditable`. Crear vía SP; listar; anular y devolver stock.

#### 5. Controlador y RBAC
Empleados: solo sus ventas. Admin: global y anulación.
