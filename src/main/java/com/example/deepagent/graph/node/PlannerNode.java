package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PlannerNode implements Node {

    @Override
    public AgentState execute(AgentState state) {
        log.info("PlannerNode: Creating plan for query: {}", state.getUserQuery());

        // MOCKED: Generate a hardcoded 3-step plan
        List<String> plan = List.of(
                "Research and gather information about: " + state.getUserQuery(),
                "Analyze the gathered information and identify key points",
                "Synthesize findings into a comprehensive answer"
        );

        state.setPlan(plan);
        state.setCurrentStep(plan.get(0));
        state.setNextAction("execute");
        state.setIterationCount(state.getIterationCount() + 1);

        log.info("PlannerNode: Created plan with {} steps", plan.size());
        return state;
    }
}
