package com.example.deepagent.graph;

import com.example.deepagent.graph.node.EvaluatorNode;
import com.example.deepagent.graph.node.ExecutorNode;
import com.example.deepagent.graph.node.PlannerNode;
import com.example.deepagent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AgentGraph {

    private final PlannerNode planner;
    private final ExecutorNode executor;
    private final EvaluatorNode evaluator;

    public AgentGraph(PlannerNode planner, ExecutorNode executor, EvaluatorNode evaluator) {
        this.planner = planner;
        this.executor = executor;
        this.evaluator = evaluator;
    }

    public AgentState execute(AgentState initialState) {
        log.info("AgentGraph: Starting execution for thread: {}", initialState.getThreadId());
        AgentState currentState = initialState;

        while (!"finish".equals(currentState.getNextAction()) &&
                currentState.getIterationCount() < 10) {

            log.info("AgentGraph: Iteration {}, Next action: {}",
                    currentState.getIterationCount(), currentState.getNextAction());

            currentState = switch (currentState.getNextAction()) {
                case "plan" -> planner.execute(currentState);
                case "execute" -> executor.execute(currentState);
                case "evaluate" -> evaluator.execute(currentState);
                default -> throw new IllegalStateException("Unknown action: " + currentState.getNextAction());
            };
        }

        log.info("AgentGraph: Execution completed for thread: {}", currentState.getThreadId());
        return currentState;
    }

    public List<AgentState> executeWithTrace(AgentState initialState) {
        log.info("AgentGraph: Starting execution with trace for thread: {}", initialState.getThreadId());
        List<AgentState> trace = new ArrayList<>();
        AgentState currentState = initialState;
        trace.add(currentState.copy());

        while (!"finish".equals(currentState.getNextAction()) &&
                currentState.getIterationCount() < 10) {

            log.info("AgentGraph: Iteration {}, Next action: {}",
                    currentState.getIterationCount(), currentState.getNextAction());

            currentState = switch (currentState.getNextAction()) {
                case "plan" -> planner.execute(currentState);
                case "execute" -> executor.execute(currentState);
                case "evaluate" -> evaluator.execute(currentState);
                default -> throw new IllegalStateException("Unknown action: " + currentState.getNextAction());
            };

            trace.add(currentState.copy());
        }

        log.info("AgentGraph: Execution with trace completed. Total states: {}", trace.size());
        return trace;
    }
}
