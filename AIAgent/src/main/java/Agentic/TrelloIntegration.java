package Agentic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TrelloIntegration {

    //i had to delete the API key and token and list id because of the github rules for keeping my data sage

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TrelloIntegration() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }


    /**
     * Maps a Task (with its optimized schedule parameters) into a TrelloCard payload.
     *
     * The resulting JSON will have:
     * - "idList": the provided Trello list ID.
     * - "name": "Task Title (ID: TaskId)".
     * - "desc": A multi-line description containing Deadline, Estimated Effort, Priority, Description, Notes, and Dependencies.
     * - "due": the scheduled start time (formatted in ISO_LOCAL_DATE_TIME).
     *
     * @param task         The task with optimized scheduling (scheduledStartTime must be set).
     * @param trelloListId The Trello list ID.
     * @return A TrelloCard object.
     */
    public static TrelloCard mapTaskToTrelloCard(Task task, String trelloListId) {
        //Build card name
        String cardName = String.format("%s (ID: %s)", task.getTitle(), task.getId());

        // Format the deadline.
        String deadlineStr = (task.getDeadline() != null)
                ? task.getDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "N/A";
        // Format the scheduled start time.
        String scheduledStart = (task.getScheduledStartTime() != null)
                ? task.getScheduledStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "N/A";

        // Join dependencies (if any).
        String dependencies = (task.getDependencies() != null && !task.getDependencies().isEmpty())
                ? task.getDependencies().stream().collect(Collectors.joining(", "))
                : "None";

        // If Task has a separate notes field, replace "None" below with that value.
        String notes = "None";

        // Build the description.
        String cardDesc = String.format(
                "Deadline: %s%nEstimated Effort: %d hours%nPriority: %s%nDescription: %s%nNotes: %s%nDependencies: %s",
                deadlineStr,
                task.getEstimatedEffortHours(),
                task.getPriority(),
                task.getDescription(),
                notes,
                dependencies
        );

        // Use the scheduled start time as the due date.
        String due = scheduledStart;

        return new TrelloCard(trelloListId, cardName, cardDesc, due);
    }
    public void sendOptimizedScheduleToTrello(List<Task> tasks, String trelloListId) {
        for (Task task : tasks) {
            if (task.getScheduledStartTime() == null) {
                System.out.println("Skipping task " + task.getId() + " as it has no scheduled start time.");
                continue;
            }
            try {

                //Serialize the card to json
                TrelloCard card = mapTaskToTrelloCard(task, trelloListId);
                String cardJson = objectMapper.writeValueAsString(card);
                System.out.println("Trello card JSON:\n" + cardJson);


                //Build API URL
                String url = "https://api.trello.com/1/cards"
                        + "?key=" + API_KEY
                        + "&token=" + API_TOKEN
                         +"&idList=" + trelloListId;

                //Build and send HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(cardJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    System.out.println("Successfully created Trello card for task " + task.getId());
                } else {
                    System.out.println("Failed to create Trello card for task " + task.getId() + ". Response" + response.body());
                }
            } catch (Exception e) {
                System.err.println("Error sending card to Trello: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Inner class representing the JSON payload that Trello expects when creating a card.
     */
    // This DTO represents the JSON payload Trello excepts for creating a card
    public static class TrelloCard {
        @JsonProperty("idList")
        private String idList;
        @JsonProperty("name")
        private String name;
        @JsonProperty("desc")
        private String desc;
        @JsonProperty("due")
        private String due;

        public TrelloCard(String idList, String name, String  desc, String due) {
            this.idList = idList;
            this.name = name;
            this.desc = desc;
            this.due = due;
        }

        // Getters and setters
        public String getIdList() {
            return idList;
        }

        public void setIdList(String idList) {
            this.idList = idList;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getDue() {
            return due;
        }

        public void setDue(String due) {
            this.due = due;
        }
        //For debug
        public String toJson() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
    }
}
