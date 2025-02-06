package Agentic;
import java.util.*;


public class KnowledgeGraph {
    private Map<String, TaskNode> taskNodes = new HashMap<>();
    private Map<String, List<String>> dependencies = new HashMap<>();
    private final Neo4jGraphVisualizer neo4j;
//    private Map<String, Integer> resourceAllocation = new HashMap<>();

    public KnowledgeGraph(String neo4jUri, String neo4jUser, String neo4jPassword) {
        this.neo4j = new Neo4jGraphVisualizer(neo4jUri, neo4jUser, neo4jPassword);
    }

    public void buildGraph(List<Task> tasks) {
        tasks.forEach(task -> {
            TaskNode node = new TaskNode(task);
            taskNodes.put(task.getId(), node);
            dependencies.put(task.getId(), new ArrayList<>(task.getDependencies()));
        });
        validateGraph();
        neo4j.visualizeGraph(tasks); // Visualize in Neo4j
    }

    // Validate the graph for cycles
    private void validateGraph() {
        if (hasCycles()) {
            throw new RuntimeException("Task dependencies contain cycles");
        }
    }

    public boolean hasCycles() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String taskId : taskNodes.keySet()) {
            if (detectCycle(taskId, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycle(String taskId, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(taskId)) return true;
        if (visited.contains(taskId)) return false;

        visited.add(taskId);
        recursionStack.add(taskId);

        for (String depId : dependencies.getOrDefault(taskId, Collections.emptyList())) {
            if (detectCycle(depId, visited, recursionStack)) {
                return true;
            }
        }
        recursionStack.remove(taskId);
        return false;
    }
    // Print the graph structure
    public void printGraph() {
        neo4j.printGraph();
    }
    public void close() {
        neo4j.close();
    }

    // Inner class to represent a task node
    private static class TaskNode {
        private Task task;
        private int duration;

        TaskNode(Task task) {
            this.task = task;
            this.duration = task.getEstimatedEffortHours();
        }

        public int getDuration() { return duration; }
    }
}
