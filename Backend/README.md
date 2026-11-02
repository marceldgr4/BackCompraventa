# CompraVenta Backend API

This is the backend implementation for the CompraVenta (Pawn Shop) management system.

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL
- Redis

### Installation & Configuration

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd Backend
   ```

2. **Environment Configuration:**
   The application requires an `.env` file for sensitive configurations.
   ```bash
   cp .env.example .env
   ```
   Open `.env` and update the placeholders with your actual credentials (DB, Redis, Supabase, and JWT Secret).

3. **Run the Application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 🛡️ Security Features
- **JWT Authentication:** Stateless authentication using JSON Web Tokens.
- **Rate Limiting:** Protected login endpoint using Bucket4j to prevent brute-force attacks.
- **Security Headers:** Configured CSP, HSTS, and X-Frame-Options to mitigate common web vulnerabilities.
- **Secret Externalization:** All sensitive keys are loaded from environment variables.

## 🛠️ Tech Stack
- **Framework:** Spring Boot 3.4.5
- **Database:** PostgreSQL (Migration via Flyway)
- **Cache/Security:** Redis
- **Documentation:** OpenAPI / Swagger (`/swagger-ui.html`)
