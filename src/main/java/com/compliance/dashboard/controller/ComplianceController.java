package com.compliance.dashboard.controller;

import com.compliance.dashboard.model.ComplianceResult;
import com.compliance.dashboard.service.ComplianceService;
import com.compliance.dashboard.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST Controller for Compliance Dashboard API.
 * Provides endpoints for health checks, app listing, and compliance data.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComplianceController {

    private final VaultService vaultService;
    private final ComplianceService complianceService;

    /**
     * Health check endpoint.
     * Tests API and Vault connectivity.
     *
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean vaultConnected = vaultService.testConnection();
            
            response.put("status", vaultConnected ? "healthy" : "unhealthy");
            response.put("vault_connected", vaultConnected);
            response.put("message", vaultConnected 
                    ? "API is running and Vault is accessible" 
                    : "API is running but Vault is not accessible");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            response.put("status", "unhealthy");
            response.put("vault_connected", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get list of available applications from Vault.
     *
     * GET /api/apps
     */
    @GetMapping("/apps")
    public ResponseEntity<Map<String, Object>> getApps() {
        log.info("Apps list requested");
        
        try {
            Set<String> apps = vaultService.getAvailableApps();
            
            Map<String, Object> response = new HashMap<>();
            response.put("apps", new ArrayList<>(apps));
            response.put("count", apps.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching apps", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to fetch apps from Vault");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get compliance data for all apps or a specific app.
     *
     * GET /api/compliance?app=<app_name>&debug=<true|false>
     *
     * @param app Optional app name to check specific app
     * @param debug Optional debug flag
     */
    @GetMapping("/compliance")
    public ResponseEntity<Object> getCompliance(
            @RequestParam(required = false) String app,
            @RequestParam(required = false, defaultValue = "false") boolean debug) {
        
        log.info("Compliance check requested - app: {}, debug: {}", app, debug);
        
        try {
            if (app != null && !app.isEmpty()) {
                // Check specific app
                ComplianceResult result = complianceService.checkCompliance(app, debug);
                return ResponseEntity.ok(result);
            } else {
                // Check all apps
                Map<String, Object> results = complianceService.checkAllAppsCompliance(debug);
                return ResponseEntity.ok(results);
            }
            
        } catch (Exception e) {
            log.error("Error in compliance check", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to check compliance");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get aggregated compliance summary across all apps and regions.
     *
     * GET /api/compliance/summary?debug=<true|false>
     *
     * @param debug Optional debug flag
     */
    @GetMapping("/compliance/summary")
    public ResponseEntity<Map<String, Object>> getComplianceSummary(
            @RequestParam(required = false, defaultValue = "false") boolean debug) {
        
        log.info("Compliance summary requested - debug: {}", debug);
        
        try {
            Map<String, Object> allResults = complianceService.checkAllAppsCompliance(debug);
            Map<String, ComplianceResult> apps = (Map<String, ComplianceResult>) allResults.get("apps");
            
            // Aggregate statistics
            int totalServers = 0;
            int totalCompliant = 0;
            int totalNonCompliant = 0;
            List<Map<String, Object>> appsSummary = new ArrayList<>();
            
            for (Map.Entry<String, ComplianceResult> entry : apps.entrySet()) {
                String appName = entry.getKey();
                ComplianceResult appData = entry.getValue();
                
                int appTotal = 0;
                int appCompliant = 0;
                int appNonCompliant = 0;
                
                if (appData.getRegions() != null) {
                    for (Map.Entry<String, com.compliance.dashboard.model.RegionResult> regionEntry : 
                            appData.getRegions().entrySet()) {
                        com.compliance.dashboard.model.RegionResult regionData = regionEntry.getValue();
                        
                        if (regionData.getError() == null) {
                            appTotal += regionData.getTotalServers();
                            appCompliant += regionData.getCompliant();
                            appNonCompliant += regionData.getNonCompliant();
                        }
                    }
                }
                
                totalServers += appTotal;
                totalCompliant += appCompliant;
                totalNonCompliant += appNonCompliant;
                
                Map<String, Object> appSummary = new HashMap<>();
                appSummary.put("app_name", appName);
                appSummary.put("total_servers", appTotal);
                appSummary.put("compliant", appCompliant);
                appSummary.put("non_compliant", appNonCompliant);
                appSummary.put("compliance_percentage", 
                        appTotal > 0 ? Math.round((double) appCompliant / appTotal * 10000.0) / 100.0 : 0.0);
                
                appsSummary.add(appSummary);
            }
            
            // Build summary response
            Map<String, Object> overall = new HashMap<>();
            overall.put("total_servers", totalServers);
            overall.put("compliant", totalCompliant);
            overall.put("non_compliant", totalNonCompliant);
            overall.put("compliance_percentage", 
                    totalServers > 0 ? Math.round((double) totalCompliant / totalServers * 10000.0) / 100.0 : 0.0);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("timestamp", allResults.get("timestamp"));
            summary.put("overall", overall);
            summary.put("by_app", appsSummary);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error generating summary", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to generate compliance summary");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Global exception handler for 404 errors.
     */
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Endpoint not found");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Global exception handler for 500 errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInternalError(Exception e) {
        log.error("Internal server error", e);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
