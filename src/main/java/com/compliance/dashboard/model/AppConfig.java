package com.compliance.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application configuration retrieved from Vault.
 * Contains OAuth credentials and API endpoints for each application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {
    
    @JsonProperty("account_id")
    private String accountId;
    
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("client_secret")
    private String clientSecret;
    
    @JsonProperty("iamaas_url")
    private String iamaasUrl;
    
    @JsonProperty("sgcp_iamaas_scopes")
    private String sgcpIamaasScopes;
}
