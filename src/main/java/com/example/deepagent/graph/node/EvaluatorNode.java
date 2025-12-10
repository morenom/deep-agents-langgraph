package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;
import com.example.deepagent.model.ExecutionStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class EvaluatorNode implements Node {

    @Override
    public AgentState execute(AgentState state) {
        log.info("EvaluatorNode: Evaluating execution quality");

        // Generate synthesis from execution history
        String synthesis = generateSynthesis(state);
        state.setSynthesis(synthesis);

        // MOCKED: Return a hardcoded quality score
        double qualityScore = 0.85;
        state.setQualityScore(qualityScore);

        // For PR1, always finish (no iteration yet)
        state.setNextAction("finish");

        log.info("EvaluatorNode: Quality score: {}, Next action: {}", qualityScore, state.getNextAction());
        return state;
    }

    private String generateSynthesis(AgentState state) {
        StringBuilder synthesis = new StringBuilder();
        synthesis.append("Based on the query: '").append(state.getUserQuery()).append("'\n\n");
        synthesis.append("Execution Summary:\n");

        for (ExecutionStep step : state.getExecutionHistory()) {
            synthesis.append(String.format("%d. %s\n", step.stepNumber(), step.stepDescription()));
            synthesis.append(String.format("   Result: %s\n", step.result()));
        }

        synthesis.append("\nConclusion: The agent has successfully completed all planned steps. ");
        synthesis.append("This demonstrates the Plan → Execute → Evaluate → Finish workflow with mocked behavior.");

        return synthesis.toString();
    }
}
