# 🏦 CompraVenta Backend API

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

A professional, high-performance REST API designed for Pawn Shop and Purchase management systems. This backend implements a hybrid architecture with **offline-first synchronization**, robust security, and a data-driven dashboard for business intelligence.

## ✨ Key Features

### 🛠️ Core Modules
- **🔐 Authentication & Authorization:** Secure JWT-based stateless auth with Role-Based Access Control (RBIN: `ADMIN`, `EMPLEADO`).
- **📦 Article Management:** Full CRUD for inventory with stock tracking and category management.
- **💍 Pawn System:** Complete lifecycle management for pawned items (Active $\rightarrow$ Redeemed $\rightarrow$ Expired/Sold).
- **💰 Sales & Purchases:** Integrated flow for buying from clients and selling to the public.
- **👥 Client & Employee Management:** Detailed registry and role assignment.
- **📈 Business Dashboard:** Real-time KPIs including total revenue, pawn distribution, and low-stock alerts.

### 🌐 Advanced Infrastructure
- **🔄 Offline-First Sync:** Implementation of the **Outbox Pattern** for seamless data synchronization between local PostgreSQL and Supabase Cloud.
- **⚡ Performance:** Distributed caching layer using **Redis** for session management and fast data retrieval.
- **📝 Audit System:** AOP-based automated auditing that logs every critical operation in the system.

## 🛡️ Security Hardening
The system is built with a "Security-First" mindset:
- **Rate Limiting:** Protection against brute-force attacks on `/api/auth/login` using **Bucket4j**.
- **HTTP Security Headers:** Enforced CSP, HSTS, and X-Frame-Options to prevent XSS and Clickjacking.
- **Secret Externalization:** Zero hardcoded keys. All sensitive data is loaded via environment variables.
- **Input Validation:** Strict request validation using Jakarta Bean Validation.

## 🚀 Getting Started

### Prerequisites
- **Java 21** (JDK 21+)
- **Maven 3.9+**
- **PostgreSQL 16+**
- **Redis 7.2+**

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd Backend
   ```

2. **Environment Configuration:**
   Copy the example environment file and fill in your credentials:
   ```bash
   cp .env.example .env
   ```
   *Refer to `.env.example` for the required variables (DB, Redis, Supabase, JWT).*

3. **Run the Application:**
   ```bash
   ./mvnw spring-boot:run
   ```

### 📖 API Documentation
Once the server is running, you can access the interactive API documentation via Swagger:
`http://localhost:8080/swagger-ui.html`

---
Developed as a professional portfolio project demonstrating advanced Spring Boot patterns, security hardening, and offline-sync architecture.
