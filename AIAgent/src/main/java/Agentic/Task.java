package Agentic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String id;
    private String title;
    private String description;
    @JsonFormat(pattern = "estimatedHours")
    private int estimatedEffortHours;
    private Priority priority;
    private List<String> dependencies;
    private double importance;
    private LocalDateTime schedulesStartTime;
    private List<String> requiredResources = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;

    // Constructor and getters/setters
    public Task(String id, String title, String description, LocalDateTime deadline, int estimatedEffortHours, Priority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.estimatedEffortHours = estimatedEffortHours;
        this.dependencies = new ArrayList<>();
    }

    //getters for variables
    public void setId(String id) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        this.id = id.trim();
    }
    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
    }
    public String getTitle() {
        return title;
    }

    public void setDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
        this.deadline = deadline;
    }

    // Add proper serialization for deadlines
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setEstimatedEffortHours(int estimatedEffortHours) {
        if (estimatedEffortHours <= 0) {
            throw new IllegalArgumentException("Estimate effort hours must be positive");
        }
        this.estimatedEffortHours = estimatedEffortHours;
    }
    public int getEstimatedEffortHours() {
        return estimatedEffortHours;
    }

    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.priority = priority;
    }
    public Priority getPriority() {
        return priority;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";

    }
    public String getDescription() {
        return description;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    @JsonProperty("dependencies")
    public List<String> getDependencies() {
        //return dependencies;
        return dependencies;
    }
    public void addDependency(String depId) {
        if (depId == null)
            throw new IllegalArgumentException("Task ID cannot be null");
        if (depId.equals(this))
            throw new IllegalArgumentException("Task cannot depend on itself");
        //dependencies.add(task);
        if (!dependencies.contains(depId))
            dependencies.add(depId);
    }
    public void removeDependency(String task) {
        dependencies.remove(task);
    }

    public void setImportance(double importance) {
        if (importance < 0)
            throw new IllegalArgumentException("Tack importance cannot be nagative");
        this.importance = importance;
    }
    public double getImportance() {
        return importance;
    }

    public void setScheduledStartTime(LocalDateTime schedulesStartTime) {
        this.schedulesStartTime = schedulesStartTime;
    }
    public LocalDateTime getScheduledStartTime() {
        return schedulesStartTime;
    }

    public List<String> getRequiredResources() { return requiredResources;}
    public void setRequiredResources(List<String> resources) { this.requiredResources = resources;}
}
