# Guía de Continuidad y Configuración del Proyecto

## 1. Estado actual (septiembre 2026)

Los módulos de dominio **ya están construidos y el proyecto compila**. La guía anterior (Employee vacío, Auth pendiente, Articles/Pawns/Sales por hacer) quedó desactualizada.

| Área | Estado |
|---|---|
| Infraestructura (Config, Security, Audit, Exception, Shared) | ✅ Completo |
| Auth + Employee + Clients + Articles | ✅ Completo |
| Pawns + Sales + **Purchases** | ✅ Completo |
| Flyway | ✅ `V1__schema_completo.sql`, `V2__seed_admin.sql`, `V3__align_base_entity_columns.sql` |
| Motor Sync | ⚠️ Solo entidad/tabla `sync_outbox` — falta el servicio |
| Tests | ❌ Stubs de Initializr |
| Dashboard | ❌ No implementado |

**Siguiente trabajo de producto (no bloquea operar compras/ventas/empeños en local):**
- `SyncEngineService` + scheduler contra Supabase
- Tests unitarios/integración
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
2. Dirígete a la raíz de tu proyecto donde se encuentre el archivo docker compose:
   ```powershell
   cd "C:\Users\Admin\OneDrive - Periferia IT Corp SAS\Documentos\CompraVenta\Backend"
   ```
3. Ejecuta el siguiente comando para levantar los servicios de base de datos y redis en segundo plano (`-d`):
   ```powershell
   docker-compose up -d
   ```
   Si el archivo está dentro de `Backend/`, usa `Docker-Compose.yml` desde esa carpeta.
4. Para validar que todo funciona correctamente, ejecuta:
   ```powershell
   docker ps
   ```
   *Deberías ver contenedores activos para PostgreSQL y Redis.*

### 2.3 Ejecutar el Backend en Modo Desarrollo
Para el día a día, es mejor ejecutar la base de datos en Docker y el código Java de forma local:
1. Navega a la carpeta que contiene el código fuente Java:
   ```powershell
   cd Backend
   ```
2. Una vez que tengas Java 21 activo, puedes iniciar el proyecto ejecutando:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *Tu servidor Spring Boot se levantará y se conectará automáticamente al PostgreSQL de tu contenedor Docker.*
   *Swagger: `http://localhost:8080/api/swagger-ui.html` (context-path `/api`).*

---

## 3. Hoja de ruta (lo que queda)

### Ya hecho
- Schema Flyway + seed admin
- JWT, `UserDetailsServiceImpl`, `AuthController` (`/auth/login`, `/auth/refresh`)
- Módulos: Employees, Clients, Articles, Pawns, Sales, Purchases

### Pendiente
1. Motor de sincronización (`SyncOutboxRepository`, `SyncEngineService`, trigger Admin).
2. Tests (JUnit 5 + Mockito; Auth e inventario como prioridad).
3. Dashboard de métricas (opcional).
