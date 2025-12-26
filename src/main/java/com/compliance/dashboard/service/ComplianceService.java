package com.compliance.dashboard.service;

import com.compliance.dashboard.client.IamAasClient;
import com.compliance.dashboard.client.OcsApiClient;
import com.compliance.dashboard.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for checking server compliance across regions.
 * Validates whether servers are running images built in the current ISO week.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final VaultService vaultService;
    private final IamAasClient iamAasClient;
    private final OcsApiClient ocsApiClient;

    private static final List<String> REGIONS = Arrays.asList("paris", "north");
    private static final Pattern WEEK_PATTERN = Pattern.compile("_(\\d{4})_w(\\d{2})", Pattern.CASE_INSENSITIVE);

    /**
     * Check compliance for all applications.
     *
     * @param debug Enable debug logging
     * @return Map of app name to compliance results
     */
    public Map<String, Object> checkAllAppsCompliance(boolean debug) {
        log.info("Checking compliance for all applications");
        Map<String, AppConfig> configs = vaultService.getAllConfigs();
        Map<String, ComplianceResult> results = new HashMap<>();
        
        for (Map.Entry<String, AppConfig> entry : configs.entrySet()) {
            String appName = entry.getKey();
            AppConfig appConfig = entry.getValue();
            
            try {
                ComplianceResult result = checkAppCompliance(appName, appConfig, debug);
                results.put(appName, result);
            } catch (Exception e) {
                log.error("Error checking compliance for app: {}", appName, e);
                results.put(appName, ComplianceResult.builder()
                        .appName(appName)
                        .error(e.getMessage())
                        .regions(new HashMap<>())
                        .build());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("apps", results);
        
        return response;
    }

    /**
     * Check compliance for a specific application.
     *
     * @param appName Application name
     * @param debug Enable debug logging
     * @return Compliance result
     */
    public ComplianceResult checkCompliance(String appName, boolean debug) {
        log.info("Checking compliance for app: {}", appName);
        AppConfig appConfig = vaultService.getAppConfig(appName);
        if (appConfig == null) {
            throw new RuntimeException("App configuration not found: " + appName);
        }
        
        return checkAppCompliance(appName, appConfig, debug);
    }

    /**
     * Check compliance for an application across all regions.
     */
    private ComplianceResult checkAppCompliance(String appName, AppConfig appConfig, boolean debug) {
        // Get current ISO week
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.get(IsoFields.WEEK_BASED_YEAR);
        int currentWeek = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        
        if (debug) {
            log.info("Current ISO week: {}-W{}", currentYear, currentWeek);
        }
        
        // Validate config
        validateAppConfig(appConfig);
        
        // Get OAuth token
        String scope = iamAasClient.buildScope(appConfig.getAccountId(), appConfig.getSgcpIamaasScopes());
        String accessToken = iamAasClient.getAccessToken(
                appConfig.getIamaasUrl(),
                appConfig.getClientId(),
                appConfig.getClientSecret(),
                scope
        );
        
        // Check compliance for each region
        Map<String, RegionResult> regionResults = new HashMap<>();
        
        for (String region : REGIONS) {
            try {
                RegionResult regionResult = checkRegionCompliance(
                        region, accessToken, currentYear, currentWeek, debug
                );
                regionResults.put(region, regionResult);
            } catch (Exception e) {
                log.error("Error checking compliance for region: {}", region, e);
                regionResults.put(region, RegionResult.builder()
                        .error(e.getMessage())
                        .totalServers(0)
                        .compliant(0)
                        .nonCompliant(0)
                        .compliancePercentage(0.0)
                        .goodServers(new ArrayList<>())
                        .badServers(new ArrayList<>())
                        .build());
            }
        }
        
        return ComplianceResult.builder()
                .appName(appName)
                .timestamp(now)
                .currentYear(currentYear)
                .currentWeek(currentWeek)
                .regions(regionResults)
                .build();
    }

    /**
     * Check compliance for a specific region.
     */
    private RegionResult checkRegionCompliance(String region, String accessToken, 
                                               int currentYear, int currentWeek, boolean debug) {
        log.info("Checking compliance for region: {}", region);
        
        // Fetch servers
        List<Map<String, Object>> servers = ocsApiClient.fetchServers(region, accessToken);
        
        // Image cache for this region
        Map<String, String> imageCache = new ConcurrentHashMap<>();
        
        // Collect server-image pairs
        List<ServerImagePair> serverImagePairs = new ArrayList<>();
        for (Map<String, Object> server : servers) {
            String serverName = (String) server.get("name");
            String imageId = extractImageId(server);
            serverImagePairs.add(new ServerImagePair(serverName, imageId));
        }
        
        // Fetch image names (with caching)
        for (ServerImagePair pair : serverImagePairs) {
            if (pair.imageId != null && !imageCache.containsKey(pair.imageId)) {
                String imageName = ocsApiClient.fetchImageName(region, pair.imageId, accessToken);
                imageCache.put(pair.imageId, imageName);
            }
        }
        
        // Classify servers
        List<ServerInfo> goodServers = new ArrayList<>();
        List<ServerInfo> badServers = new ArrayList<>();
        
        for (ServerImagePair pair : serverImagePairs) {
            String imageName = pair.imageId != null ? imageCache.get(pair.imageId) : null;
            ServerInfo serverInfo = classifyServer(
                    pair.serverName, pair.imageId, imageName, currentYear, currentWeek
            );
            
            if (serverInfo.getReason() == null) {
                goodServers.add(serverInfo);
            } else {
                badServers.add(serverInfo);
            }
        }
        
        int totalServers = serverImagePairs.size();
        double compliancePercentage = totalServers > 0 
                ? Math.round((double) goodServers.size() / totalServers * 10000.0) / 100.0 
                : 0.0;
        
        return RegionResult.builder()
                .totalServers(totalServers)
                .compliant(goodServers.size())
                .nonCompliant(badServers.size())
                .compliancePercentage(compliancePercentage)
                .goodServers(goodServers)
                .badServers(badServers)
                .build();
    }

    /**
     * Classify a server as compliant or non-compliant.
     */
    private ServerInfo classifyServer(String serverName, String imageId, String imageName,
                                     int currentYear, int currentWeek) {
        ServerInfo.ServerInfoBuilder builder = ServerInfo.builder()
                .name(serverName)
                .imageId(imageId != null ? imageId : "N/A")
                .imageName(imageName != null ? imageName : "N/A");
        
        // Extract year and week from image name
        YearWeek yearWeek = extractYearWeek(imageName);
        
        if (yearWeek == null) {
            builder.reason("No week info or unparsable");
            return builder.build();
        }
        
        builder.imageYear(yearWeek.year).imageWeek(yearWeek.week);
        
        // Check compliance
        if (yearWeek.year == currentYear && yearWeek.week == currentWeek) {
            // Compliant - current week
            return builder.build();
        } else if (yearWeek.year < currentYear || 
                  (yearWeek.year == currentYear && yearWeek.week < currentWeek)) {
            // Non-compliant - older than current week
            builder.reason("Older than current week");
            return builder.build();
        } else {
            // Non-compliant - future week/year
            builder.reason("Future week/year");
            return builder.build();
        }
    }

    /**
     * Extract image ID from server object.
     */
    private String extractImageId(Map<String, Object> server) {
        Object image = server.get("image");
        if (image instanceof Map) {
            return (String) ((Map<?, ?>) image).get("id");
        }
        return null;
    }

    /**
     * Extract year and week from image name using regex.
     */
    private YearWeek extractYearWeek(String imageName) {
        if (imageName == null) {
            return null;
        }
        
        Matcher matcher = WEEK_PATTERN.matcher(imageName);
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int week = Integer.parseInt(matcher.group(2));
                return new YearWeek(year, week);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse year/week from image name: {}", imageName);
                return null;
            }
        }
        
        return null;
    }

    /**
     * Validate app configuration has all required fields.
     */
    private void validateAppConfig(AppConfig config) {
        List<String> missing = new ArrayList<>();
        
        if (config.getAccountId() == null) missing.add("account_id");
        if (config.getClientId() == null) missing.add("client_id");
        if (config.getClientSecret() == null) missing.add("client_secret");
        if (config.getIamaasUrl() == null) missing.add("iamaas_url");
        if (config.getSgcpIamaasScopes() == null) missing.add("sgcp_iamaas_scopes");
        
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required config keys: " + String.join(", ", missing));
        }
    }

    /**
     * Helper class for server-image pairs.
     */
    private static class ServerImagePair {
        final String serverName;
        final String imageId;
        
        ServerImagePair(String serverName, String imageId) {
            this.serverName = serverName;
            this.imageId = imageId;
        }
    }

    /**
     * Helper class for year-week pairs.
     */
    private static class YearWeek {
        final int year;
        final int week;
        
        YearWeek(int year, int week) {
            this.year = year;
            this.week = week;
        }
    }
}
