package com.example.deepagent.controller;

import com.example.deepagent.dto.AgentRequest;
import com.example.deepagent.dto.AgentResponse;
import com.example.deepagent.graph.AgentGraph;
import com.example.deepagent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentGraph agentGraph;

    public AgentController(AgentGraph agentGraph) {
        this.agentGraph = agentGraph;
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentResponse> execute(@RequestBody AgentRequest request) {
        log.info("Received agent execution request: {}", request.query());

        // Create initial state
        AgentState initialState = AgentState.createInitial(request.query());

        // Execute agent
        AgentState finalState = agentGraph.execute(initialState);

        // Build response
        AgentResponse response = new AgentResponse(
                finalState.getThreadId(),
                finalState.getSynthesis(),
                finalState.getExecutionHistory(),
                finalState.getIterationCount(),
                finalState.getQualityScore(),
                finalState.getPlan()
        );

        log.info("Agent execution completed for thread: {}", finalState.getThreadId());
        return ResponseEntity.ok(response);
    }
}
