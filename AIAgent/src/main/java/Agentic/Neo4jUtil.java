package Agentic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Neo4jUtil {
    // Load Neo4j configuration from config.properties
    public static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = Neo4jUtil.class.getClassLoader()
                .getResourceAsStream("config.properties")){
            if(input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
