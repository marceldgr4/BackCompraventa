# Guía de Continuidad y Configuración del Proyecto

## 1. Estado actual (15 septiembre 2026)

Los módulos de dominio **están construidos**, el motor Sync **MVP está operativo** y el proyecto **compila**. Los nombres de configuración son lowercase para Linux/Docker.

| Área | Estado |
|---|---|
| Infraestructura (Config, Security, Audit, Exception, Shared) | ✅ Completo |
| Auth + Employee + Clients + Articles | ✅ Completo |
| Pawns + Sales + Purchases | ✅ Completo |
| Flyway | ✅ `V1`–`V5` (V4/V5 alinean enums pawn/source y expiración) |
| Motor Sync | ✅ MVP: outbox + scheduler + `/sync/status` + `/sync/trigger` |
| Tests | ⚠️ Unitarios de Clients y Sync; faltan IT/Auth/Articles |
| Dashboard | ❌ No implementado |

**Siguiente trabajo de producto (no bloquea operar compras/ventas/empeños en local):**
- Tests de integración (Auth, inventario)
- Descarga remota desde Supabase (el upload del outbox ya existe)
- Endpoint de dashboard/KPIs (opcional)

Detalle de Purchases: ver `PROMPT_MODULO_PURCHASES.md`.  
Análisis de avance: `INFORME_ANALISIS_COMPRAVENTA.md`.

---

## 2. Configuración de Docker (Paso a Paso)

Para poder levantar tu entorno de base de datos local (PostgreSQL) y Redis sin instalar programas directamente en tu máquina, usaremos Docker.

### 2.1 Requisitos Previos
1. **Docker Desktop:** Asegúrate de tener [Docker Desktop](https://www.docker.com/products/docker-desktop/) descargado, instalado y abierto en tu máquina.
2. **Actualizar a Java 21:** Para compilar el código Spring Boot, tu entorno de variables de sistema (`JAVA_HOME`) debe apuntar al JDK 21.

### 2.2 Levantar la Infraestructura (Bases de datos)
En tu proyecto ya cuentas con un archivo `docker-compose.yml`. Sigue estos pasos:
1. Abre tu **PowerShell**.
2. Dirígete a la carpeta del backend (donde está `docker-compose.yml`):
   ```powershell
   cd "C:\Users\Admin\OneDrive - Periferia IT Corp SAS\Documentos\CompraVenta\Backend\Backend"
   ```
3. Ejecuta el siguiente comando para levantar PostgreSQL y Redis en segundo plano (`-d`):
   ```powershell
   docker compose up -d
   ```
4. Para validar que todo funciona correctamente, ejecuta:
   ```powershell
   docker ps
   ```
   *Deberías ver contenedores activos para PostgreSQL y Redis.*

### 2.3 Ejecutar el Backend en Modo Desarrollo
Para el día a día, es mejor ejecutar la base de datos en Docker y el código Java de forma local:
1. Navega a la carpeta que contiene el código fuente Java:
   ```powershell
   cd "C:\Users\Admin\OneDrive - Periferia IT Corp SAS\Documentos\CompraVenta\Backend\Backend"
   ```
2. Una vez que tengas Java 21 activo, puedes iniciar el proyecto ejecutando:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *Tu servidor Spring Boot se levantará y se conectará automáticamente al PostgreSQL de tu contenedor Docker.*
   *Swagger: `http://localhost:8080/api/swagger-ui.html` (context-path `/api`).*

**Notas:**
- Define `JWT_SECRET` (≥ 32 caracteres) en `.env` o el entorno. En perfil `production` no se admite el placeholder `CHANGE_ME`.
- Sync local va apagado por defecto (`SYNC_ENABLED=false`). Para subir el outbox a Supabase: claves reales + `SYNC_ENABLED=true`.
- Si Flyway ya había aplicado un `V1` antiguo, el checksum puede fallar: `flyway repair` y dejar correr `V4`/`V5`.

---

## 3. Hoja de ruta (lo que queda)

### Ya hecho
- Schema Flyway V1–V5 + seed admin
- JWT (`JwtStartupValidator`), `UserDetailsServiceImpl`, `AuthController`
- Módulos: Employees, Clients, Articles, Pawns, Sales, Purchases
- Sync MVP: `SyncOutboxRepository`, `SyncEngineService`, `SupabaseSyncClient`, `SyncController`
- YAML y Compose en lowercase (`application.yml`, `docker-compose.yml`)

### Pendiente
1. Tests de integración (Auth, inventario) y más unitarios.
2. Dashboard de métricas (opcional).
3. Download remoto Supabase (opcional respecto al upload del outbox).
