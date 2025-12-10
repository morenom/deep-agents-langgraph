package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlannerNode implements Node {

    private final ChatClient chatClient;

    public PlannerNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AgentState execute(AgentState state) {
        log.info("PlannerNode: Creating plan for query: {}", state.getUserQuery());

        try {
            // Create prompt for planning
            String prompt = createPlanningPrompt(state);

            // Call LLM to generate plan
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Parse the response into a list of steps
            List<String> plan = parsePlanFromResponse(response);

            if (plan.isEmpty()) {
                log.warn("PlannerNode: LLM returned empty plan, using fallback");
                plan = createFallbackPlan(state.getUserQuery());
            }

            state.setPlan(plan);
            state.setCurrentStep(plan.get(0));
            state.setNextAction("execute");
            state.setIterationCount(state.getIterationCount() + 1);

            log.info("PlannerNode: Created plan with {} steps", plan.size());
            return state;

        } catch (Exception e) {
            log.error("PlannerNode: Error calling LLM, using fallback plan", e);
            List<String> fallbackPlan = createFallbackPlan(state.getUserQuery());
            state.setPlan(fallbackPlan);
            state.setCurrentStep(fallbackPlan.get(0));
            state.setNextAction("execute");
            state.setIterationCount(state.getIterationCount() + 1);
            return state;
        }
    }

    private String createPlanningPrompt(AgentState state) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a planning assistant. Break down the following task into 3-5 specific, actionable steps.\n\n");
        prompt.append("Task: ").append(state.getUserQuery()).append("\n\n");

        if (state.getIterationCount() > 0 && state.getSynthesis() != null) {
            prompt.append("Previous attempt summary:\n");
            prompt.append(state.getSynthesis()).append("\n\n");
            prompt.append("Please create an improved plan based on the previous attempt.\n\n");
        }

        prompt.append("Return ONLY the numbered steps, one per line, starting with '1.', '2.', etc.\n");
        prompt.append("Do not include any explanation or preamble. Just the steps.");

        return prompt.toString();
    }

    private List<String> parsePlanFromResponse(String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> line.matches("^\\d+\\..*"))  // Lines starting with number and dot
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", ""))  // Remove the number prefix
                .collect(Collectors.toList());
    }

    private List<String> createFallbackPlan(String query) {
        return List.of(
                "Research and gather information about: " + query,
                "Analyze the gathered information and identify key points",
                "Synthesize findings into a comprehensive answer"
        );
    }
}
