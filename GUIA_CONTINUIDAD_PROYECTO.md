# Guía de Continuidad y Configuración del Proyecto

## 1. Estado Actual: ¿Qué falta construir?

Hemos actualizado el archivo principal `ANALISIS_INTEGRAL_COMPRAVENTA.md`. Las tareas de refactorización estructural, errores de compilación y convenciones que corregimos ya están subrayadas/tachadas como **✅ COMPLETADO**.

**Lo que falta construir para poder operar el backend (Siguientes Pasos Críticos):**

*   **`Employee` Entity + Flyway:** Crear la entidad principal de empleado, su repositorio y los scripts iniciales de migración de Flyway para crear la estructura real en la base de datos.
*   **Servicios de Seguridad Core:**
    *   Implementar `UserDetailsServiceImpl` (que en este momento está vacío y causará un error en ejecución).
    *   Desarrollar el `SecurityContextHelper` para acceder al usuario autenticado.
    *   Construir el `AuthController` y `AuthServiceImpl` (endpoints de `/login` y `/register`).
*   **Módulos de Negocio Restantes:** Empezar con el desarrollo de Articles, Clients, Pawns (Empeños) y Sales.
*   **Servicio de Sincronización:** Construir la lógica offline (`SyncService` y `SyncScheduler`).

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

---

## 3. Hoja de Ruta Inmediata (Próximos Pasos de Trabajo)

Una vez que tengas Docker configurado y Java 21 listo, este es el orden de acciones a seguir para programar lo que falta:

### 🎯 Paso 1: Configurar Migraciones y Entidades (Flyway)
- Crear el archivo `V1__init_schema.sql` en `src/main/resources/db/migration/`.
- Crear la tabla `employee` y la tabla `audit_log` en SQL puro.
- Implementar la clase `@Entity` de `Employee.java`.

### 🎯 Paso 2: Conectar la Seguridad
- Completar `UserDetailsServiceImpl` para que busque el `Employee` desde la base de datos (inyección del repositorio de empleados).
- Asegurarse de que el Filtro JWT esté validando tokens contra la entidad Employee.

### 🎯 Paso 3: Endpoints de Acceso y Validación
- Crear `AuthController` exponiendo la autenticación.
- Usar Postman o el entorno Swagger (`http://localhost:8080/swagger-ui.html`) integrado para pedir un Token.
- Realizar pruebas verificando que el log de auditoría se guarda correctamente al realizar acciones con un token válido.
