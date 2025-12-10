package com.example.deepagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {
    private int maxIterations = 10;
    private double qualityThreshold = 0.75;
}
