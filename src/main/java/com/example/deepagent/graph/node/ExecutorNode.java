package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;
import com.example.deepagent.model.ExecutionStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
public class ExecutorNode implements Node {

    @Override
    public AgentState execute(AgentState state) {
        log.info("ExecutorNode: Executing step: {}", state.getCurrentStep());

        if (state.getExecutionHistory() == null) {
            state.setExecutionHistory(new ArrayList<>());
        }

        int stepNumber = state.getExecutionHistory().size() + 1;

        // MOCKED: Simulate execution with a mocked result
        String result = String.format("Completed: %s. [This is a mocked execution result for demonstration purposes]",
                state.getCurrentStep());

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
    }
}
