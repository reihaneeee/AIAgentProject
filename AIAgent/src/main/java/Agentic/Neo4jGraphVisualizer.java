package Agentic;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.*;
import java.util.stream.Collectors;

public class Neo4jGraphVisualizer {
    private final Driver driver;

    // Constructor using Neo4j's Driver
    public Neo4jGraphVisualizer(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void visualizeGraph(List<Task> tasks) {
        System.out.println("Visualizing graph with " + tasks.size() + " tasks. ");
        try (Session session = driver.session()) {
            // Clear existing data
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });

            // Create nodes and relationships
            tasks.forEach(task -> {
                session.writeTransaction(tx -> {
                    // Create task node
                    tx.run("CREATE (t:Task {id: $id, title: $title, priority: $priority, importance: $importance, deadline: $deadline, description: $description})",
                            Map.of(
                                    "id", task.getId(),
                                    "title", task.getTitle(),
                                    "priority", task.getPriority().toString(),
                                    "importance", task.getImportance(),
                                    "deadline", task.getDeadline(),
                                    "description", task.getDescription()
                            ));

                    // Create dependencies
                    task.getDependencies().forEach(depId -> {
                        tx.run("MATCH (a:Task {id: $taskId}), (b:Task {id: $depId}) " +
                                        "CREATE (a)-[:DEPENDS_ON]->(b)",
                                Map.of("taskId", task.getId(), "depId", depId));
                    });
                    return null;
                });
            });

            // Add critical path visualization
            List<String> criticalPath = getCriticalPath(tasks);
            for (int i = 0; i < criticalPath.size() - 1; i++) {
                String current = criticalPath.get(i);
                String next = criticalPath.get(i + 1);
                session.writeTransaction(tx -> {
                    tx.run("MATCH (a:Task {id: $current}), (b:Task {id: $next}) " +
                                    "MERGE (a)-[r:CRITICAL_PATH]->(b) " +
                                    "SET r.color = 'red'",
                            Map.of("current", current, "next", next));
                    return null;
                });
            }
        }
    }

    public void printGraph() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n)-[r]->(m) RETURN n, r, m");

            System.out.println("\nKnowledge Graph Visualization:");
            while (result.hasNext()) {
                Record record = result.next();
                Node startNode = record.get("n").asNode();
                Relationship relationship = record.get("r").asRelationship();

                Node endNode = record.get("m").asNode();

                String relationType = relationship.type();
                String color = relationship.containsKey("color") ?
                        relationship.get("color").asString() : "black";

                System.out.printf("%s [%s] --[%s:%s]--> %s [%s]%n",
                        startNode.get("id").asString(),
                        startNode.get("title").asString(),
                        relationType,
                        color,
                        endNode.get("id").asString(),
                        endNode.get("title").asString());
            }
        }
    }
    private List<String> getCriticalPath(List<Task> tasks) {
        // Implement your critical path algorithm here
        // This is a simplified version
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getDeadline).reversed())
                .map(Task::getId)
                .collect(Collectors.toList());
    }
    public void close() {
        driver.close();
    }
}