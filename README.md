# Deep Agent Demo - LangGraph in Java/Spring Boot

A minimal but complete Deep Agent system using Java 21 and Spring Boot 3.x that demonstrates the core concepts of LangGraph: stateful agents, multi-step reasoning, and self-correction.

## Project Status: PR1 - Foundation with Mock Agent ✅

This is **PR1** of the implementation plan. Currently includes:
- ✅ Complete Spring Boot application with Gradle
- ✅ Stateful agent execution with Plan → Execute → Evaluate → Finish flow
- ✅ Mocked behavior (no LLM yet - coming in PR2)
- ✅ REST API endpoint
- ✅ Basic test coverage

## Technology Stack

- **Java 21** - Latest LTS with modern features (records, pattern matching)
- **Spring Boot 3.2.0** - Production-ready framework
- **Gradle with Kotlin DSL** - Modern build tool
- **Lombok** - Reduce boilerplate
- **JUnit 5** - Testing framework

## Quick Start

### Prerequisites
- Java 21 installed
- Gradle 8.x installed (required for initial setup)

### Initial Setup

First time setup - generate the Gradle wrapper:

```bash
# If you have Gradle installed locally
gradle wrapper

# This downloads the Gradle distribution and creates wrapper files
```

### Build the Project

```bash
# On Windows
gradlew.bat build

# On Linux/Mac
./gradlew build

# Or if you have Gradle installed
gradle build
```

### Run the Application

```bash
# On Windows
gradlew.bat bootRun

# On Linux/Mac
./gradlew bootRun
```

The application starts on `http://localhost:8080`

### Run Tests

```bash
# On Windows
gradlew.bat test

# On Linux/Mac
./gradlew test
```

## API Usage

### Execute Agent

Send a query to the agent:

```bash
curl -X POST http://localhost:8080/api/agent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What are the main benefits of microservices architecture?"
  }'
```

### Example Response

```json
{
  "threadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "finalAnswer": "Based on the query: 'What are the main benefits of microservices architecture?'\n\nExecution Summary:\n1. Research and gather information about: What are the main benefits of microservices architecture?\n   Result: Completed: Research and gather information about: What are the main benefits of microservices architecture?. [This is a mocked execution result for demonstration purposes]\n2. Analyze the gathered information and identify key points\n   Result: Completed: Analyze the gathered information and identify key points. [This is a mocked execution result for demonstration purposes]\n3. Synthesize findings into a comprehensive answer\n   Result: Completed: Synthesize findings into a comprehensive answer. [This is a mocked execution result for demonstration purposes]\n\nConclusion: The agent has successfully completed all planned steps. This demonstrates the Plan → Execute → Evaluate → Finish workflow with mocked behavior.",
  "executionTrace": [
    {
      "stepNumber": 1,
      "stepDescription": "Research and gather information about: What are the main benefits of microservices architecture?",
      "result": "Completed: Research and gather information about: What are the main benefits of microservices architecture?. [This is a mocked execution result for demonstration purposes]",
      "timestamp": "2024-01-15T10:30:15.123"
    },
    {
      "stepNumber": 2,
      "stepDescription": "Analyze the gathered information and identify key points",
      "result": "Completed: Analyze the gathered information and identify key points. [This is a mocked execution result for demonstration purposes]",
      "timestamp": "2024-01-15T10:30:16.456"
    },
    {
      "stepNumber": 3,
      "stepDescription": "Synthesize findings into a comprehensive answer",
      "result": "Completed: Synthesize findings into a comprehensive answer. [This is a mocked execution result for demonstration purposes]",
      "timestamp": "2024-01-15T10:30:17.789"
    }
  ],
  "iterations": 1,
  "qualityScore": 0.85,
  "planSteps": [
    "Research and gather information about: What are the main benefits of microservices architecture?",
    "Analyze the gathered information and identify key points",
    "Synthesize findings into a comprehensive answer"
  ]
}
```

## Architecture

### Agent Flow

```
User Query → Plan → Execute → Evaluate → Finish
                ↑                          ↓
                └──────── (iterate) ───────┘
```

1. **Plan**: Creates a 3-step plan for the query (currently mocked)
2. **Execute**: Executes each step sequentially (currently mocked)
3. **Evaluate**: Assesses quality and decides to finish or replan (currently always finishes)
4. **Finish**: Returns synthesis of all execution steps

### Package Structure

```
com.example.deepagent/
├── DeepAgentApplication.java      # Main Spring Boot application
├── controller/
│   └── AgentController.java       # REST API endpoints
├── dto/
│   ├── AgentRequest.java          # API request DTO
│   └── AgentResponse.java         # API response DTO
├── graph/
│   ├── AgentGraph.java            # Orchestrates node execution
│   └── node/
│       ├── Node.java              # Node interface
│       ├── PlannerNode.java       # Creates execution plans
│       ├── ExecutorNode.java      # Executes plan steps
│       └── EvaluatorNode.java     # Evaluates results
└── model/
    ├── AgentState.java            # Stateful agent context
    └── ExecutionStep.java         # Execution history record
```

### Key Concepts Demonstrated

#### 1. Stateful Execution
- `AgentState` carries all context through the graph
- Immutable state updates via `copy()` method
- State tracks: query, plan, execution history, quality score, iteration count

#### 2. Graph-Based Control Flow
- Nodes implement the `Node` functional interface
- `AgentGraph` routes between nodes using Java 21 switch expressions
- Clear separation of concerns (planning, execution, evaluation)

#### 3. Observability
- `executeWithTrace()` captures full execution history
- Each state transition is logged
- Execution steps include timestamps

## What's Coming Next

### PR2: LLM Integration (Real Intelligence)
- Replace mocked behavior with OpenAI API calls
- Spring AI integration for LLM operations
- Real planning, execution, and evaluation

### PR3: Persistence & State Management
- H2 database for checkpointing
- Save/resume agent execution
- Service layer for business logic

### PR4: Iteration & Self-Correction
- Multi-iteration reasoning
- Re-planning based on quality evaluation
- Adaptive behavior

### PR5: Production Features
- Comprehensive error handling
- Input validation
- Structured logging
- API documentation

## Development

### Project Structure

```
deep-agents-langgraph/
├── build.gradle.kts           # Gradle build configuration
├── settings.gradle.kts        # Gradle settings
├── src/
│   ├── main/
│   │   ├── java/              # Application source code
│   │   └── resources/
│   │       └── application.yml # Configuration
│   └── test/
│       └── java/              # Test source code
└── README.md
```

### Running Tests with Coverage

```bash
./gradlew test jacocoTestReport
```

### IDE Setup

Import as a Gradle project in your IDE:
- **IntelliJ IDEA**: File → Open → Select `build.gradle.kts`
- **Eclipse**: File → Import → Existing Gradle Project
- **VS Code**: Install "Java Extension Pack" and open folder

## Configuration

See `src/main/resources/application.yml`:

```yaml
agent:
  max-iterations: 10          # Maximum agent iterations
  quality-threshold: 0.75     # Quality threshold for completion
```

## Current Limitations (PR1)

Since this is PR1 with mocked behavior:
- Plans are hardcoded (always 3 generic steps)
- Execution results are simulated
- Quality score is fixed at 0.85
- No actual reasoning or LLM integration
- Always completes in 1 iteration

These will be addressed in subsequent PRs!

## Contributing

This is an incremental implementation following a 5-PR plan. Each PR builds on the previous one:
1. **PR1** (current): Foundation with mocked agent
2. **PR2**: LLM integration
3. **PR3**: Persistence
4. **PR4**: Iteration logic
5. **PR5**: Production features

## License

MIT License - feel free to use this as a learning resource or starting point for your own Deep Agent implementations!
