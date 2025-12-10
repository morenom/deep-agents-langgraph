package com.example.deepagent.dto;

import com.example.deepagent.model.ExecutionStep;

import java.util.List;

public record AgentResponse(
        String threadId,
        String finalAnswer,
        List<ExecutionStep> executionTrace,
        int iterations,
        double qualityScore,
        List<String> planSteps
) {
}
