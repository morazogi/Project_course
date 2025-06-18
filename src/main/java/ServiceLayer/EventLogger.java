package ServiceLayer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class EventLogger {
    private static final String EVENT_LOG_FILE = "src/main/resources/logs/event-log.txt";

    // Method to log events
    public static void logEvent(String username, String description) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = String.format("[%s] User: %s | Event: %s | Description: %s%n", timestamp, username, "Event", description);

        writeToFile(logMessage, EVENT_LOG_FILE);
    }

    // Method to write the log message to the file
    public static void writeToFile(String logMessage, String logFileName) {
        try (FileWriter writer = new FileWriter(new File(logFileName), true)) {
            writer.write(logMessage); // Append the log message to the log file
        } catch (IOException e) {
            e.printStackTrace();    // If there's an error writing to the file, print the exception
        }
    }
}
