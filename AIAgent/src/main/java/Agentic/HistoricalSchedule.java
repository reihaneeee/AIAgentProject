package Agentic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HistoricalSchedule {
    private LocalDateTime createDate;
    private List<Task> taskList;
    private Map<String, String> metadata;

    public LocalDateTime getCreatedDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public void setTasks(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<Task> getTasks() {
        return taskList;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
