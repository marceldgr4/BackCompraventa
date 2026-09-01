# Módulo Pawns (Empeños) — Especificación e implementación

**Estado:** ✅ **IMPLEMENTADO**

**Contexto:** Backend CompraVenta — Spring Boot 3.4.5, Java 21, PostgreSQL 16.  
Depende de `Clients` y `Articles`. Tablas `pawns` y `pawn_payments` + `fn_expire_overdue_pawns()` en el esquema inicial.

El texto siguiente es la especificación original. El módulo ya está construido; al cerrar Purchases se corrigieron puntos que impedían compilar.

---

## Lo que está en el código

| Capa | Ubicación |
|---|---|
| Enum | `Modules/Pawns/Enums/PawnStatus.java` |
| Entidades | `Pawn.java`, `PawnPayment.java` |
| Repositorios | `PawnRepository`, `PawnPaymentRepository` |
| DTOs | `CreatePawnRequest`, `CreateAgilePawnRequest`, `PawnPaymentRequest`, responses |
| Mapper | `PawnMapper` |
| Servicio | `PawnService` / `PawnServiceImpl` |
| Controlador | `PawnController` — `/pawns` |

**Comportamiento:** empeño normal (descuenta stock), empeño ágil (cliente RAPIDO + artículo + empeño), pagos, cuota impagada (ADMIN), marcar retirado/perdido, expiración `@Scheduled` → `fn_expire_overdue_pawns()`.

**Endpoints:** `GET /pawns`, `GET /pawns/{globalId}`, `POST /pawns`, `POST /pawns/agile`, `POST /pawns/{globalId}/payments`, `GET /pawns/{globalId}/payments`, `POST /pawns/{globalId}/missed-installments` (ADMIN), `PATCH /pawns/{globalId}/return`, `PATCH /pawns/{globalId}/lost` (ADMIN).

**Correcciones de compilación (septiembre 2026):**
- `@Auditable(action = ...)` → `@Auditable(operation = ...)` (el atributo de la anotación es `operation`).
- `SourceType.EMPENO` → `SourceType.EMPEÑO` (valor del enum Java).
- `ApiResponse.success(...)` añadido como alias de `ok(...)`.

---

### Especificación original (referencia)

#### 1. Enums y entidades
`PawnStatus`: Activo, Vencido, Finalizado, Retirado, Perdido, Vendido (alinear con BD). `Pawn` y `PawnPayment`.

#### 2. Máquina de estados en `Pawn`
`canAcceptPayments()`, `isTerminalState()`, `canBeMarkedReturned()`, `validateStateTransition(...)`.

#### 3–6. Repositorio, DTOs, servicio, controlador
Crear empeño, empeño ágil en una transacción, pagos, devolución, job de expiración, RBAC ADMIN en impagos/reversiones.
