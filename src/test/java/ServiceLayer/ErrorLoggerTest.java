//package ServiceLayer;
//import org.junit.jupiter.api.*;
//import java.io.*;
//import java.nio.file.*;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ErrorLoggerTest {
//    private static final String LOG_FILE = "src/main/resources/logs/error-log.txt";
//
//    @BeforeEach
//    void clearLogFile() throws IOException {
//        Files.write(Paths.get(LOG_FILE), new byte[0]); // clear the file
//    }
//
//    @Test
//    void logError_ShouldWriteErrorToLogFile() throws IOException {
//        // Arrange
//        String username = "admin";
//        String errorMsg = "Failed to delete product";
//        String errorDetails = "java.lang.IllegalArgumentException: ID not found";
//
//        // Act
//        ErrorLogger.logError(username, errorMsg, errorDetails);
//
//        // Assert
//        List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
//        String logContent = String.join("\n", lines);
//        assertTrue(logContent.contains(username));
//        assertTrue(logContent.contains(errorMsg));
//        assertTrue(logContent.contains(errorDetails));
//    }
//}
