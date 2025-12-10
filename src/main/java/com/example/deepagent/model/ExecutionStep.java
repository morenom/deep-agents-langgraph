package com.example.deepagent.model;

import java.time.LocalDateTime;

public record ExecutionStep(
        int stepNumber,
        String stepDescription,
        String result,
        LocalDateTime timestamp
) {
}
