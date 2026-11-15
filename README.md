# 🏦 CompraVenta Backend API

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

A high-performance REST API designed for Pawn Shop, Inventory, and Point-of-Sale management systems. This backend implements a hybrid architecture with **offline-first synchronization**, rate limiting, role-based security, automated database migrations, and an integrated management dashboard.

---

## ✨ Key Features

### 🛠️ Core Business Modules
- **🔐 Authentication & Authorization:** Stateless JWT authentication (`AccessToken` + `RefreshToken`) with Role-Based Access Control (`ADMIN`, `EMPLEADO`).
- **📦 Article Management:** Complete inventory CRUD with automated stock tracking, barcode/serial management, and categorizations.
- **💍 Pawn System:** End-to-end lifecycle management for pawned goods (Creation $\rightarrow$ Installment payments $\rightarrow$ Redemption / Forfeiture).
- **💰 Sales & Purchases:** Point-of-sale checkout with inventory deduction, alongside customer/supplier purchase records.
- **👥 Client & Employee Management:** Customer directory (including quick-checkout client mode) and staff administration.
- **📈 Business Dashboard:** Real-time metrics including revenue aggregates, active vs. forfeited pawn ratios, and low-inventory alerts.

### 🌐 Advanced Infrastructure
- **🔄 Offline-First Sync:** Implementation of the **Outbox Pattern** to queue local PostgreSQL transactions and sync with Supabase Cloud.
- **⚡ Performance & Rate Limiting:** **Redis 7.2** for distributed caching and **Bucket4j** brute-force protection on authentication endpoints.
- **📝 Audit Logging:** AOP-based auditing capturing the actor, timestamp, IP, and state deltas on sensitive operations.
- **🗄️ Database Migrations:** Version-controlled database schema management using **Flyway**.
- **🖥️ pgAdmin Integration:** Built-in web database manager ready out of the box.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (JDK 21+)
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose**

---

### Installation & Quick Start

#### 1. Clone the repository & Enter directory
```bash
git clone <your-repo-url>
cd BackCompraventa/Backend
```

#### 2. Configure Environment Variables
Copy `.env.example` to create your local `.env`:
```bash
cp .env.example .env
```
*(The default values are already pre-configured to work smoothly in local development).*

---

### 🛠️ Running the Application

You can choose between two execution modes:

#### Option A: Hybrid Development (Recommended for developers)
Run PostgreSQL, Redis, and pgAdmin in Docker, while running the Spring Boot API locally with hot-reload and debugger support:

1. **Start infrastructure containers:**
   ```bash
   docker compose up -d db redis pgadmin
   ```

2. **Launch Spring Boot API:**
   ```bash
   ./mvnw spring-boot:run
   ```

#### Option B: Full Docker Deployment
Run the complete stack including the compiled Spring Boot application inside Docker:

```bash
docker compose up -d --build
```

To stop all services:
```bash
docker compose down
# Or to clean volumes as well:
docker compose down -v
```

---

## 🔍 Verification & Access Points

| Service | URL | Credentials / Notes |
|---|---|---|
| **API Base URL** | `http://localhost:8080/api` | Base context path for all endpoints |
| **API Health Check** | [http://localhost:8080/api/actuator/health](http://localhost:8080/api/actuator/health) | Returns `{"status":"UP"}` |
| **Swagger UI (OpenAPI)** | [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) | Interactive API exploration and test runner |
| **pgAdmin 4 (Web DB)** | [http://localhost:8081](http://localhost:8081) | Email: `admin@compraventa.com` <br> Password: `admin123` |

### 💡 Connecting pgAdmin to PostgreSQL
When opening pgAdmin at `http://localhost:8081`:
1. Right-click **Servers** $\rightarrow$ **Register** $\rightarrow$ **Server...**
2. In the **General** tab: Set Name to `CompraVenta`.
3. In the **Connection** tab:
   - **Host name/address:** `db` *(use `db`, not `localhost`, because pgAdmin connects over the Docker internal network)*
   - **Port:** `5432`
   - **Maintenance database:** `compraventa`
   - **Username:** `compraventa`
   - **Password:** `compraventa_pass_2026`
4. Click **Save**.

---

## 🛡️ Security Hardening

- **Rate Limiting:** Protects `/api/auth/login` against brute force attacks using a Token Bucket algorithm via Bucket4j.
- **HTTP Security Headers:** Strict CSP, `X-Frame-Options: DENY`, and HSTS enabled.
- **Input Validation:** Enforced through Jakarta Validation annotations on all incoming DTOs.
- **Secret Management:** Sensitive credentials externalized through environment variables.

---

## 📚 Complete Documentation
For exhaustive architectural breakdown, entity designs, and endpoint specifications, please refer to:
👉 [DOCUMENTACION_FINAL_BACKEND.md](file:///Users/marceldiazgranadosrobayo/IdeaProjects/BackCompraventa/DOCUMENTACION_FINAL_BACKEND.md)
