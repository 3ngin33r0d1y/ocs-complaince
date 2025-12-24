package com.compliance.dashboard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Compliance results for a specific region.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionResult {
    
    @JsonProperty("total_servers")
    private int totalServers;
    
    private int compliant;
    
    @JsonProperty("non_compliant")
    private int nonCompliant;
    
    @JsonProperty("compliance_percentage")
    private double compliancePercentage;
    
    @JsonProperty("good_servers")
    private List<ServerInfo> goodServers;
    
    @JsonProperty("bad_servers")
    private List<ServerInfo> badServers;
    
    private String error;
}
