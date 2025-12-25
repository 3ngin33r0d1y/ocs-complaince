package com.compliance.dashboard.service;

import com.compliance.dashboard.model.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Map;
import java.util.Set;

/**
 * Service for interacting with HashiCorp Vault.
 * Retrieves application configurations stored in Vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultService {

    private final ObjectMapper objectMapper;

    @Value("${vault.config-path:compliance/config}")
    private String configPath;

    @Value("${vault.uri}")
    private String vaultUri;

    @Value("${vault.namespace:}")
    private String vaultNamespace;

    @Value("${vault.app-role.role-id}")
    private String roleId;

    @Value("${vault.app-role.secret-id}")
    private String secretId;

    @Value("${vault.skip-verify:false}")
    private boolean skipVerify;

    private WebClient vaultClient;

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClient.create();
        if (skipVerify) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to configure insecure SSL for Vault", e);
            }
        }
        this.vaultClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        log.info("Vault config: uri={}, namespace={}, configPath={}", vaultUri, vaultNamespace, configPath);
        log.info("Vault AppRole env set: roleIdSet={}, roleIdLen={}, secretIdSet={}, secretIdLen={}",
                hasText(roleId), safeLength(roleId), hasText(secretId), safeLength(secretId));
    }

    /**
     * Retrieve all application configurations from Vault.
     *
     * @return Map of app name to AppConfig
     */
    public Map<String, AppConfig> getAllConfigs() {
        try {
            log.info("Retrieving configurations from Vault path: {}", configPath);
            String token = login();
            Map<String, Object> data = readSecret(token);
            
            // Convert each app config to AppConfig object
            Map<String, AppConfig> configs = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String appName = entry.getKey();
                AppConfig appConfig = objectMapper.convertValue(entry.getValue(), AppConfig.class);
                configs.put(appName, appConfig);
            }
            
            log.info("Successfully retrieved {} app configurations from Vault", configs.size());
            return configs;
            
        } catch (Exception e) {
            log.error("Failed to retrieve configurations from Vault", e);
            throw new RuntimeException("Failed to retrieve Vault configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve configuration for a specific application.
     *
     * @param appName The application name
     * @return AppConfig or null if not found
     */
    public AppConfig getAppConfig(String appName) {
        Map<String, AppConfig> allConfigs = getAllConfigs();
        return allConfigs.get(appName);
    }

    /**
     * Get list of available application names.
     *
     * @return Set of app names
     */
    public Set<String> getAvailableApps() {
        return getAllConfigs().keySet();
    }

    /**
     * Test Vault connectivity.
     *
     * @return true if Vault is accessible
     */
    public boolean testConnection() {
        try {
            String url = vaultUri + "/v1/sys/health";
            Integer status = vaultClient.get()
                    .uri(url)
                    .headers(headers -> setNamespaceHeader(headers))
                    .retrieve()
                    .toBodilessEntity()
                    .map(entity -> entity.getStatusCode().value())
                    .block();
            return status != null && status < 500;
        } catch (Exception e) {
            log.error("Vault health check failed", e);
            return false;
        }
    }

    private String login() {
        if (!hasText(roleId) || !hasText(secretId)) {
            throw new RuntimeException("Missing VAULT_ROLE_ID or VAULT_SECRET_ID");
        }

        String url = vaultUri + "/v1/auth/approle/login";
        log.info("Vault login URL: {}", url);
        Map<String, String> payload = Map.of(
                "role_id", roleId,
                "secret_id", secretId
        );

        Map<String, Object> response = vaultClient.post()
                .uri(url)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    setNamespaceHeader(headers);
                })
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("auth")) {
            throw new RuntimeException("No auth data returned from Vault login");
        }

        Map<String, Object> auth = (Map<String, Object>) response.get("auth");
        Object token = auth.get("client_token");
        if (token == null) {
            throw new RuntimeException("No client_token in Vault login response");
        }
        return token.toString();
    }

    private Map<String, Object> readSecret(String token) {
        String path = normalizeConfigPath(configPath);
        String url = vaultUri + "/v1/" + namespacePathPrefix() + path;
        log.info("Vault read URL: {}", url);

        Map<String, Object> response = vaultClient.get()
                .uri(url)
                .header("X-Vault-Token", token)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new RuntimeException("No response from Vault");
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            throw new RuntimeException("No data field in Vault response");
        }

        Map<String, Object> inner = (Map<String, Object>) data.get("data");
        if (inner == null) {
            throw new RuntimeException("No data.data field in Vault response");
        }

        return inner;
    }

    private String normalizeConfigPath(String path) {
        String normalized = path == null ? "" : path.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("secret/data/")) {
            return normalized;
        }
        return "secret/data/" + normalized;
    }

    private String namespacePathPrefix() {
        if (!hasText(vaultNamespace)) {
            return "";
        }
        String normalized = vaultNamespace.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            return normalized;
        }
        return normalized + "/";
    }

    private void setNamespaceHeader(HttpHeaders headers) {
        if (hasText(vaultNamespace)) {
            headers.set("X-Vault-Namespace", vaultNamespace);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }
}
