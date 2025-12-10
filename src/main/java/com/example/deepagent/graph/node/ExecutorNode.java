package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;
import com.example.deepagent.model.ExecutionStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
public class ExecutorNode implements Node {

    private final ChatClient chatClient;

    public ExecutorNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AgentState execute(AgentState state) {
        log.info("ExecutorNode: Executing step: {}", state.getCurrentStep());

        if (state.getExecutionHistory() == null) {
            state.setExecutionHistory(new ArrayList<>());
        }

        int stepNumber = state.getExecutionHistory().size() + 1;

        try {
            // Create prompt for execution
            String prompt = createExecutionPrompt(state);

            // Call LLM to execute the step
            String result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            ExecutionStep step = new ExecutionStep(
                    stepNumber,
                    state.getCurrentStep(),
                    result,
                    LocalDateTime.now()
            );

            state.getExecutionHistory().add(step);

            // Determine next action: move to next step or evaluate
            if (stepNumber < state.getPlan().size()) {
                state.setCurrentStep(state.getPlan().get(stepNumber));
                state.setNextAction("execute");
                log.info("ExecutorNode: Moving to next step ({}/{})", stepNumber + 1, state.getPlan().size());
            } else {
                state.setNextAction("evaluate");
                log.info("ExecutorNode: All steps executed, moving to evaluation");
            }

            return state;

        } catch (Exception e) {
            log.error("ExecutorNode: Error calling LLM for execution", e);
            // Create fallback result
            String fallbackResult = String.format("Error executing step: %s. Using fallback.", state.getCurrentStep());
            ExecutionStep step = new ExecutionStep(
                    stepNumber,
                    state.getCurrentStep(),
                    fallbackResult,
                    LocalDateTime.now()
            );
            state.getExecutionHistory().add(step);

            // Continue to next step or evaluate
            if (stepNumber < state.getPlan().size()) {
                state.setCurrentStep(state.getPlan().get(stepNumber));
                state.setNextAction("execute");
            } else {
                state.setNextAction("evaluate");
            }

            return state;
        }
    }

    private String createExecutionPrompt(AgentState state) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an execution assistant working on the following query:\n\n");
        prompt.append("Original Query: ").append(state.getUserQuery()).append("\n\n");
        prompt.append("Current Step to Execute: ").append(state.getCurrentStep()).append("\n\n");

        if (!state.getExecutionHistory().isEmpty()) {
            prompt.append("Previous steps completed:\n");
            for (ExecutionStep step : state.getExecutionHistory()) {
                prompt.append(String.format("%d. %s\n   Result: %s\n",
                        step.stepNumber(), step.stepDescription(), step.result()));
            }
            prompt.append("\n");
        }

        prompt.append("Execute the current step and provide a detailed result. ");
        prompt.append("Be thorough and specific in your execution.");

        return prompt.toString();
    }
}
