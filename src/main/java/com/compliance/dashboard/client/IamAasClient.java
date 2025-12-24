package com.compliance.dashboard.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Client for obtaining OAuth tokens from IAMaaS.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamAasClient {

    private final WebClient webClient;
    
    private static final int MAX_RETRIES = 5;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(120);
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    /**
     * Get OAuth access token from IAMaaS.
     *
     * @param iamaasUrl The IAMaaS token endpoint URL
     * @param clientId OAuth client ID
     * @param clientSecret OAuth client secret
     * @param scope The requested scope
     * @return Access token
     */
    public String getAccessToken(String iamaasUrl, String clientId, String clientSecret, String scope) {
        log.info("Requesting access token from IAMaaS: {}", iamaasUrl);
        
        String basicAuth = createBasicAuthHeader(clientId, clientSecret);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("scope", scope);
        
        try {
            Map<String, Object> response = webClient.post()
                    .uri(iamaasUrl)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY)
                            .doBeforeRetry(signal -> 
                                log.warn("Retrying IAMaaS token request, attempt: {}", signal.totalRetries() + 1)))
                    .block();
            
            if (response != null && response.containsKey("access_token")) {
                log.info("Successfully obtained access token");
                return (String) response.get("access_token");
            }
            
            throw new RuntimeException("No access_token in IAMaaS response");
            
        } catch (Exception e) {
            log.error("Failed to obtain access token from IAMaaS", e);
            throw new RuntimeException("Failed to obtain access token: " + e.getMessage(), e);
        }
    }

    /**
     * Build scope string from account ID and scopes.
     *
     * @param accountId The account ID
     * @param scopesStr Space-separated scopes
     * @return Formatted scope string
     */
    public String buildScope(String accountId, String scopesStr) {
        String[] scopes = scopesStr.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(accountId).append(":sgcp:").append(scopes[i]);
        }
        
        return sb.toString();
    }

    /**
     * Create Basic Authentication header.
     */
    private String createBasicAuthHeader(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
