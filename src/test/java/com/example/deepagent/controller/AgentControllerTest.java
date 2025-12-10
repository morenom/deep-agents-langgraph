package com.example.deepagent.controller;

import com.example.deepagent.dto.AgentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testExecuteEndpoint() throws Exception {
        // Given
        AgentRequest request = new AgentRequest("What are the benefits of microservices?");

        // When & Then
        mockMvc.perform(post("/api/agent/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").exists())
                .andExpect(jsonPath("$.finalAnswer").exists())
                .andExpect(jsonPath("$.executionTrace").isArray())
                .andExpect(jsonPath("$.executionTrace.length()").value(3))
                .andExpect(jsonPath("$.iterations").exists())
                .andExpect(jsonPath("$.qualityScore").exists())
                .andExpect(jsonPath("$.planSteps").isArray())
                .andExpect(jsonPath("$.planSteps.length()").value(3));
    }

    @Test
    void testExecuteEndpointReturnsValidResponse() throws Exception {
        // Given
        AgentRequest request = new AgentRequest("Explain REST APIs");

        // When & Then
        mockMvc.perform(post("/api/agent/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").isString())
                .andExpect(jsonPath("$.finalAnswer").isString())
                .andExpect(jsonPath("$.qualityScore").isNumber())
                .andExpect(jsonPath("$.iterations").isNumber());
    }
}
