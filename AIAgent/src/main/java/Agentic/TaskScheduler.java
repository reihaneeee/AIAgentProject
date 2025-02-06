// Enhanced TaskScheduler.java
package Agentic;

import java.time.LocalDateTime;
import java.util.*;

public class TaskScheduler {
    private final ProductivityCalculator productivityPattern;
    private final ImportanceCalculator importanceCalculator;

    public TaskScheduler() {
        this.productivityPattern = new ProductivityCalculator();
        this.importanceCalculator = new ImportanceCalculator();

    }

//    public ScheduleResult scheduleRes
    public static class ScheduleResult {
        private final List<Task> scheduledTasks;

        public ScheduleResult(List<Task> scheduledTasks) {
            this.scheduledTasks = scheduledTasks;
        }
    }
}