package com.compliance.dashboard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Complete compliance result for an application across all regions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceResult {
    
    @JsonProperty("app_name")
    private String appName;
    
    private LocalDateTime timestamp;
    
    @JsonProperty("current_week")
    private int currentWeek;
    
    @JsonProperty("current_year")
    private int currentYear;
    
    private Map<String, RegionResult> regions;
    
    private String error;
}
