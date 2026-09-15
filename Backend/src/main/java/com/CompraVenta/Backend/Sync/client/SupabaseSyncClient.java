package com.CompraVenta.Backend.Sync.client;

import com.CompraVenta.Backend.Sync.SyncOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Slf4j
@Component
public class SupabaseSyncClient {

    private final String baseUrl;
    private final String serviceRoleKey;
    private final RestClient restClient;

    public SupabaseSyncClient(
            @Value("${sync.supabase.url:}") String baseUrl,
            @Value("${sync.supabase.service-role-key:}") String serviceRoleKey,
            @Value("${sync.supabase.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.restClient = RestClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader("apikey", this.serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + this.serviceRoleKey)
                .defaultHeader("Prefer", "return=minimal")
                .requestFactory(requestFactory(timeoutSeconds))
                .build();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(baseUrl)
                && StringUtils.hasText(serviceRoleKey)
                && !serviceRoleKey.contains("your-service-role-key")
                && !serviceRoleKey.equalsIgnoreCase("change-me");
    }

    public void push(SyncOutbox item) {
        String path = "/rest/v1/" + item.getEntityType();
        String filter = "?global_id=eq." + item.getEntityId();
        String operation = item.getOperation() == null ? "" : item.getOperation().toUpperCase();

        try {
            switch (operation) {
                case "INSERT" -> restClient.post()
                        .uri(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(item.getPayload())
                        .retrieve()
                        .toBodilessEntity();
                case "UPDATE" -> restClient.patch()
                        .uri(path + filter)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(item.getPayload())
                        .retrieve()
                        .toBodilessEntity();
                case "DELETE" -> restClient.delete()
                        .uri(path + filter)
                        .retrieve()
                        .toBodilessEntity();
                default -> throw new IllegalArgumentException("Operación de sync no soportada: " + item.getOperation());
            }
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatusCode.valueOf(404) && "DELETE".equals(operation)) {
                return;
            }
            if (ex.getStatusCode() == HttpStatusCode.valueOf(409)) {
                throw new SyncConflictException(ex.getMessage());
            }
            throw new IllegalStateException("Error HTTP " + ex.getStatusCode().value() + " al sincronizar "
                    + item.getEntityType() + "/" + item.getEntityId() + ": " + ex.getResponseBodyAsString(), ex);
        }
    }

    private static String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url.trim();
    }

    private static org.springframework.http.client.ClientHttpRequestFactory requestFactory(long timeoutSeconds) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        int timeoutMs = Math.toIntExact(Duration.ofSeconds(timeoutSeconds).toMillis());
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return factory;
    }
}
