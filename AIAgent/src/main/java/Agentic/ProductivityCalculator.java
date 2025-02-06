package Agentic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ProductivityCalculator {
    private Map<LocalTime, Double> productivityMap = new HashMap<>();
    private final Map<Integer, Integer> hourlyWorkload = new HashMap<>();
    private final ObjectMapper objectMapper;

    public ProductivityCalculator() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
        loadProductivityPattern();
    }

    /**
     * Loads the productivity pattern from a JSON file.
     */
    private void loadProductivityPattern() {
        try {
            File file = new File("productivity_pattern.json");
            if(file.exists()) {
                productivityMap.putAll(objectMapper.readValue(file, Map.class));
            } else {
                initializeDefaultPattern();
            }
        } catch (IOException e) {
            System.err.println("Failed to load productivity pattern: " + e.getMessage());
            initializeDefaultPattern();
        }
    }

    /**
     * Initializes the default productivity pattern.
     */
    private void initializeDefaultPattern() {
        setProductivity(LocalTime.of(9, 0), LocalTime.of(12, 0), 1.5);  // Morning focus
        setProductivity(LocalTime.of(13, 0), LocalTime.of(17, 0), 1.2); // Afternoon work
        setProductivity(LocalTime.of(18, 0), LocalTime.of(21, 0), 0.9); // Evening
    }

    /**
     * Sets productivity for a time range.
     *
     * @param start      Start time
     * @param end        End time
     * @param multiplier Productivity multiplier
     */
    public void setProductivity(LocalTime start, LocalTime end, double multiplier) {
        LocalTime current = start;
        while (current.isBefore(end)) {
            productivityMap.put(current, multiplier);
            current = current.plusHours(1);
        }
    }

    /**
     * Gets the productivity multiplier for a specific time.
     *
     * @param time Time of day
     * @return Productivity multiplier
     */
    public double getProductivity(LocalTime time) {
        return  productivityMap.getOrDefault(time, 1.0);
    }

    /**
     * Saves the productivity pattern to a JSON file.
     */
    public void saveProductivityPattern() {
        try {
            File file = new File("productivity_pattern.json");
            objectMapper.writeValue(file, productivityMap);
            System.out.println("Productivity pattern saved to  " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save productivity pattern: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to input their productivity pattern.
     */
    public void inputProductivityPattern(Scanner scanner) {
        System.out.println("Enter your productivity pattern for time ranges (24-hour format)");
        while (true) {
            System.out.println("Enter start time (HH:mm) or 'done' to finish: ");
            String startInput = scanner.nextLine();
            if(startInput.equalsIgnoreCase("done")) break;

            System.out.println("Enter end time (HH:mm): ");
            String endInput = scanner.nextLine();

            System.out.println("Enter productivity multiplayer (e.g 1.5): ");
            double multiplayer = scanner.nextDouble();
            scanner.nextLine(); //consume the newline character

            //parse the time inputs
            LocalTime start = LocalTime.parse(startInput);
            LocalTime end = LocalTime.parse(endInput);

            //Set productivity for the time range
            setProductivity(start, end, multiplayer);
        }
        saveProductivityPattern();
    }

    public double getMultiplier(LocalTime time) {
        return productivityMap.getOrDefault(time, 1.0);
    }


}
