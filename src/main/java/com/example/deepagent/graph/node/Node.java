package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;

@FunctionalInterface
public interface Node {
    AgentState execute(AgentState state);
}
