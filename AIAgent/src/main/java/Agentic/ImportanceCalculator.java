package Agentic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class ImportanceCalculator {
    private static final double PRIORITY_WEIGHT = 0.4;
    private static final double DEADLINE_WEIGHT = 0.35;
    private static final double DEPENDENCY_WEIGHT = 0.25;
    private static final double EFFORT_HOURS = 0.2;

    public double calculateImportance(Task task, Set<String> completedTasks) {

        //if priority is null, default to low
        if(task.getPriority() == null) {
            task.setPriority(Priority.MEDIUM);
        }
        double priorityScore = calculatePriorityScore(task);
        double deadLineScore = calculateDeadlineScore(task);
        double dependencyScore = calculateDependencyScore(task, completedTasks);
        //double effortHoursScore = calculateEstimateHours(task);

        return (priorityScore * PRIORITY_WEIGHT) +
                (dependencyScore * DEPENDENCY_WEIGHT) +
                //(effortHoursScore * EFFORT_HOURS) +
                (deadLineScore * DEADLINE_WEIGHT);
    }

    private double calculatePriorityScore(Task task) {
        if(task.getPriority() == null) return 0.0;
        //Normalize priority score(0.0 to 1.0)
        return task.getPriority().getWeight() / (double) Priority.URGENT.getWeight();
    }

    private double calculateDeadlineScore(Task task) {
        long totalHours = Duration.between(LocalDateTime.now(), task.getDeadline()).toHours();
        //Normalize deadline urgency: tasks due within one week are prioritized
        return 1.0 - Math.min(1.0, totalHours / 168.0);
    }

    private double calculateDependencyScore(Task task, Set<String> completedTasks) {
        //if there are no dependencies, return 1.0 (fully available)
        if(task.getDependencies() == null || task.getDependencies().isEmpty()) {
            return 1.0;
        }
        long unmetDependencies = task.getDependencies().stream()
                .filter(dep -> !completedTasks.contains(dep))
                .count();
        double score = 1.0 - (unmetDependencies / (double) task.getDependencies().size());
        return score;
    }

//    private double calculateEstimateHours(Task task) {
    //TODO
//        int estimatedHours = task.getEstimatedEffortHours();
//
//    }
}
