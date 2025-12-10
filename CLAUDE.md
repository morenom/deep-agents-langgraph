# Deep Agent with LangGraph - Minimal Spring Boot Implementation

## Project Overview

Build a minimal but complete Deep Agent system using Java 21 and Spring Boot 3.x that demonstrates the core concepts of LangGraph: stateful agents, multi-step reasoning, and self-correction.

## Technology Stack

- **Java**: 21 (latest LTS)
- **Spring Boot**: 3.2.x
- **Spring AI**: For LLM integration (OpenAI)
- **Build Tool**: Maven
- **Database**: H2 (in-memory for checkpointing)

## Project Structure

```
deep-agent-demo/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/deepagent/
│       │       ├── DeepAgentApplication.java
│       │       ├── config/
│       │       │   └── AgentConfig.java
│       │       ├── model/
│       │       │   ├── AgentState.java
│       │       │   └── ExecutionStep.java
│       │       ├── graph/
│       │       │   ├── AgentGraph.java
│       │       │   ├── node/
│       │       │   │   ├── PlannerNode.java
│       │       │   │   ├── ExecutorNode.java
│       │       │   │   ├── EvaluatorNode.java
│       │       │   │   └── Node.java (interface)
│       │       │   └── edge/
│       │       │       └── EdgeRouter.java
│       │       ├── service/
│       │       │   ├── AgentService.java
│       │       │   └── CheckpointService.java
│       │       └── controller/
│       │           └── AgentController.java
│       └── resources/
│           ├── application.yml
│           └── schema.sql
├── pom.xml
└── README.md
```

## Implementation Requirements

### 1. Core Agent State (`AgentState.java`)

```java
package com.example.deepagent.model;

import java.util.*;

public class AgentState {
    private String threadId;
    private String userQuery;
    private List<String> plan;
    private String currentStep;
    private List<ExecutionStep> executionHistory;
    private String synthesis;
    private double qualityScore;
    private int iterationCount;
    private String nextAction; // "plan", "execute", "evaluate", "finish"
    
    // Constructor, getters, setters
    
    public AgentState copy() {
        // Deep copy for immutability
    }
}
```

### 2. Node Interface (`Node.java`)

```java
package com.example.deepagent.graph.node;

import com.example.deepagent.model.AgentState;

@FunctionalInterface
public interface Node {
    AgentState execute(AgentState state);
}
```

### 3. Planner Node (`PlannerNode.java`)

Create a plan by breaking down the user query into steps.

**Requirements:**
- Use Spring AI's ChatClient to call OpenAI
- Parse LLM response into a list of steps
- Update state with plan and set nextAction to "execute"

### 4. Executor Node (`ExecutorNode.java`)

Execute the current step in the plan.

**Requirements:**
- Execute one step at a time
- Store result in executionHistory
- Move to next step or set nextAction to "evaluate" when done

### 5. Evaluator Node (`EvaluatorNode.java`)

Evaluate if the work is complete and satisfactory.

**Requirements:**
- Use LLM to assess quality (0.0 to 1.0 score)
- If quality >= 0.75 OR iterations >= 3: nextAction = "finish"
- Otherwise: nextAction = "plan" (replan)

### 6. Agent Graph (`AgentGraph.java`)

Orchestrate the flow between nodes.

**Requirements:**
```java
package com.example.deepagent.graph;

import com.example.deepagent.graph.node.*;
import com.example.deepagent.model.AgentState;
import org.springframework.stereotype.Component;

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
        AgentState currentState = initialState;
        
        while (!"finish".equals(currentState.getNextAction()) && 
               currentState.getIterationCount() < 10) {
            
            currentState = switch (currentState.getNextAction()) {
                case "plan" -> planner.execute(currentState);
                case "execute" -> executor.execute(currentState);
                case "evaluate" -> evaluator.execute(currentState);
                default -> throw new IllegalStateException("Unknown action: " + currentState.getNextAction());
            };
        }
        
        return currentState;
    }
    
    public List<AgentState> executeWithTrace(AgentState initialState) {
        List<AgentState> trace = new ArrayList<>();
        AgentState currentState = initialState;
        trace.add(currentState.copy());
        
        while (!"finish".equals(currentState.getNextAction()) && 
               currentState.getIterationCount() < 10) {
            
            currentState = switch (currentState.getNextAction()) {
                case "plan" -> planner.execute(currentState);
                case "execute" -> executor.execute(currentState);
                case "evaluate" -> evaluator.execute(currentState);
                default -> throw new IllegalStateException("Unknown action: " + currentState.getNextAction());
            };
            
            trace.add(currentState.copy());
        }
        
        return trace;
    }
}
```

### 7. Agent Service (`AgentService.java`)

Business logic layer.

**Requirements:**
- Create initial state from user query
- Execute agent graph
- Save/load checkpoints
- Return execution trace for observability

### 8. REST Controller (`AgentController.java`)

**Endpoints:**

```java
POST /api/agent/execute
Body: { "query": "Research quantum computing applications" }
Response: { 
    "threadId": "uuid",
    "finalAnswer": "...",
    "executionTrace": [...],
    "iterations": 3,
    "qualityScore": 0.85
}

GET /api/agent/status/{threadId}
Response: Current state of agent execution

POST /api/agent/resume/{threadId}
Body: { "additionalContext": "..." }
Response: Continues execution from checkpoint
```

### 9. Checkpoint Service (`CheckpointService.java`)

Persist agent state to H2 database.

**Requirements:**
- Save state after each node execution
- Load state by threadId
- Use Spring Data JPA

**Entity:**
```java
@Entity
public class Checkpoint {
    @Id
    private String threadId;
    
    @Column(columnDefinition = "TEXT")
    private String stateJson; // Serialize AgentState to JSON
    
    private LocalDateTime timestamp;
    
    // getters, setters
}
```

### 10. Configuration (`application.yml`)

```yaml
spring:
  application:
    name: deep-agent-demo
  
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
  
  datasource:
    url: jdbc:h2:mem:agentdb
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

server:
  port: 8080

agent:
  max-iterations: 10
  quality-threshold: 0.75
```

### 11. Maven Dependencies (`pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>deep-agent-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Deep Agent Demo</name>
    <description>Minimal Deep Agent implementation with LangGraph concepts</description>
    
    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0-M3</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Starter Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Spring AI OpenAI -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Lombok (optional, for cleaner code) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
</project>
```

## Implementation Steps

### Phase 1: Basic Structure (30 min)
1. Create Spring Boot project with dependencies
2. Implement AgentState and ExecutionStep models
3. Create Node interface
4. Implement basic PlannerNode (just parse a simple prompt)

### Phase 2: Core Graph (45 min)
5. Implement ExecutorNode (simulate execution)
6. Implement EvaluatorNode (simple scoring)
7. Create AgentGraph with routing logic
8. Test graph execution with hardcoded state

### Phase 3: LLM Integration (30 min)
9. Configure Spring AI with OpenAI
10. Update PlannerNode to use actual LLM
11. Update EvaluatorNode to use LLM for scoring
12. Test with real queries

### Phase 4: REST API (30 min)
13. Implement AgentService
14. Create AgentController with endpoints
15. Test via Postman/curl

### Phase 5: Persistence (30 min)
16. Implement CheckpointService with JPA
17. Add checkpoint save/load to AgentService
18. Test state recovery

## Example Usage

### Start the application:
```bash
export OPENAI_API_KEY='your-key-here'
mvn spring-boot:run
```

### Execute an agent:
```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What are the main benefits of microservices architecture?"
  }'
```

### Response:
```json
{
  "threadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "finalAnswer": "Microservices architecture offers several key benefits:\n\n1. Scalability: Individual services can be scaled independently...",
  "executionTrace": [
    {
      "node": "planner",
      "action": "Created 3-step research plan",
      "timestamp": "2024-01-15T10:30:00"
    },
    {
      "node": "executor",
      "action": "Executed step 1: Research scalability benefits",
      "timestamp": "2024-01-15T10:30:15"
    },
    {
      "node": "evaluator",
      "action": "Quality score: 0.85",
      "timestamp": "2024-01-15T10:30:30"
    }
  ],
  "iterations": 1,
  "qualityScore": 0.85,
  "planSteps": [
    "Research scalability benefits",
    "Research development flexibility",
    "Research operational advantages"
  ]
}
```

## Key Concepts Demonstrated

### 1. **Stateful Execution**
- AgentState carries all context through the graph
- Immutable state updates (copy-on-write pattern)
- State persisted at each step

### 2. **Graph-Based Control Flow**
- Nodes represent discrete operations
- Routing based on state (similar to conditional edges in LangGraph)
- Clear separation of concerns

### 3. **Iterative Reasoning**
- Plan → Execute → Evaluate → Replan loop
- Self-correction through quality assessment
- Bounded iteration to prevent infinite loops

### 4. **Observability**
- Execution trace captures each step
- State checkpointing for debugging
- Clear audit trail

### 5. **Production Patterns**
- Spring Boot for production-ready server
- REST API for integration
- Database persistence
- Error handling and validation

## Testing

### Unit Tests Example:

```java
@SpringBootTest
class PlannerNodeTest {
    
    @Autowired
    private PlannerNode planner;
    
    @Test
    void testPlannerCreatesValidPlan() {
        AgentState input = new AgentState();
        input.setUserQuery("Explain quantum computing");
        input.setNextAction("plan");
        
        AgentState output = planner.execute(input);
        
        assertNotNull(output.getPlan());
        assertTrue(output.getPlan().size() >= 2);
        assertTrue(output.getPlan().size() <= 5);
        assertEquals("execute", output.getNextAction());
    }
}
```

### Integration Test Example:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgentControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testAgentExecution() {
        Map<String, String> request = Map.of("query", "Test query");
        
        ResponseEntity<AgentResponse> response = restTemplate.postForEntity(
            "/api/agent/execute",
            request,
            AgentResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getThreadId());
        assertTrue(response.getBody().getIterations() > 0);
    }
}
```

## Extensions (Optional)

### 1. Add Streaming Support
- Use Server-Sent Events (SSE) to stream execution progress
- Update client in real-time as nodes execute

### 2. Add Tool Integration
- Create a ToolNode that can call external APIs
- Demonstrate multi-tool orchestration

### 3. Add Human-in-the-Loop
- Pause execution at certain points
- Require approval via API call to continue

### 4. Add Metrics
- Use Micrometer to track execution time, iterations, cost
- Export to Prometheus/Grafana

### 5. Add Async Execution
- Use Spring's @Async for long-running agents
- Return immediately with status endpoint for polling

## Success Criteria

The implementation successfully demonstrates Deep Agent concepts if:

1. ✅ Agent can break down complex queries into steps
2. ✅ Agent executes steps sequentially with state management
3. ✅ Agent evaluates its own output quality
4. ✅ Agent can iterate/replan based on evaluation
5. ✅ State is persisted and recoverable
6. ✅ Execution trace shows clear decision flow
7. ✅ API is RESTful and well-documented
8. ✅ Code uses Java 21 features (records, pattern matching, etc.)
9. ✅ Integration with LLM works correctly
10. ✅ System handles errors gracefully

## Documentation Requirements

### README.md should include:
- Quick start guide
- API documentation
- Architecture diagram
- Example requests/responses
- Configuration options
- How to run tests

## Notes

- Keep it minimal - focus on core concepts
- Use Java 21 features (records, pattern matching in switch, virtual threads if applicable)
- Prioritize clarity over optimization
- Add comprehensive logging
- Include error handling
- Write clean, self-documenting code

## Estimated Time: 2.5 - 3 hours

This should give you a working Deep Agent system that demonstrates all the key LangGraph concepts in a production-grade Java/Spring Boot implementation, perfect for interview discussions and further experimentation.
