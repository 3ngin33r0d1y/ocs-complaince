package com.compliance.dashboard.service;

import com.compliance.dashboard.model.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

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

    private final VaultTemplate vaultTemplate;
    private final ObjectMapper objectMapper;

    @Value("${vault.config-path:compliance/config}")
    private String configPath;

    /**
     * Retrieve all application configurations from Vault.
     *
     * @return Map of app name to AppConfig
     */
    public Map<String, AppConfig> getAllConfigs() {
        try {
            log.info("Retrieving configurations from Vault path: {}", configPath);
            
            VaultResponseSupport<Map> response = vaultTemplate.read(configPath, Map.class);
            
            if (response == null || response.getData() == null) {
                throw new RuntimeException("No data found at Vault path: " + configPath);
            }
            
            Map<String, Object> data = response.getData();
            
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
            vaultTemplate.opsForSys().health();
            return true;
        } catch (Exception e) {
            log.error("Vault health check failed", e);
            return false;
        }
    }
}
