package ServiceLayer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ErrorLogger {
    private static final String ERROR_LOG_FILE = "src/main/resources/logs/error-log.txt";

    // Method to log errors
    @Transactional
    public static void logError(String username, String description, String errorDetails) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = String.format("[%s] User: %s | Error: %s | Details: %s%n", timestamp, username, description, errorDetails);

        writeToFile(logMessage, ERROR_LOG_FILE);
    }

    // Method to write the log message to the file
    @Transactional
    private static void writeToFile(String logMessage, String logFile) {
        try (FileWriter writer = new FileWriter(new File(logFile), true)) {
            writer.write(logMessage); // Append the log message to the log file
        } catch (IOException e) {
            e.printStackTrace();    // If there's an error writing to the file, print the exception
        }
    }
}
// example between description and error Details
// description - "Failed to delete product"
// errorDetails - "java.lang.NullPointerException: product was null"