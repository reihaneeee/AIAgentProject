package Agentic;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;


public class LlamaIntegration {
    //
    private static final int MAX_DAILY_HOURS = 8; // Max hours per day
    private static final String LLAMA_API_ENDPOINT = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "llama3.2";
    private final ScheduleRetrieval scheduleRetrieval;
    //private final KnowledgeGraph knowledgeGraph;

    private final ObjectMapper objectMapper;
    public LlamaIntegration() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) //Register JavaTimeModule
                .enable(SerializationFeature.INDENT_OUTPUT) //Pretty-print Json
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.scheduleRetrieval = new ScheduleRetrieval();
        //this.knowledgeGraph = new KnowledgeGraph();
    }


    public String getOptimizedSchedule(List<Task> tasks) {

        int retries = 3;
        while (retries > 0) {
            try {
                System.out.println("Serialized tasks: " + objectMapper.writeValueAsString(tasks));
                String schedulingPrompt = buildSchedulingPrompt(tasks);
                String payload = objectMapper.writeValueAsString(Map.of(
                        "model", MODEL_NAME,
                        "prompt", schedulingPrompt,
                        "format", "json"
                ));
                System.out.println("Final request payload:\n" + payload);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(LLAMA_API_ENDPOINT))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = HttpClient.newHttpClient()
                        .send(httpRequest, HttpResponse.BodyHandlers.ofString());

                System.out.println("Raw API response:\n" + response.body());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    if (responseBody.contains("\"tasks\"")) {
                        return responseBody;
                    } else {
                        System.err.println("Invalid response format. Retrying...");
                        retries--;
                    }
                } else {
                    throw new RuntimeException("API Error: " + response.statusCode());
                }
            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    throw new RuntimeException("Failed after 3 retries", e);
                }
            }
        }
        throw new RuntimeException("Unable to get valid response from LLM");
    }

    private String buildSchedulingPrompt(List<Task> tasks) throws JsonProcessingException {

        // Retrieve historical context using RAG
        List<HistoricalSchedule> relevantSchedules = scheduleRetrieval.retrieveRelevantSchedules(tasks);

        String historicalContext = relevantSchedules.stream()
                .map(schedule -> formatHistoricalSchedule(schedule))
                .collect(Collectors.joining("\n"));

        String taskJson = objectMapper.writeValueAsString(tasks);
        return String.format("""
                Historical Context from Similar Schedules:
                %s
                Generate a JSON array of scheduled tasks following these rules:
                        1. Use this structure for each task (ONLY include these fields):
                           {
                             "id": "Task_ID",
                             "title": "Task Title",
                             "startTime": "ISO-8601",
                             "endTime": "ISO-8601",
                             "priority": "URGENT/HIGH/MEDIUM/LOW",
                             "deadline": "ISO-8601",
                             "description": "Task Description",
                             "dependencies": ["dep_id1", ...]
                           }
                        2. Strict scheduling rules:
                           - Calculate start/end times based on estimated hours
                           - Max 8 working hours/day (09:00-17:00)
                           - Respect dependencies and deadlines
                           - Schedule dependencies first
                        3. Current time: %s
                        4. Tasks to schedule: %s
                        """,historicalContext, LocalDateTime.now(), taskJson);
    }
    private String formatHistoricalSchedule(HistoricalSchedule schedule) {
        return String.format("""
                Schedule from %s:
                - Total tasks: %d
                - Key tasks: %s
                - Outcome: %s
                """, schedule.getCreatedDate(),
                schedule.getTasks().size(),
                schedule.getTasks().stream()
                        .limit(3)
                        .map(Task::getTitle)
                        .collect(Collectors.joining(", ")),
                schedule.getMetadata().get("outcome")
                );
    }
    private void validateTaskTimestamps(ScheduledTask task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            throw new RuntimeException(
                    "Invalid task timestamps for task: " + task.getId() +
                            ". Start: " + task.getStartTime() + ", End: " + task.getEndTime()
            );
        }
        if (task.getStartTime().isAfter(task.getEndTime())) {
            throw new RuntimeException(
                    "Start time after end time for task: " + task.getId() +
                            ". Start: " + task.getStartTime() + ", End: " + task.getEndTime()
            );
        }
    }
    private void validateSubtaskStructured(List<ScheduledTask> tasks) {
        Map<String, List<ScheduledTask>> groupedTasks = tasks.stream()
                .filter(task -> task.getId().contains("."))
                .collect(Collectors.groupingBy(
                        task -> task.getId().split("\\.")[0]
                ));
        groupedTasks.forEach((parentId, subtask) -> {
            //Verify sequential subtasks
            for(int i=0; i < subtask.size(); i++) {
                ScheduledTask current = subtask.get(i);

                //Check the numbers
                if(!current.getId().endsWith("." + (i + 1))) {
                    throw new RuntimeException("Invalid subtask numbering: " + current.getId());
                }

                //Check daily limits
                long hours = ChronoUnit.HOURS.between(
                        current.getStartTime(),
                        current.getEndTime()
                );
                if(hours > MAX_DAILY_HOURS) {
                    throw new RuntimeException("Subtask exceeds daily limit: " + current.getId() +
                            " (" + hours + "h)"
                    );
                }
            }
        });
    }
    private void validateTimeBlocks(ScheduledTask task) {
        LocalTime start = task.getStartTime().toLocalTime();
        LocalTime end = task.getEndTime().toLocalTime();

        boolean isMorningBlock =
                start.isAfter(LocalTime.of(8, 59)) &&
                        end.isBefore(LocalTime.of(14, 1));

        boolean isEveningBlock =
                start.isAfter(LocalTime.of(16, 59)) &&
                        end.isBefore(LocalTime.of(20, 1));

        if (!isMorningBlock && !isEveningBlock) {
            throw new RuntimeException("Task violates time blocks: " + task.getId());
        }
    }
    private void validateDependencies(List<ScheduledTask> scheduledTasks) {
        Map<String, ScheduledTask> taskMap = scheduledTasks.stream()
                .collect(Collectors.toMap(ScheduledTask::getId, t -> t));

        scheduledTasks.forEach(task -> {
            task.getDependencies().forEach(depId -> {
                if (!taskMap.containsKey(depId)) {
                    throw new RuntimeException(
                            "Task " + task.getId() + " has invalid dependency: " + depId
                    );
                }
                ScheduledTask dependency = taskMap.get(depId);
                if(task.getStartTime().isBefore(dependency.getEndTime())) {
                    throw new RuntimeException(
                            "Task " + task.getId() + " starts before dependency " + depId
                            + " completes\n"
                            + "Task Starts: " + task.getStartTime() + "\n"
                            + "Dependency End: " + dependency.getEndTime()
                    );
                }
            });
        });
    }
    private void validateWithKnowledgeGraph(List<ScheduledTask> scheduledTasks) {}




    /**
     * Parses the LLM response and prints it in a human-readable format.
     *
     * @param llamaResponse JSON response from the LLM
     */
    public void displaySchedule(String llamaResponse) {
        try {
            System.out.println("Raw LLM Response:\n" + llamaResponse); // Debugging

            // Split the response into chunks
            String[] chunks = llamaResponse.split("\n");
            StringBuilder fullResponse = new StringBuilder();

            for (String chunk : chunks) {
                if (!chunk.trim().isEmpty()) {
                    JsonNode jsonNode = objectMapper.readTree(chunk);
                    if (jsonNode.has("response")) {
                        fullResponse.append(jsonNode.get("response").asText());
                    }
                }
            }

            // Parse the combined response
            JsonNode root = objectMapper.readTree(fullResponse.toString());
            if (!root.has("tasks")) {
                //System.err.println("DEBUG: Missing tasks array in: " + llamaResponse);
                throw new RuntimeException("Response does not contain 'tasks' array");
            }

            List<ScheduledTask> tasks = objectMapper.convertValue(
                    root.get("tasks"),
                    new TypeReference<List<ScheduledTask>>() {}
            );

            System.out.println("\nOptimized Schedule:");
            if (!tasks.isEmpty()) {
                //validateDependencies(tasks);
                validateSubtaskStructured(tasks);
                tasks.forEach(task -> {
                    validateTaskTimestamps(task);
                    //validateTimeBlocks(task);
                    System.out.printf(
                            "Task %s: %s\n Dependencies: %s\n Description: %s\n  Start: %s\n  End: %s\n  Priority: %s\n Deadline: %s\n Notes: %s\n",
                            task.getId(),
                            task.getTitle(),
                            task.getDependencies().isEmpty() ? "None" : String.join(", ", task.getDependencies()),
                            task.getDescription() != null ? task.getDescription() : "No description",
                            formatDateTime(task.getStartTime()),
                            formatDateTime(task.getEndTime()),
                            task.getPriority(),
                            formatDateTime(task.getDeadline()),
                            task.getNotes() != null ? task.getNotes() : "No notes"
                    );
                });
            } else {
                System.err.println("No tasks found in response");
            }
            // Build and validate knowledge graph
            //knowledgeGraph.buildGraph(tasks);
            //validateWithKnowledgeGraph(tasks);
        } catch (Exception e) {
            System.err.println("Failed to parse response. Raw output:");
            System.err.println(llamaResponse);
            throw new RuntimeException("Parsing error", e);
        }
    }
//    private void validateWithKnowledgeGraph(List<ScheduledTask> scheduledTasks) {
//        List<String> criticalPath = knowledgeGraph.getCriticalPath();
//        System.out.println("\nCritical Path Analysis:");
//        System.out.println("Tasks on critical path: " + String.join(" -> ", criticalPath));
//
//        scheduledTasks.forEach(task -> {
//            if (criticalPath.contains(task.getId())) {
//                System.out.printf("[CRITICAL] Task %s must be completed on time to avoid delays%n", task.getId());
//            }
//        });
//    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ?
                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                "Not scheduled";
    }

    /**
     * Represents the LLM response structure.
     */
    private static class LlamaResponse {
        private List<ScheduledTask> tasks;
        public List<ScheduledTask> getTasks() {
            return tasks;
        }

        public void setTasks(List<ScheduledTask> tasks) {
            this.tasks = tasks;
        }
    }
    private static LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid timestamp: " + timestamp + ". Using fallback.");
            return LocalDateTime.now().plusHours(1); // Fallback: Current time + 1 hour
        }
    }

    /**
     * Represents a scheduled task in the LLM response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true) // Add this annotation
    public static class ScheduledTask {
        private String id;
        private String title;
        private String description;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDateTime startTime;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDateTime endTime;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDateTime deadline;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> requiredResources  = Collections.emptyList();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String notes;
        private String priority;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> dependencies = Collections.emptyList();

        //Getters and setters
        public String getId() { return id;}
        public void setId(String id) { this.id = id;}
        public String getTitle() {return title;}
        public void setTitle(String title) {this.title = title;}
        public LocalDateTime getDeadline() { return deadline;}

        public void setDeadline(String deadline) {
            this.deadline = parseTimestamp(deadline);
        }

        public String getDescription() { return description;}
        public void setDescription(String description) {this.description = description;}
        public LocalDateTime getStartTime() { return startTime;}
        public void setStartTime(String startTime) {
            this.startTime = parseTimestamp(startTime);
        }
        public LocalDateTime getEndTime() { return  endTime; }
        public void setEndTime(String endTime) {
            this.endTime = parseTimestamp(endTime);
        }
        public List<String> getRequiredResources() { return requiredResources;}
        public void setRequiredResources(List<String> assignedResources) { this.requiredResources = requiredResources;}
        public String getNotes() { return notes;}
        public void setNotes(String notes) { this.notes = notes;}
        public String getPriority() { return  priority; }
        public void setPriority(String priority) { this.priority = priority;}

        public List<String> getDependencies() { return dependencies;}
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies;}
    }
    private static class LlamaRequest {
        private List<Task> tasks;
        public LlamaRequest(List<Task> tasks) {
            this.tasks = tasks;
        }
        public List<Task> getTasks() {
            return tasks;
        }
        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }
    }
}
