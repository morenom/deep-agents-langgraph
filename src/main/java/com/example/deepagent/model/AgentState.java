package com.example.deepagent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {
    private String threadId;
    private String userQuery;
    private List<String> plan;
    private String currentStep;
    private List<ExecutionStep> executionHistory;
    private String synthesis;
    private double qualityScore;
    private int iterationCount;
    private String nextAction; // "plan", "execute", "evaluate", "finish"

    public AgentState copy() {
        AgentState copy = new AgentState();
        copy.threadId = this.threadId;
        copy.userQuery = this.userQuery;
        copy.plan = this.plan != null ? new ArrayList<>(this.plan) : null;
        copy.currentStep = this.currentStep;
        copy.executionHistory = this.executionHistory != null ? new ArrayList<>(this.executionHistory) : null;
        copy.synthesis = this.synthesis;
        copy.qualityScore = this.qualityScore;
        copy.iterationCount = this.iterationCount;
        copy.nextAction = this.nextAction;
        return copy;
    }

    public static AgentState createInitial(String userQuery) {
        AgentState state = new AgentState();
        state.threadId = UUID.randomUUID().toString();
        state.userQuery = userQuery;
        state.plan = new ArrayList<>();
        state.executionHistory = new ArrayList<>();
        state.iterationCount = 0;
        state.qualityScore = 0.0;
        state.nextAction = "plan";
        return state;
    }
}
