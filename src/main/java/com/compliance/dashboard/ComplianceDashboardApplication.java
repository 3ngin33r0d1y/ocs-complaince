package com.compliance.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot Application for OCS Compliance Dashboard.
 * 
 * This application provides a REST API for monitoring server compliance
 * across multiple cloud regions, with integrated React frontend.
 */
@SpringBootApplication
@EnableAsync
public class ComplianceDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceDashboardApplication.class, args);
    }
}
