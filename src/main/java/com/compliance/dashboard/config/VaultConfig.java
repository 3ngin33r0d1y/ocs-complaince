package com.compliance.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.support.VaultToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import jakarta.annotation.PostConstruct;

/**
 * Vault configuration for HashiCorp Vault integration.
 * Configures AppRole authentication for secure access to Vault secrets.
 */
@Configuration
public class VaultConfig extends AbstractVaultConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VaultConfig.class);

    @Value("${vault.uri}")
    private String vaultUri;

    @Value("${vault.namespace:}")
    private String vaultNamespace;

    @Value("${vault.app-role.role-id}")
    private String roleId;

    @Value("${vault.app-role.secret-id}")
    private String secretId;

    @PostConstruct
    public void logVaultConfig() {
        log.info("Vault config: uri={}, namespace={}, configPath={}", vaultUri, vaultNamespace, getConfigPath());
        log.info("Vault AppRole env set: roleIdSet={}, roleIdLen={}, secretIdSet={}, secretIdLen={}",
                hasText(roleId), safeLength(roleId), hasText(secretId), safeLength(secretId));
    }

    private String getConfigPath() {
        return System.getProperty("VAULT_CONFIG_PATH",
                System.getenv().getOrDefault("VAULT_CONFIG_PATH", "compliance/config"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        try {
            return VaultEndpoint.from(URI.create(vaultUri));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Vault URI: " + vaultUri, e);
        }
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId))
                .build();

        return new AppRoleAuthentication(options, restOperations());
    }
}
