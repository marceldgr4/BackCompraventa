# 📚 DOCUMENTACIÓN FINAL BACKEND — Sistema CompraVenta

## 1. Resumen Ejecutivo
El sistema **CompraVenta** es una plataforma híbrida de gestión diseñada para casas de empeño y negocios de compraventa de artículos. El backend proporciona una API REST robusta que gestiona el ciclo de vida completo de los activos: desde la adquisición (Compras) y el financiamiento (Empeños) hasta la liquidación final (Ventas).

El sistema implementa una arquitectura **offline-first**, permitiendo que aplicaciones cliente (como la aplicación Desktop Swing o clientes web) operen localmente y sincronicen sus transacciones con una nube centralizada mediante el patrón **Outbox**, asegurando la integridad de los datos incluso en entornos de conectividad intermitente.

---

## 2. Arquitectura Final

### 2.1 Stack Tecnológico
- **Framework:** Spring Boot 3.4.5 (Java 21)
- **Base de Datos Primaria:** PostgreSQL 16 (con extensiones de UUID y esquema optimizado)
- **Caché y Rate Limiting:** Redis 7.2 (gestión de tokens JWT, control de concurrencia y caché de artículos)
- **Gestión de Base de Datos:** pgAdmin 4 integrado vía Docker
- **Sincronización Cloud:** Supabase (PostgreSQL Cloud)
- **Seguridad:** Spring Security + JWT (JSON Web Tokens) + Bucket4j (Rate Limiting)
- **Migraciones de BD:** Flyway (migración consolidada `V1__schema_completo.sql`)
- **Documentación de API:** SpringDoc OpenAPI 2.8.4 (Swagger UI)

### 2.2 Flujo de Datos y Sincronización
El sistema utiliza un motor de sincronización basado en el **Outbox Pattern**:
1. **Operación Local:** Cada cambio en la base de datos local se registra transaccionalmente en la tabla `sync_outbox`.
2. **Procesamiento:** Un scheduler interno (`SyncEngineService`) detecta periódicamente registros en estado `PENDING`.
3. **Sincronización:** Se transmiten los cambios por lotes a Supabase vía HTTPS utilizando firma criptográfica.
4. **Resolución:** Se aplica una estrategia configurable (`remote-wins` o `local-wins`) para dirimir conflictos de concurrencia.

### 2.3 Estructura del Proyecto
```
Backend/
├── Dockerfile                   # Multi-stage build (Temurin 21 JRE Alpine)
├── Docker-Compose.yml           # Orquestación completa (DB, Redis, pgAdmin, Backend)
├── .env                         # Variables de entorno locales (no versionadas)
├── .env.example                 # Plantilla de configuración de entorno
├── pom.xml                      # Dependencias Maven y configuración del build
├── src/main/java/com/CompraVenta/Backend/
│   ├── 📁 Audit/                # Auditoría transversal (AOP, Entity @Auditable, AuditLog)
│   ├── 📁 Config/               # Configuraciones (Security, CORS, Redis, OpenAPI, Scheduler)
│   ├── 📁 Exception/            # Manejo global de excepciones (@RestControllerAdvice)
│   ├── 📁 Security/             # Filtros JWT, UserDetails y Contexto de Seguridad
│   ├── 📁 Shared/               # Clases base, DTOs genéricos, ApiResponse y Enums
│   ├── 📁 Sync/                 # Motor de Sincronización (Outbox, Scheduler, Supabase Client)
│   └── 📁 Modules/
│       ├── 📁 Articles/         # Inventario y Catálogo de Artículos
│       ├── 📁 Clients/          # Clientes y Proveedores (Registro Completo y Rápido)
│       ├── 📁 Dashboard/        # Métricas, KPIs e Inteligencia de Negocio
│       ├── 📁 Employee/         # Gestión de Empleados, Roles y Autenticación
│       ├── 📁 Pawns/            # Ciclo de Vida de Empeños y Pagos de Cuotas
│       ├── 📁 Purchases/        # Compras a Clientes y Proveedores
│       └── 📁 Sale/             # Ventas de Artículos y Detalles de Factura
└── src/main/resources/
    ├── application.yml          # Configuración principal con defaults para entorno local
    ├── application-production.yml # Configuración optimizada para producción
    └── db/migration/
        └── V1__schema_completo.sql # Esquema DDL inicial y consolidado de Flyway
```

---

## 3. Módulos Implementados

### 🔐 Auth & Security
- **Responsabilidades:** Autenticación por usuario y contraseña, emisión de `AccessToken` (1 hora) y `RefreshToken` (7 días).
- **Control de Intentos:** Protección contra ataques de fuerza bruta en `/api/auth/login` con **Bucket4j** (5 intentos por minuto por IP).

### 👥 Employees & Clients
- **Employees:** Gestión de cuentas internas con roles (`ADMIN`, `EMPLEADO`), encriptación BCrypt y borrado lógico (`is_deleted`).
- **Clients:** Registro completo de personas naturales/jurídicas y soporte para "Clientes Rápidos" (registro expedito en mostrador).

### 📦 Articles (Inventario)
- **Responsabilidades:** Control de catálogo, categorías, números de serie, precios de compra/venta y control de stock.
- **Estados:** `DISPONIBLE`, `EMPEÑADO`, `VENDIDO`, `RETIRADO`, etc.

### 💍 Pawns (Empeños)
- **Responsabilidades:** Registro de contratos de empeño, cálculo de intereses, cuotas y fechas de vencimiento.
- **Cobranzas:** Registro de pagos parciales o totales de cuotas y retiro o liquidación del bien.

### 💰 Sales & Purchases
- **Purchases:** Adquisición de artículos a clientes o proveedores, generando el alta en inventario.
- **Sales:** Facturación y venta directa de artículos con validación de stock y cálculo de ingresos.

### 📊 Dashboard
- **Responsabilidades:** Agregación de KPIs en tiempo real: ingresos diarios/mensuales, porcentaje de artículos empeñados vs vendidos y alertas de stock.

### 🔄 Sync Engine
- **Responsabilidades:** Desacopla la persistencia local de la nube. Si no hay conexión a internet, las operaciones se registran localmente y se transmiten automáticamente cuando el enlace se reanuda.

---

## 4. Modelo de Seguridad

### 4.1 Implementación JWT
- Cada petición a rutas protegidas requiere la cabecera `Authorization: Bearer <token>`.
- El filtro `JwtAuthenticationFilter` extrae el claim, valida la firma criptográfica HMAC-SHA256 (`JWT_SECRET`) y establece el `SecurityContext`.

### 4.2 Control de Acceso Basado en Roles (RBAC)
- **`ROLE_ADMIN`:** Acceso total (gestión de empleados, reportes de auditoría, anulación de transacciones).
- **`ROLE_EMPLEADO`:** Operaciones transaccionales de caja (ventas, compras, empeños y consulta de catálogo).

### 4.3 Cabeceras de Seguridad y Auditoría
- **Cabeceras HTTP:** Se aplican `Content-Security-Policy`, `X-Frame-Options: DENY` y `Strict-Transport-Security` (HSTS).
- **Auditoría Transversal:** Métodos anotados con `@Auditable` disparan el `AuditAspect` que registra usuario, fecha, operación y diferencias antes/después en `audit_log`.

---

## 5. Infraestructura y Despliegue con Docker

El archivo `Docker-Compose.yml` orquesta los 4 servicios esenciales:

| Servicio | Contenedor | Imagen | Puerto Host:Contenedor | Descripción |
|---|---|---|---|---|
| **db** | `compraventa-postgres` | `postgres:16-alpine` | `5432:5432` | Motor de base de datos PostgreSQL |
| **redis** | `compraventa-redis` | `redis:7.2-alpine` | `6379:6379` | Caché en memoria y rate limiting |
| **pgadmin** | `compraventa-pgadmin` | `dpage/pgadmin4` | `8081:80` | Cliente web de administración de BD |
| **backend** | `compraventa-backend` | `compraventa-backend:latest` | `8080:8080` | API REST Spring Boot (Java 21) |

---

## 6. Variables de Entorno (`.env`)

Copia el archivo `.env.example` a `.env` en el directorio `Backend/`:

```bash
cp Backend/.env.example Backend/.env
```

| Variable | Valor por Defecto Local | Descripción |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Perfil de configuración de Spring Boot (`local` o `production`) |
| `LOCAL_DB_URL` | `jdbc:postgresql://localhost:5432/compraventa` | URL JDBC (usar `db` en lugar de `localhost` si el backend corre dentro de Docker) |
| `LOCAL_DB_USER` | `compraventa` | Usuario de base de datos |
| `LOCAL_DB_PASSWORD` | `compraventa_pass_2026` | Contraseña del usuario de base de datos |
| `PGADMIN_EMAIL` | `admin@compraventa.com` | Email de inicio de sesión en pgAdmin |
| `PGADMIN_PASSWORD` | `admin123` | Contraseña de inicio de sesión en pgAdmin |
| `REDIS_HOST` | `localhost` | Host de Redis (`redis` dentro de Docker) |
| `REDIS_PORT` | `6379` | Puerto de conexión a Redis |
| `REDIS_PASSWORD` | `redis_pass_2026` | Contraseña requerida por Redis |
| `JWT_SECRET` | *(Clave alfanumérica de 64+ caracteres)* | Secreto criptográfico HMAC-SHA256 |
| `JWT_EXPIRATION_MS` | `3600000` (1h) | Duración del token de acceso |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7d) | Duración del refresh token |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Orígenes permitidos para clientes frontend |
| `SERVER_PORT` | `8080` | Puerto en el que escucha la API |
| `SYNC_ENABLED` | `false` | Activa (`true`) o desactiva (`false`) el motor de sincronización Supabase |
| `SUPABASE_URL` | *(Opcional)* | URL del proyecto Supabase Cloud |
| `SUPABASE_SERVICE_ROLE_KEY` | *(Opcional)* | Service Role Key con permisos de escritura |

---

## 7. Guía Operativa de Despliegue

### Modalidad A: Desarrollo Híbrido (Recomendada para programar)
Permite tener la base de datos, Redis y pgAdmin en Docker, mientras compilas y depuras la API en tu IDE o consola:

1. **Levantar la infraestructura auxiliar:**
   ```bash
   cd Backend
   docker compose up -d db redis pgadmin
   ```

2. **Ejecutar la API con Maven:**
   ```bash
   ./mvnw spring-boot:run
   ```

---

### Modalidad B: Despliegue 100% Dockerizado
Ejecuta todo el stack (incluyendo la compilación y ejecución del backend en contenedor):

1. **Construir y levantar todos los servicios:**
   ```bash
   cd Backend
   docker compose up -d --build
   ```

2. **Verificar estado de los contenedores:**
   ```bash
   docker compose ps
   ```

3. **Ver logs del Backend:**
   ```bash
   docker compose logs -f backend
   ```

4. **Detener el entorno:**
   ```bash
   docker compose down
   # Para eliminar volúmenes y reiniciar datos limpios:
   docker compose down -v
   ```

---

## 8. Verificación y Monitoreo del Sistema

Una vez los servicios estén corriendo, puedes verificar cada componente:

### 1. API Health Check
- **URL:** [http://localhost:8080/api/actuator/health](http://localhost:8080/api/actuator/health)
- **Respuesta esperada:**
  ```json
  {"status":"UP"}
  ```

### 2. Documentación Swagger UI
- **URL:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- Permite probar interactivamente todos los endpoints, autenticarse con Bearer Token y visualizar los esquemas DTO.

### 3. Consola Web de pgAdmin
- **URL:** [http://localhost:8081](http://localhost:8081)
- **Login:**
  - Email: `admin@compraventa.com`
  - Contraseña: `admin123`
- **Registro del servidor en pgAdmin:**
  - Clic derecho en **Servers** ➔ **Register** ➔ **Server...**
  - **General:** Name = `CompraVenta`
  - **Connection:**
    - Host name/address: `db` *(⚠️ Usar `db`, no `localhost`)*
    - Port: `5432`
    - Maintenance database: `compraventa`
    - Username: `compraventa`
    - Password: `compraventa_pass_2026`

---

## 9. Endpoints Principales

| Módulo | Endpoint | Método | Acceso | Descripción |
|---|---|---|---|---|
| **Auth** | `/api/auth/login` | `POST` | Público | Autenticación con email y contraseña |
| **Auth** | `/api/auth/refresh` | `POST` | Público | Renovación de token expirado |
| **Actuator** | `/api/actuator/health` | `GET` | Público | Estado de salud del sistema |
| **Swagger** | `/api/swagger-ui.html` | `GET` | Público | Interfaz de documentación OpenAPI |
| **Articles** | `/api/articles` | `GET / POST` | Empleado / Admin | Consulta y registro de inventario |
| **Clients** | `/api/clientes` | `GET / POST` | Empleado / Admin | Directorio de clientes |
| **Pawns** | `/api/pawns` | `GET / POST` | Empleado / Admin | Creación y listado de empeños |
| **Pawns** | `/api/pawns/{id}/payments` | `POST` | Empleado / Admin | Registro de pago de cuotas |
| **Sales** | `/api/sales` | `GET / POST` | Empleado / Admin | Facturación de ventas |
| **Purchases**| `/api/purchases` | `GET / POST` | Empleado / Admin | Registro de compras |
| **Employees**| `/api/employees` | `GET / POST` | Admin | Gestión de personal y roles |
| **Dashboard**| `/api/dashboard` | `GET` | Empleado / Admin | Métricas y KPIs de negocio |
| **Sync** | `/api/sync/status` | `GET` | Admin | Estado del motor Outbox |
