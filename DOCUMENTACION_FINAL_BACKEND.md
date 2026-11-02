# 📚 DOCUMENTACIÓN FINAL BACKEND — Sistema CompraVenta

## 1. Resumen Ejecutivo
El sistema **CompraVenta** es una plataforma híbrida de gestión diseñada para casas de empeño y negocios de compraventa de artículos. El backend proporciona una API REST robusta que gestiona el ciclo de vida completo de los activos: desde la adquisición (Compras) y el financiamiento (Empeños) hasta la liquidación final (Ventas).

El sistema implementa una arquitectura **offline-first**, permitiendo que aplicaciones cliente (como la aplicación Desktop Swing) operen localmente y sincronicen sus transacciones con una nube centralizada mediante un patrón de *Outbox*, asegurando la integridad de los datos incluso en entornos de conectividad intermitente.

---

## 2. Arquitectura Final

### 2.1 Stack Tecnológico
- **Framework:** Spring Boot 3.x (Java 21)
- **Base de Datos Primaria:** PostgreSQL 16
- **Caché y Sesiones:** Redis (para gestión de tokens JWT y caché de artículos)
- **Sincronización Cloud:** Supabase (PostgreSQL Cloud)
- **Seguridad:** Spring Security + JWT (JSON Web Tokens)
- **Migraciones:** Flyway

### 2.2 Flujo de Datos y Sincronización
El sistema utiliza un motor de sincronización basado en el **Outbox Pattern**:
1. **Operación Local:** Cada cambio en la base de datos local se registra simultáneamente en una tabla `sync_outbox`.
2. **Procesamiento:** Un scheduler interno detecta registros `PENDING`.
3. **Sincronización:** El `SyncEngineService` envía los cambios a Supabase vía HTTPS.
4. **Resolución:** Se aplica una estrategia de `remote-wins` para resolver conflictos de datos.

### 2.3 Estructura del Proyecto
```
Backend/
├── src/main/java/com/CompraVenta/Backend/
│   ├── 📁 Audit/            # Auditoría transversal (Aspectos y Logs)
│   ├── 📁 Config/          # Configuraciones técnicas (CORS, Redis, OpenAPI)
│   ├── 📁 Exception/       # Manejo global de errores y excepciones de negocio
│   ├── 📁 Security/        # JWT, Filtros de Seguridad y Contexto de Usuario
│   ├── 📁 Shared/          # DTOs comunes, BaseEntity y Enums globales
│   ├── 📁 Sync/            # Motor de Sincronización y Clientes Supabase
│   ├── 📁 Auth/            # Gestión de Autenticación y Login
│   ├── 📁 Employees/        # Gestión de Personal y Roles
│   ├── 📁 Articles/         # Inventario y Catálogo de Artículos
│   ├── 📁 Pawns/           # Gestión de Empeños y Pagos
│   ├── 📁 Sales/           # Gestión de Ventas y Facturación
│   ├── 📁 Purchases/        # Gestión de Compras a Proveedores/Clientes
│   └── 📁 Dashboard/       # Métricas, KPIs y Reportes (Implementado)
└── resources/
    ├── application.yml     # Configuración base
    └── db/migration/       # Scripts de Flyway (V1-V6)
```

---

## 3. Módulos Implementados

### 🔐 Auth & Security
- **Responsabilidades:** Login, renovación de tokens (Refresh Token) y cierre de sesión.
- **Flujo:** Emite un `AccessToken` (1h) y un `RefreshToken` (7d). Valida credenciales contra la base de datos local.

### 👥 Employees & Clients
- **Employees:** Gestión de usuarios del sistema, asignación de roles (`ADMIN`, `EMPLEADO`) y estados de cuenta.
- **Clients:** Registro de clientes y proveedores, incluyendo soporte para "Clientes Rápidos" (registro simplificado).

### 📦 Articles (Inventario)
- **Responsabilidades:** Control de stock, precios de costo/venta y estados del artículo.
- **Integración:** Es el núcleo central; todas las compras, ventas y empeños afectan el estado del artículo.

### 💍 Pawns (Empeños)
- **Responsabilidades:** Gestión de contratos de empeño, seguimiento de cuotas y pagos.
- **Funciones Clave:** Empeños ágiles, marcado de artículos como perdidos/retirados y job automático de expiración de empeños vencidos.

### 💰 Sales & Purchases
- **Sales:** Registro de ventas con validación de stock y anulación lógica.
- **Purchases:** Adquisición de artículos, creación automática de inventario y gestión de compras anónimas.

### 📊 Dashboard
- **Responsabilidades:** Consolidación de métricas operativas, reportes de ventas y KPIs de inventario.

### 🔄 Sync Engine
- **Responsabilidades:** Gestión del Outbox local y comunicación con la API de Supabase para respaldo y centralización de datos.

---

## 4. Modelo de Seguridad

### 4.1 Implementación JWT
- **Autenticación:** Basada en Bearer Tokens. El `JwtAuthenticationFilter` intercepta cada solicitud para validar la firma y expiración del token.
- **Caché de Sesión:** Las sesiones activas se almacenan en Redis para permitir la invalidación inmediata (Logout).

### 4.2 Control de Acceso (RBAC)
Se utiliza `@PreAuthorize` para restringir endpoints basándose en roles:
- **`ROLE_ADMIN`:** Acceso total al sistema, incluyendo gestión de empleados, auditoría y anulación de transacciones.
- **`ROLE_EMPLEADO`:** Acceso a operaciones de mostrador (ventas, compras, empeños) y visualización de sus propias transacciones.

### 4.3 Sistema de Auditoría
Implementado mediante **AOP (Aspect-Oriented Programming)**:
- **`@Auditable`:** Anotación aplicada a métodos sensibles.
- **`AuditAspect`:** Captura automáticamente el usuario, la IP, el valor previo y el valor posterior de la entidad modificada, persistiendo el registro en `audit_log`.

---

## 5. Infraestructura y Despliegue

### 5.1 Dockerización
El proyecto utiliza un despliegue optimizado mediante Docker:
- **Dockerfile:** Multi-stage build basado en `eclipse-temurin:21-jre-alpine` para minimizar la imagen final.
- **Docker Compose:** Levanta la API Backend y la instancia de Redis.
- **PostgreSQL:** Se conecta a una instancia local o remota (Supabase) según la configuración.

### 5.2 Variables de Entorno Requeridas (`.env`)
| Variable | Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`local` o `production`) |
| `LOCAL_DB_URL` | URL de conexión a PostgreSQL |
| `REDIS_PASSWORD` | Contraseña de seguridad para Redis |
| `JWT_SECRET` | Clave secreta para firma de tokens (mín. 64 chars) |
| `SUPABASE_URL` | Endpoint de la instancia de Supabase |
| `SUPABASE_SERVICE_ROLE_KEY` | Clave de administración de Supabase |
| `SYNC_ENABLED` | Activa/Desactiva la sincronización cloud |

---

## 6. Guía Operativa

### 6.1 Ejecución del Proyecto
1. **Levantar Infraestructura:** `docker compose up -d`
2. **Ejecutar Backend:** `./mvnw spring-boot:run`
3. **Acceso a API:** `http://localhost:8080/api`
4. **Documentación:** `http://localhost:8080/api/swagger-ui.html`

### 6.2 Endpoints Principales
| Módulo | Endpoint | Método | Acceso |
|---|---|---|---|
| Auth | `/api/auth/login` | `POST` | Público |
| Articles | `/api/articles` | `GET/POST` | Empleado/Admin |
| Pawns | `/api/pawns` | `GET/POST` | Empleado/Admin |
| Sales | `/api/sales` | `GET/POST` | Empleado/Admin |
| Purchases | `/api/purchases` | `GET/POST` | Empleado/Admin |
| Employees | `/api/employees` | `GET/POST` | Admin |
| Sync | `/api/sync/status` | `GET` | Empleado/Admin |
| Dashboard | `/api/dashboard` | `GET` | Empleado/Admin |
