package com.compliance.dashboard.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with OCS APIs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcsApiClient {

    private final WebClient webClient;
    
    private static final int MAX_RETRIES = 5;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(120);
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    /**
     * Fetch server details from OCS API for a specific region.
     *
     * @param region The region (e.g., "paris", "north")
     * @param accessToken OAuth access token
     * @return List of server objects
     */
    public List<Map<String, Object>> fetchServers(String region, String accessToken) {
        String url = buildServersUrl(region);
        log.info("Fetching servers from: {}", url);
        
        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY)
                            .doBeforeRetry(signal -> 
                                log.warn("Retrying servers fetch for region {}, attempt: {}", 
                                    region, signal.totalRetries() + 1)))
                    .block();
            
            if (response != null && response.containsKey("servers")) {
                List<Map<String, Object>> servers = (List<Map<String, Object>>) response.get("servers");
                log.info("Fetched {} servers from region {}", servers.size(), region);
                return servers;
            }
            
            throw new RuntimeException("No servers in response");
            
        } catch (Exception e) {
            log.error("Failed to fetch servers from region: {}", region, e);
            throw new RuntimeException("Failed to fetch servers: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch image details by image ID.
     *
     * @param region The region
     * @param imageId The image ID
     * @param accessToken OAuth access token
     * @return Image name or null if not found
     */
    public String fetchImageName(String region, String imageId, String accessToken) {
        String url = buildImageUrl(region, imageId);
        
        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY)
                            .doBeforeRetry(signal -> 
                                log.debug("Retrying image fetch for {}, attempt: {}", 
                                    imageId, signal.totalRetries() + 1)))
                    .block();
            
            if (response != null && response.containsKey("image")) {
                Map<String, Object> image = (Map<String, Object>) response.get("image");
                return (String) image.get("name");
            }
            
            return null;
            
        } catch (Exception e) {
            log.warn("Failed to fetch image {} from region {}: {}", imageId, region, e.getMessage());
            return null;
        }
    }

    /**
     * Build OCS servers detail URL for a region.
     */
    private String buildServersUrl(String region) {
        return String.format("https://ocs.eu-fr-%s.cloud/v0/servers/detail", region);
    }

    /**
     * Build OCS image URL for a region and image ID.
     */
    private String buildImageUrl(String region, String imageId) {
        return String.format("https://ocs.eu-fr-%s.cloud/v0/images/%s", region, imageId);
    }
}
