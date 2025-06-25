package ServiceLayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-tests for {@link ErrorLogger}.
 *
 * <p>We do <strong>not</strong> modify the constant path because changing
 * a {@code static final} field is brittle on modern JDKs.
 * Instead we create / clean the real log file before every test and
 * inspect its content afterwards.</p>
 */
class ErrorLoggerTest {

    /** Hard-coded in ErrorLogger. */
    private static final Path LOG_FILE =
            Paths.get("src/main/resources/logs/error-log.txt");

    @BeforeEach
    void prepare() throws Exception {
        // make sure parent folders exist
        Files.createDirectories(LOG_FILE.getParent());
        // start with a clean file
        Files.deleteIfExists(LOG_FILE);
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(LOG_FILE);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Tests
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    void logError_writesOneLine() throws Exception {
        ErrorLogger.logError("alice",
                "Failed to delete product",
                "NullPointerException");

        List<String> lines = Files.readAllLines(LOG_FILE);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        assertAll(
                () -> assertTrue(line.contains("alice")),
                () -> assertTrue(line.contains("Failed to delete product")),
                () -> assertTrue(line.contains("NullPointerException"))
        );
    }

    @Test
    void logError_appendsOnSecondCall() throws Exception {
        ErrorLogger.logError("u1", "desc1", "detail1");
        ErrorLogger.logError("u2", "desc2", "detail2");

        List<String> lines = Files.readAllLines(LOG_FILE);
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("u2"));
    }
}
