package ServiceLayer;

import DomainLayer.*;
import InfrastructureLayer.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * System Initializer that reads and executes initialization commands from init-state.txt
 * Ensures all operations are legal and complete successfully
 */
@Service
public class SystemInitialiser {

    private final UserService userService;
    private final OwnerManagerService ownerManagerService;
    private final RegisteredService registeredService;
    private final StoreRepository storeRepository;

    // Store tokens for users during initialization
    private Map<String, String> userTokens = new HashMap<>();
    
    // Store store IDs during initialization
    private Map<String, String> storeIds = new HashMap<>();

    /**
     * Constructor with dependency injection
     */
    public SystemInitialiser(UserService userService, 
                           OwnerManagerService ownerManagerService, 
                           RegisteredService registeredService, 
                           StoreRepository storeRepository) {
        // Add null checks to catch injection issues early
        if (userService == null) {
            throw new IllegalArgumentException("UserService cannot be null");
        }
        if (ownerManagerService == null) {
            throw new IllegalArgumentException("OwnerManagerService cannot be null");
        }
        if (registeredService == null) {
            throw new IllegalArgumentException("RegisteredService cannot be null");
        }
        if (storeRepository == null) {
            throw new IllegalArgumentException("StoreRepository cannot be null");
        }
        
        this.userService = userService;
        this.ownerManagerService = ownerManagerService;
        this.registeredService = registeredService;
        this.storeRepository = storeRepository;
        
        System.out.println("✅ SystemInitialiser: Dependencies injected successfully");
        System.out.println("✅ SystemInitialiser: userService = " + userService);
        System.out.println("✅ SystemInitialiser: ownerManagerService = " + ownerManagerService);
        System.out.println("✅ SystemInitialiser: registeredService = " + registeredService);
        System.out.println("✅ SystemInitialiser: storeRepository = " + storeRepository);
    }

    /**
     * Initialize the system by reading and executing commands from init-state.txt
     * @return true if all operations succeed, false otherwise
     */
    public boolean initializeSystem() {
        try {
            System.out.println("🚀 SystemInitialiser: Starting system initialization...");
            System.out.println("📖 SystemInitialiser: Reading init-state.txt file...");
            
            BufferedReader reader = new BufferedReader(new FileReader("init-state.txt"));
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                System.out.println("🔧 SystemInitialiser: Executing line " + lineNumber + ": " + line);
                
                // Parse and execute the command

                if (!executeCommand(line, lineNumber)) {
                    System.err.println("❌ SystemInitialiser: Initialization failed at line " + lineNumber + ": " + line);
                    reader.close();
                    return false;
                }
            }
            
            reader.close();
            System.out.println("✅ SystemInitialiser: System initialization completed successfully!");
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ SystemInitialiser: Error reading init-state.txt: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ SystemInitialiser: Unexpected error during initialization: " + e.getMessage());
            return false;
        }
    }

    /**
     * Parse and execute a single command line
     * @param commandLine the command line to execute
     * @param lineNumber the line number for error reporting
     * @return true if command executed successfully, false otherwise
     */
    private boolean executeCommand(String commandLine, int lineNumber) {
        try {
            // Extract function name and parameters
            Pattern pattern = Pattern.compile("(\\w+)\\(([^)]*)\\)");
            Matcher matcher = pattern.matcher(commandLine);
            
            if (!matcher.find()) {
                System.err.println("Invalid command format at line " + lineNumber);
                return false;
            }
            
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            
            // Parse parameters (remove quotes and split by comma)
            String[] params = parseParameters(parameters);
            
            // Execute the appropriate function
            switch (functionName) {
                case "registration":
                    return executeGuestRegistration(params, lineNumber);
                case "login":
                    return executeLogin(params, lineNumber);
                case "store":
                    return executeOpenStore(params, lineNumber);
                case "manager":
                    return executeAppointManager(params, lineNumber);
                case "product":
                    return executeAddProduct(params, lineNumber);
                case "remove-product":
                default:
                    System.err.println("Unknown command: " + functionName + " at line " + lineNumber);
                    return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error executing command at line " + lineNumber + ": " + e.getMessage());
            return false;   
        }
    }

    /**
     * Parse parameters from a comma-separated string, handling quoted strings
     */
    private String[] parseParameters(String parameters) {
        // Remove outer quotes and split by comma, handling quoted strings
        String[] parts = parameters.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String[] result = new String[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            // Remove surrounding quotes if present
            if (part.startsWith("\"") && part.endsWith("\"")) {
                result[i] = part.substring(1, part.length() - 1);
            } else {
                result[i] = part;
            }
        }
        
        return result;
    }

    private boolean executeGuestRegistration(String[] params, int lineNumber) {
        if (params.length < 2) {
            System.err.println("guest-registration requires at least 2 parameters at line " + lineNumber);
            return false;
        }
        
        try {
            String username = params[0];
            String password = params[1];
            
            // Add null check for userService
            if (userService == null) {
                System.err.println("❌ CRITICAL ERROR: userService is null in executeGuestRegistration!");
                System.err.println("❌ This indicates a dependency injection problem");
                return false;
            }
            
            System.out.println("DEBUG: About to call userService.signUp(" + username + ", " + password + ")");
            System.out.println("DEBUG: userService: " + userService);
            System.out.println("DEBUG: userService class: " + userService.getClass().getName());
            userService.signUp(username, password);
            System.out.println("DEBUG: userService.signUp() completed successfully");
            System.out.println("Registered user: " + username);
            return true;
        } catch (Exception e) {
            System.err.println("DEBUG: Exception in executeGuestRegistration: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    private boolean executeLogin(String[] params, int lineNumber) {
        if (params.length < 2) {
            System.err.println("login requires 2 parameters at line " + lineNumber);
            return false;
        }
        
        try {
            String username = params[0];
            String password = params[1];
            
            String token = userService.login(username, password);
            userTokens.put(username, token);
            System.out.println("Logged in user: " + username);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to login user: " + e.getMessage());
            return false;
        }
    }

    private boolean executeOpenStore(String[] params, int lineNumber) {
        if (params.length < 3) {
            System.err.println("open-store requires 3 parameters at line " + lineNumber);
            return false;
        }
        
        try {
            String ownerUsername = params[0];
            String storeName = params[1];
            String displayName = params[2];
            
            String token = userTokens.get(ownerUsername);
            if (token == null) {
                System.err.println("User " + ownerUsername + " not logged in");
                return false;
            }
            
            String storeId = registeredService.openStore(token, storeName);
            // Store the mapping for later use
            storeIds.put(storeName, storeId);
            System.out.println("Opened store: " + displayName + " (ID: " + storeId + ") owned by " + ownerUsername);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to open store: " + e.getMessage());
            return false;
        }
    }

    private boolean executeAppointManager(String[] params, int lineNumber) {
        if (params.length < 3) {
            System.err.println("appoint-manager requires 3 parameters at line " + lineNumber);
            return false;
        }
        
        try {
            String ownerUsername = params[0];
            String storeName = params[1];
            String managerUsername = params[2];
            
            String storeId = getStoreId(storeName);
            if (storeId == null) {
                System.err.println("Store " + storeName + " not found");
                return false;
            }
            
            ownerManagerService.appointStoreManager(ownerUsername, storeId, managerUsername, new boolean[] {true, true, true, true, true, true, true, true, true, true});
            System.out.println("Appointed " + managerUsername + " as manager of " + storeName);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to appoint manager: " + e.getMessage());
            return false;
        }
    }

    private boolean executeAddProduct(String[] params, int lineNumber) {
        if (params.length < 7) {
            System.err.println("add-product requires 7 parameters at line " + lineNumber);
            return false;
        }
        try {
            String ownerUsername = params[0];
            String storeName = params[1];
            String productName = params[2];
            String description = params[3];
            float price = Float.parseFloat(params[4]);
            int quantity = Integer.parseInt(params[5]);
            String category = params[6];
            
            String storeId = getStoreId(storeName);
            if (storeId == null) {
                System.err.println("Store " + storeName + " not found");
                return false;
            }

            ownerManagerService.addProduct(ownerUsername, storeId, productName, description, price, quantity, category);
            System.out.println("Added product: " + productName + " to store " + storeName);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to add product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the current user tokens (for testing/debugging)
     */
    public Map<String, String> getUserTokens() {
        return new HashMap<>(userTokens);
    }

    /**
     * Get the current store IDs (for testing/debugging)
     */
    public Map<String, String> getStoreIds() {
        return new HashMap<>(storeIds);
    }

    /**
     * Get store ID by store name
     * @param storeName the name of the store
     * @return the store ID if found, null otherwise
     */
    public String getStoreId(String storeName) {
        Store store = storeRepository.getStoreByName(storeName);
        if (store != null) {
            return store.getId();
        }
        return null;
    }

    /**
     * Add a store ID mapping
     * @param storeName the name of the store
     * @param storeId the ID of the store
     */
    public void addStoreId(String storeName, String storeId) {
        storeIds.put(storeName, storeId);
    }
} 
