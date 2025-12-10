package com.example.deepagent.graph.node;

import com.example.deepagent.config.AgentConfig;
import com.example.deepagent.model.AgentState;
import com.example.deepagent.model.ExecutionStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EvaluatorNode implements Node {

    private final ChatClient chatClient;
    private final AgentConfig agentConfig;

    public EvaluatorNode(ChatClient.Builder chatClientBuilder, AgentConfig agentConfig) {
        this.chatClient = chatClientBuilder.build();
        this.agentConfig = agentConfig;
    }

    @Override
    public AgentState execute(AgentState state) {
        log.info("EvaluatorNode: Evaluating execution quality");

        try {
            // Generate synthesis using LLM
            String synthesis = generateSynthesisWithLLM(state);
            state.setSynthesis(synthesis);

            // Evaluate quality using LLM
            double qualityScore = evaluateQualityWithLLM(state, synthesis);
            state.setQualityScore(qualityScore);

            // Determine next action based on quality and iterations
            String nextAction = determineNextAction(state, qualityScore);
            state.setNextAction(nextAction);

            log.info("EvaluatorNode: Quality score: {}, Next action: {}", qualityScore, nextAction);
            return state;

        } catch (Exception e) {
            log.error("EvaluatorNode: Error calling LLM for evaluation, using fallback", e);
            // Fallback: generate basic synthesis and finish
            String synthesis = generateFallbackSynthesis(state);
            state.setSynthesis(synthesis);
            state.setQualityScore(0.75);
            state.setNextAction("finish");
            return state;
        }
    }

    private String generateSynthesisWithLLM(AgentState state) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a synthesis assistant. Create a comprehensive answer to the following query based on the execution results.\n\n");
        prompt.append("Original Query: ").append(state.getUserQuery()).append("\n\n");
        prompt.append("Execution Steps and Results:\n");

        for (ExecutionStep step : state.getExecutionHistory()) {
            prompt.append(String.format("%d. %s\n", step.stepNumber(), step.stepDescription()));
            prompt.append(String.format("   Result: %s\n\n", step.result()));
        }

        prompt.append("Synthesize the above results into a clear, comprehensive answer to the original query. ");
        prompt.append("Be thorough and well-structured.");

        return chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();
    }

    private double evaluateQualityWithLLM(AgentState state, String synthesis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a quality evaluator. Evaluate the quality and completeness of the following answer.\n\n");
        prompt.append("Original Query: ").append(state.getUserQuery()).append("\n\n");
        prompt.append("Answer: ").append(synthesis).append("\n\n");
        prompt.append("Evaluate on a scale from 0.0 to 1.0 where:\n");
        prompt.append("- 1.0 = Perfect, complete, accurate answer\n");
        prompt.append("- 0.7-0.9 = Good answer with minor gaps\n");
        prompt.append("- 0.5-0.7 = Acceptable but incomplete\n");
        prompt.append("- Below 0.5 = Poor or significantly incomplete\n\n");
        prompt.append("Return ONLY a number between 0.0 and 1.0, nothing else.");

        try {
            String response = chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content()
                    .trim();

            // Extract number from response
            String numberStr = response.replaceAll("[^0-9.]", "");
            double score = Double.parseDouble(numberStr);

            // Clamp to valid range
            return Math.max(0.0, Math.min(1.0, score));

        } catch (Exception e) {
            log.warn("EvaluatorNode: Could not parse quality score, using default 0.75", e);
            return 0.75;
        }
    }

    private String determineNextAction(AgentState state, double qualityScore) {
        // Always finish if we've reached max iterations
        if (state.getIterationCount() >= agentConfig.getMaxIterations()) {
            log.info("EvaluatorNode: Max iterations reached, finishing");
            return "finish";
        }

        // Finish if quality is good enough
        if (qualityScore >= agentConfig.getQualityThreshold()) {
            log.info("EvaluatorNode: Quality threshold met, finishing");
            return "finish";
        }

        // Otherwise, replan for another iteration
        log.info("EvaluatorNode: Quality below threshold, replanning");
        return "plan";
    }

    private String generateFallbackSynthesis(AgentState state) {
        StringBuilder synthesis = new StringBuilder();
        synthesis.append("Based on the query: '").append(state.getUserQuery()).append("'\n\n");
        synthesis.append("Execution Summary:\n");

        for (ExecutionStep step : state.getExecutionHistory()) {
            synthesis.append(String.format("%d. %s\n", step.stepNumber(), step.stepDescription()));
            synthesis.append(String.format("   Result: %s\n", step.result()));
        }

        synthesis.append("\nThe agent completed the planned steps.");

        return synthesis.toString();
    }
}
