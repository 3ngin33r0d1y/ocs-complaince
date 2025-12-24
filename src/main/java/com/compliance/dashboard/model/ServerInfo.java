package com.compliance.dashboard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server information including compliance status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerInfo {
    
    private String name;
    
    @JsonProperty("image_name")
    private String imageName;
    
    @JsonProperty("image_id")
    private String imageId;
    
    @JsonProperty("image_year")
    private Integer imageYear;
    
    @JsonProperty("image_week")
    private Integer imageWeek;
    
    private String reason;
}
