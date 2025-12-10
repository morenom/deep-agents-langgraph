package com.example.deepagent.graph;

import com.example.deepagent.model.AgentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentGraphTest {

    @Autowired
    private AgentGraph agentGraph;

    @Test
    void testGraphExecutionCompletesSuccessfully() {
        // Given
        AgentState initialState = AgentState.createInitial("What is quantum computing?");

        // When
        AgentState finalState = agentGraph.execute(initialState);

        // Then
        assertNotNull(finalState);
        assertEquals("finish", finalState.getNextAction());
        assertNotNull(finalState.getPlan());
        assertFalse(finalState.getPlan().isEmpty());
        assertNotNull(finalState.getExecutionHistory());
        assertEquals(3, finalState.getExecutionHistory().size()); // 3 steps executed
        assertNotNull(finalState.getSynthesis());
        assertTrue(finalState.getQualityScore() > 0);
        assertTrue(finalState.getIterationCount() > 0);
    }

    @Test
    void testGraphExecutionWithTrace() {
        // Given
        AgentState initialState = AgentState.createInitial("Explain machine learning");

        // When
        List<AgentState> trace = agentGraph.executeWithTrace(initialState);

        // Then
        assertNotNull(trace);
        assertFalse(trace.isEmpty());
        assertTrue(trace.size() > 1); // Should have multiple states

        // First state should be initial
        assertEquals("plan", trace.get(0).getNextAction());

        // Last state should be finished
        AgentState lastState = trace.get(trace.size() - 1);
        assertEquals("finish", lastState.getNextAction());
    }

    @Test
    void testGraphGoesToThroughAllPhases() {
        // Given
        AgentState initialState = AgentState.createInitial("What is Spring Boot?");

        // When
        List<AgentState> trace = agentGraph.executeWithTrace(initialState);

        // Then - verify we went through plan -> execute -> evaluate -> finish
        boolean hasPlan = trace.stream().anyMatch(s -> "plan".equals(s.getNextAction()));
        boolean hasExecute = trace.stream().anyMatch(s -> "execute".equals(s.getNextAction()));
        boolean hasEvaluate = trace.stream().anyMatch(s -> "evaluate".equals(s.getNextAction()));
        boolean hasFinish = trace.stream().anyMatch(s -> "finish".equals(s.getNextAction()));

        assertTrue(hasPlan || trace.get(0).getNextAction().equals("plan"));
        assertTrue(hasExecute);
        assertTrue(hasEvaluate);
        assertTrue(hasFinish);
    }
}
