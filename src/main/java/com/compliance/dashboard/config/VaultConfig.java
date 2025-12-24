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

import java.net.URI;

/**
 * Vault configuration for HashiCorp Vault integration.
 * Configures AppRole authentication for secure access to Vault secrets.
 */
@Configuration
public class VaultConfig extends AbstractVaultConfiguration {

    @Value("${vault.uri}")
    private String vaultUri;

    @Value("${vault.namespace:}")
    private String vaultNamespace;

    @Value("${vault.app-role.role-id}")
    private String roleId;

    @Value("${vault.app-role.secret-id}")
    private String secretId;

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
