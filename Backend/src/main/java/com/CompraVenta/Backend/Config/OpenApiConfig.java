package com.CompraVenta.Backend.Config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private  static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()));

    }
    private Info buildInfo() {
        return new Info()
                .title("CompraVenta Backend API")
                .description("""
                        REST API para el Sistema de CompraVenta / Empeño / Inventario.
                        
                        **Módulos disponibles:**
                        - 🔐 Auth — Autenticación JWT (online/offline)
                        - 📦 Articles — Gestión de inventario
                        - 🤝 Pawns — Empeños y pagos
                        - 💰 Sales — Ventas
                        - 🛒 Purchases — Compras
                        - 👤 Clients — Clientes
                        - 👔 Employees — Empleados
                        - 🔄 Sync — Estado de sincronización
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Equipo CompraVenta")
                        .email("dev@compraventa.com")
                );
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8080/api").description("Local"),
                new Server().url("https://api.compraventa.com/api").description("Producción")
        );
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token JWT obtenido desde POST /auth/login");
    }
}

