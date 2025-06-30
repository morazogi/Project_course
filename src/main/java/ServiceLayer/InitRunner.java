package ServiceLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Event listener that initializes the system after the application has fully started
 * This ensures all beans are properly initialized before running the initialization
 */
@Component
public class InitRunner {

    @Autowired
    private Environment environment;

    @Autowired
    private SystemInitialiser systemInitialiser;

    /**
     * Execute after the application has fully started to ensure all controllers are initialized.
     * Will only run if the RUN_INIT_STATE environment variable is set to "1".
     */
    @EventListener(ApplicationReadyEvent.class)
    public void run(ApplicationReadyEvent event) {
        System.out.println("🔥 InitRunner: ApplicationReadyEvent triggered!");
        System.out.println("🔍 InitRunner: Starting initialization check...");
        System.out.println("🔍 InitRunner: Environment variable RUN_INIT_STATE = " + environment.getProperty("RUN_INIT_STATE"));
        
        String runInitState = environment.getProperty("RUN_INIT_STATE");
        
        if ("1".equals(runInitState)) {
            System.out.println("🚀 InitRunner: RUN_INIT_STATE is set to 1, starting system initialization...");
            try {
                systemInitialiser.initializeSystem();
                System.out.println("✅ InitRunner: System initialization completed successfully!");
            } catch (Exception e) {
                System.err.println("❌ InitRunner: Error during system initialization: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⏭️ InitRunner: RUN_INIT_STATE is not set to 1, skipping initialization. Value: " + runInitState);
        }
    }
} 