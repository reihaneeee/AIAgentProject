package Agentic;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {
    public static void main(String[] args) {
        // Load Neo4j configuration
        Properties config = Neo4jUtil.loadConfig();
        String neo4jUri = config.getProperty("neo4j.uri");
        String neo4jUser = config.getProperty("neo4j.user");
        String neo4jPassword = config.getProperty("neo4j.password");

        // Initialize KnowledgeGraph with Neo4j connection
        KnowledgeGraph knowledgeGraph = new KnowledgeGraph(neo4jUri, neo4jUser, neo4jPassword);

        Scanner scanner = new Scanner(System.in);
        List<Task> tasks = new ArrayList<>();
        ProductivityCalculator productivityCalculator = new ProductivityCalculator();

        //Allow the user to input their productivity pattern
        System.out.println("Do you want to input your productivity pattern? (yes/no)");
        String input = scanner.nextLine();
        if(input.equalsIgnoreCase("yes")) {
            productivityCalculator.inputProductivityPattern(scanner);
        }

        // Collect tasks from the user
        while (true) {
            System.out.println("Enter task details (or type 'done' to finish):");

            System.out.print("Task ID: ");
            String id = scanner.nextLine();
            if (id.equalsIgnoreCase("done")) break;

            System.out.print("Title: ");
            String title = scanner.nextLine();

            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Deadline (yyyy-MM-dd HH:mm): ");
            //LocalDateTime deadline = LocalDateTime.parse(scanner.nextLine());
            String deadlineInput = scanner.nextLine();
            LocalDateTime deadline = LocalDateTime.parse(deadlineInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            System.out.print("Estimated Hours: ");
            int estimatedHours = Integer.parseInt(scanner.nextLine());

            System.out.print("Priority (URGENT, HIGH, MEDIUM, LOW): ");
            Priority priority = Priority.valueOf(scanner.nextLine().toUpperCase());



            // Create and add the task
            Task task = new Task(id, title, description, deadline, estimatedHours, priority);
            tasks.add(task);
            task.setPriority(priority);

            //After collecting all tasks
            ImportanceCalculator importanceCalculator = new ImportanceCalculator();
            /* Assume tasks are completed initially*/
            Set<String> completedTasks = new HashSet<>();
            for(Task task1 : tasks) {
                double importance = importanceCalculator.calculateImportance(task1, completedTasks);
                task1.setImportance(importance);
            }

            // Add dependencies (if any)
            System.out.print("Dependencies (comma-separated task IDs, or leave blank): ");
            String[] dependencies = scanner.nextLine().split(",");
            for (String depId : dependencies) {
                if (!depId.trim().isEmpty()) {
                    task.addDependency(depId.trim());
                }
            }
        }

        // Send tasks to LLM and get the optimized schedule
        LlamaIntegration llama = new LlamaIntegration();
        String llamaResponse = llama.getOptimizedSchedule(tasks);
        // Display the LLM response
        llama.displaySchedule(llamaResponse);

        for (Task task : tasks) {
            if (task.getScheduledStartTime() == null) {
                System.out.println("\nOptimized Schedule printed above shows details for task " + task.getId() +
                        " (" + task.getTitle() + ").");
                System.out.print("Please enter the scheduled start time for this task in ISO format (yyyy-MM-ddTHH:mm:ss): ");
                String scheduledStartStr = scanner.nextLine();
                LocalDateTime scheduledStart = LocalDateTime.parse(scheduledStartStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                task.setScheduledStartTime(scheduledStart);
            }
            System.out.println("Task " + task.getId() + " scheduled start time: " + task.getScheduledStartTime());
        }

        // Build and visualize the knowledge graph
        knowledgeGraph.buildGraph(tasks);
        knowledgeGraph.printGraph();

        // Assume 'tasks' is your list of scheduled tasks (each Task now has a scheduledStartTime)
        System.out.print("Enter Trello List ID: ");
        String trelloListId = scanner.nextLine();

        TrelloIntegration trelloIntegration = new TrelloIntegration();
        trelloIntegration.sendOptimizedScheduleToTrello(tasks, trelloListId);

        // Clean up
        knowledgeGraph.close();
        scanner.close();
    }
}