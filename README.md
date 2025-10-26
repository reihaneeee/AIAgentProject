# 🤖 AI-Powered Task Management Agent

> An intelligent task-scheduling agent that bridges the gap between natural language task descriptions and a fully organized Trello board.

This agent analyzes user inputs, dependencies, and personal productivity patterns to automatically and intelligently schedule tasks. It leverages a Large Language Model (**Llama 3.2**) for decision-making, the **Trello API** for execution, and a **Neo4j** graph database to visualize complex task dependencies.

## 📚 Table of Contents
- [✨ Key Features](#-key-features)
- [⚙️ How It Works: System Architecture](#️-how-it-works-system-architecture)
- [🛠️ Tech Stack](#️-tech-stack)
- [🚀 Getting Started](#-getting-started)
- [💡 Project Learnings & Key Concepts](#-project-learnings--key-concepts)

---

## ✨ Key Features

* **🧠 Intelligent Scheduling:** Uses an LLM (**Llama 3.2**) to understand task context, priority, and user productivity patterns (e.g., "I'm most productive in the morning") to create an optimal schedule.
* **🔄 Trello Integration:** Automatically populates Trello boards and calendars with the scheduled tasks via the **Trello API**.
* **🕸️ Dependency Visualization:** Models all tasks and their dependencies as a graph in a **Neo4j** database, allowing for a clear visual understanding of project flows.
* **📚 Context-Aware Memory:** Implements a retrieval system (**RAG**) to provide the LLM with relevant history and context, leading to smarter and more personalized scheduling decisions over time.

---

## ⚙️ How It Works: System Architecture

The application operates as a multi-step pipeline, transforming user requests into actions.

1.  **Input:** The user provides task details, including metadata, priorities, and dependencies.
2.  **Context Retrieval:** The system queries a vector database (the retrieval system) to fetch relevant historical data or user preferences.
3.  **Prompt Augmentation:** The user's input is combined with the retrieved context into a sophisticated prompt for the LLM.
4.  **LLM Processing:** The prompt is sent to a **Llama 3.2** instance. The LLM analyzes the data and generates a structured `JSON` response containing the optimal schedule and task breakdown.
5.  **Action & Visualization:**
    * The `JSON` response is parsed.
    * **Trello API:** API calls are made to create and organize cards on the user's Trello board and calendar.
    * **Neo4j API:** The task data and its relationships are sent to the Neo4j database using **Cypher** queries to update the project graph.

---

## 🛠️ Tech Stack

* **LLM:** Llama 3.2
* **Scheduling & Ops:** Trello API
* **Graph Database:** Neo4j (queried with Cypher)
* **Retrieval System:** Vector Database (for RAG)
* **Containerization:** Docker & Docker Compose

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone [https://github.com/your-username/AIAgentProject.git](https://github.com/your-username/AIAgentProject.git)
cd AIAgentProject
```
---

### 🔑 2. Set up Environment Variables
Create a `.env` file in the project root and add your API keys:

```dotenv
TRELLO_API_KEY=your_key
TRELLO_API_TOKEN=your_token
NEO4J_URI=bolt://localhost:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=your_password
# ... any other keys
```

---
🐳 3. Build and Run with Docker
This project uses Docker Compose to manage all its services (the main app, Neo4j, and the vector DB).

docker-compose up -d --build

---
🌐 4. Access the Application
The application should now be running. You can interact with it via its API (e.g., at http://localhost:8000).

---
💡 Project Learnings & Key Concepts
This project served as a deep dive into building a modern, agentic AI system. The key technical concepts explored include:

API Integration: Interfacing with heterogeneous systems, including the Trello REST API for external actions and the Neo4j API for internal data modeling.

LLM Interaction: Structuring precise requests to LLMs, handling structured JSON responses, and robustly parsing that data to drive application logic.

Prompt Engineering: Designing and iterating on sophisticated prompts to elicit accurate, structured, and non-hallucinatory JSON outputs from the LLM.

Containerization: Using Docker and docker-compose to create a reproducible, isolated, and multi-container environment for development and deployment.

Graph Databases (Neo4j): Modeling complex, non-linear relationships (task dependencies) in a graph database and using the Cypher query language to create, read, and update the task graph.

Polyglot Persistence: Understanding the distinct advantages of different database paradigms—using a graph database (Neo4j) for its powerful relationship modeling and a vector database for efficient semantic retrieval.

LLM as an Agent: Moving beyond simple text generation to use an LLM as the "brain" of an autonomous system that can perceive (get tasks), plan (create a schedule), and act (call APIs).

Retrieval-Augmented Generation (RAG): Implementing a retrieval system to provide the LLM with long-term memory and external context, significantly improving the quality and personalization of its responses.


Retrieval-Augmented Generation (RAG): Implementing a retrieval system to provide the LLM with long-term memory and external context, significantly improving the quality and personalization of its responses.
