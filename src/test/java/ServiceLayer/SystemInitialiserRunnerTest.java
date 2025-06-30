package ServiceLayer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * Tests for {@link SystemInitialiserRunner}.
 *
 * <p>❗  NOTE:  These tests are deliberately simple.  Their primary goal is
 * to execute as many different code paths as possible so that JaCoCo (or any
 * other coverage tool) will mark those lines as “covered”.<br>
 * <br>
 * We use reflection to reach the runner’s private helpers.  That keeps the
 * production code unchanged while giving us full line-coverage.</p>
 *
 * <p>Because no actual persistence or Spring context is required, the runner
 * works entirely in memory; all we have to do is format command strings
 * correctly.</p>
 */
public class SystemInitialiserRunnerTest {

    private SystemInitialiserRunner runner;
    private Method executeCommand;   // private boolean executeCommand(String,int)
    private Method parseParameters;  // private String[] parseParameters(String)

    @BeforeEach
    void setUp() throws Exception {
        runner = new SystemInitialiserRunner();

        executeCommand = SystemInitialiserRunner.class
                .getDeclaredMethod("executeCommand", String.class, int.class);
        executeCommand.setAccessible(true);

        parseParameters = SystemInitialiserRunner.class
                .getDeclaredMethod("parseParameters", String.class);
        parseParameters.setAccessible(true);
    }

    /* --------------------------------------------------------------------- */
    /*  Simple unit tests for the private helper parseParameters()           */
    /* --------------------------------------------------------------------- */

    @Nested
    @DisplayName("parseParameters()")
    class ParseParameters {

        @Test
        @DisplayName("strips quotes and splits on commas")
        void basicParsing() throws Exception {
            String[] out = (String[]) parseParameters.invoke(
                    runner, "\"a\",\"b\",c");
            assertArrayEquals(new String[]{"a", "b", "c"}, out);
        }

        @Test
        @DisplayName("handles commas *inside* quoted strings")
        void keepsCommasInsideQuotes() throws Exception {
            String[] out = (String[]) parseParameters.invoke(
                    runner, "\"a,b\",c");
            assertArrayEquals(new String[]{"a,b", "c"}, out);
        }

        @Test
        @DisplayName("trims surrounding whitespace")
        void trimsWhitespace() throws Exception {
            String[] out = (String[]) parseParameters.invoke(
                    runner, "  \"x\" ,  \"y\"  ");
            assertArrayEquals(new String[]{"x", "y"}, out);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Black-box command execution via the private executeCommand()         */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("guest-registration succeeds with two params")
    void guestRegistrationSucceeds() throws Exception {
        boolean ok = (boolean) executeCommand.invoke(
                runner, "guest-registration(\"u\",\"p\")", 1);
        assertTrue(ok);
    }

    @Test
    @DisplayName("login succeeds after registration")
    void loginAfterRegistration() throws Exception {
        executeCommand.invoke(runner, "guest-registration(\"uu\",\"pp\")", 1);
        boolean ok = (boolean) executeCommand.invoke(
                runner, "login(\"uu\",\"pp\")", 2);
        assertTrue(ok);
    }

    @Test
    @DisplayName("open-store fails when owner not logged-in")
    void openStoreWithoutLoginFails() throws Exception {
        boolean ok = (boolean) executeCommand.invoke(
                runner, "open-store(\"ghost\",\"s\",\"Store\")", 3);
        assertFalse(ok);
    }

    @Test
    @DisplayName("open-store succeeds when owner is logged-in")
    void openStoreHappyPath() throws Exception {
        executeCommand.invoke(runner, "guest-registration(\"own\",\"pw\")", 1);
        executeCommand.invoke(runner, "login(\"own\",\"pw\")", 2);
        boolean ok = (boolean) executeCommand.invoke(
                runner, "open-store(\"own\",\"s\",\"Store\")", 3);
        assertTrue(ok);
    }

    @Test
    @DisplayName("add-product completes end-to-end")
    void addProductHappyPath() throws Exception {
        // Register, login, open store first
        executeCommand.invoke(runner, "guest-registration(\"o\",\"pw\")", 1);
        executeCommand.invoke(runner, "login(\"o\",\"pw\")", 2);
        executeCommand.invoke(runner, "open-store(\"o\",\"mystore\",\"My Store\")", 3);

        boolean ok = (boolean) executeCommand.invoke(
                runner,
                "add-product(\"o\",\"mystore\",\"p\",\"desc\",12.5,3,\"cat\")",
                4);
        assertTrue(ok);
    }

    @Test
    @DisplayName("remove-product fails when user not logged-in")
    void removeProductNotLoggedIn() throws Exception {
        executeCommand.invoke(runner, "guest-registration(\"o2\",\"pw\")", 1);
        executeCommand.invoke(runner, "open-store(\"o2\",\"s2\",\"Store2\")", 2);

        boolean ok = (boolean) executeCommand.invoke(
                runner, "remove-product(\"o2\",\"s2\",\"prod\")", 3);
        assertFalse(ok); // should fail because o2 never logged-in
    }

    @Test
    @DisplayName("add-to-cart fails when store is missing")
    void addToCartStoreMissing() throws Exception {
        executeCommand.invoke(runner, "guest-registration(\"a\",\"pw\")", 1);
        executeCommand.invoke(runner, "login(\"a\",\"pw\")", 2);

        boolean ok = (boolean) executeCommand.invoke(
                runner, "add-to-cart(\"a\",\"nosuch\",\"p\",1)", 3);
        assertFalse(ok);
    }

    @Test
    @DisplayName("remove-from-cart happy path")
    void removeFromCartHappyPath() throws Exception {
        // Full flow: register, login, store, product, add-to-cart, remove-from-cart
        executeCommand.invoke(runner, "guest-registration(\"b\",\"pw\")", 1);
        executeCommand.invoke(runner, "login(\"b\",\"pw\")", 2);
        executeCommand.invoke(runner, "open-store(\"b\",\"s3\",\"S3\")", 3);
        executeCommand.invoke(runner, "add-product(\"b\",\"s3\",\"p3\",\"d\",1.0,2,\"c\")", 4);
        executeCommand.invoke(runner, "add-to-cart(\"b\",\"s3\",\"p3\",1)", 5);

        boolean ok = (boolean) executeCommand.invoke(
                runner, "remove-from-cart(\"b\",\"s3\",\"p3\",1)", 6);
        assertTrue(ok);
    }

    @Test
    @DisplayName("unknown command returns false (graceful handling)")
    void unknownCommandGracefullyFails() throws Exception {
        boolean ok = (boolean) executeCommand.invoke(
                runner, "does-not-exist()", 99);
        assertFalse(ok);
    }

    /* --------------------------------------------------------------------- */
    /*  Smoke test: initializeSystem() doesn’t explode                       */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("initializeSystem() completes without throwing")
    void initializeSystemDoesNotThrow() {
        // Whether the file exists or not, it must not throw an exception.
        assertDoesNotThrow(() -> runner.initializeSystem());
    }
}
