package com.CompraVenta.Backend.Config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Slf4j
@Component
public class JwtStartupValidator {

    private static final int MIN_SECRET_LENGTH = 32;

    private final String secret;
    private final Environment environment;

    public JwtStartupValidator(
            @Value("${jwt.secret:}") String secret,
            Environment environment
    ) {
        this.secret = secret;
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("production"));

        if (!StringUtils.hasText(secret) || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + MIN_SECRET_LENGTH + " caracteres. Defínelo en el entorno."
            );
        }

        if (secret.contains("CHANGE_ME")) {
            if (production) {
                throw new IllegalStateException(
                        "JWT_SECRET de producción no puede usar el valor placeholder CHANGE_ME."
                );
            }
            log.warn("JWT_SECRET usa un placeholder. Cámbialo antes de exponer el API fuera de local.");
        }
    }
}
