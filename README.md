# System Initialization

This project includes a comprehensive system initialization mechanism that allows you to set up the entire system state from a simple text file.

## Overview

The system initialization process reads commands from `init-state.txt` and executes them in sequence to set up users, stores, products, and policies. The initialization ensures that all operations are legal and complete successfully. If any operation fails, the entire initialization process fails and reports an error.

## Quick Start

### 1. Run the System Initializer

```bash
./run-system-initializer.sh
```

This script will:
- Compile the project using Maven
- Run the SystemInitialiser
- Report success or failure

### 2. Manual Execution

```bash
mvn compile
java -cp target/classes ServiceLayer.SystemInitialiser
```

## init-state.txt File Format

The `init-state.txt` file contains initialization commands in a simple, readable format. Each line represents a command to be executed in order.

### File Structure

```
# Comments start with #
# Empty lines are ignored

# Command format: command-name("param1", "param2", ...)
command-name("value1", "value2", "value3")
```

### Available Commands

#### User Management

**guest-registration(username, password, email, phone)**
- Registers a new user in the system
- Example: `guest-registration("moshe", "moshePassword123", "moshe@email.com", "1234567890")`

**login(username, password)**
- Logs in a user and stores their token
- Example: `login("rina", "rinaPassword123")`

#### Store Management

**open-store(ownerUsername, storeName, displayName)**
- Opens a new store owned by the specified user
- Example: `open-store("rina", "shoes", "Fashion Shoes Store")`

**appoint-manager(ownerUsername, storeName, managerUsername)**
- Appoints a manager to a store with full permissions
- Example: `appoint-manager("rina", "shoes", "moshe")`

#### Product Management

**add-product(ownerUsername, storeName, productName, description, price, quantity, category)**
- Adds a product to a store's inventory
- Example: `add-product("rina", "shoes", "Running Shoes", "Comfortable running shoes", 89.99, 50, "Athletic")`

**remove-product(ownerUsername, storeName, productName)**
- Removes a product from a store's inventory
- Example: `remove-product("rina", "shoes", "Running Shoes")`

#### Shopping Cart Operations

**add-to-cart(username, storeName, productName, quantity)**
- Adds items to a user's shopping cart
- Example: `add-to-cart("david", "shoes", "Running Shoes", 2)`

**remove-from-cart(username, storeName, productName, quantity)**
- Removes items from a user's shopping cart
- Example: `remove-from-cart("david", "shoes", "Running Shoes", 1)`

## Example init-state.txt

```txt
# System Initialization Commands
# This file contains the sequence of operations to initialize the system
# Each line represents a command to be executed in order

# Step 1: Register users
guest-registration("moshe", "moshePassword123", "moshe@email.com", "1234567890")
guest-registration("rina", "rinaPassword123", "rina@email.com", "0987654321")

# Step 2: Login as rina
login("rina", "rinaPassword123")

# Step 3: Open shop as rina
open-store("rina", "shoes", "Fashion Shoes Store")

# Step 4: Appoint moshe as manager
appoint-manager("rina", "shoes", "moshe")

# Step 5: Add some products to the store
add-product("rina", "shoes", "Running Shoes", "Comfortable running shoes", 89.99, 50, "Athletic")
add-product("rina", "shoes", "Dress Shoes", "Elegant dress shoes", 129.99, 30, "Formal")

# Step 6: Register another user for testing
guest-registration("david", "davidPassword123", "david@email.com", "5555555555")

# Step 7: Login as david
login("david", "davidPassword123")

# Step 8: Add items to cart
add-to-cart("david", "shoes", "Running Shoes", 2)

# System initialization complete - all operations should succeed
```

## System Initialization Rules

### Legal Operations
The system ensures that only legal operations are performed:

1. **User Authentication**: Users must be logged in before performing store operations
2. **Store Ownership**: Only store owners can add/remove products or appoint managers
3. **Store Existence**: Stores must exist before products can be added or managers appointed
4. **Product Existence**: Products must exist before they can be added to carts

### Error Handling
- **Fail-Fast**: If any command fails, the entire initialization process stops
- **Detailed Error Messages**: Each error includes the line number and specific failure reason
- **Validation**: Parameters are validated before execution

### Execution Flow
1. Read `init-state.txt` line by line
2. Skip empty lines and comments (starting with `#`)
3. Parse each command to extract function name and parameters
4. Execute the appropriate service layer method
5. Validate the operation was successful
6. Continue to the next command or fail if any operation fails

## Architecture

### SystemInitialiser Class
Located at `src/main/java/ServiceLayer/SystemInitialiser.java`

**Key Features:**
- Spring `@Service` component with dependency injection
- Reads and parses `init-state.txt`
- Executes commands through service layer
- Manages user tokens and store IDs
- Provides comprehensive error reporting

**Dependencies:**
- `UserService`: For user registration and login
- `OwnerManagerService`: For store and product management
- `RegisteredService`: For store operations
- `StoreRepository`: For store ID retrieval

### Command Execution
Each command is executed through the appropriate service layer method:

- **User Operations**: `UserService.signUp()`, `UserService.login()`
- **Store Operations**: `RegisteredService.openStore()`
- **Product Operations**: `OwnerManagerService.addProduct()`, `OwnerManagerService.removeProduct()`
- **Cart Operations**: `UserService.addToCart()`, `UserService.removeFromCart()`

## Testing

### Run System Initializer Tests
```bash
mvn test -Dtest=SystemInitialiserTest
```

### Test Coverage
The test suite covers:
- Basic initialization functionality
- User token management
- Store ID management
- Service integration

## Troubleshooting

### Common Issues

1. **File Not Found**: Ensure `init-state.txt` exists in the project root
2. **Compilation Errors**: Run `mvn clean compile` to rebuild
3. **Permission Denied**: Make sure `run-system-initializer.sh` is executable (`chmod +x run-system-initializer.sh`)
4. **Service Layer Errors**: Check that all required services are properly configured

### Debug Mode
To see detailed execution information, the SystemInitialiser logs:
- Each command being executed
- Success/failure of each operation
- User tokens and store IDs being managed

## Extending the System

### Adding New Commands
1. Add the command to the switch statement in `executeCommand()`
2. Create the corresponding `execute[CommandName]()` method
3. Update this README with the new command documentation

### Customizing Initialization
Modify `init-state.txt` to include your specific initialization sequence. The file format is flexible and supports any combination of the available commands.

## Security Considerations

- **Password Storage**: Passwords are handled by the UserService layer
- **Token Management**: User tokens are managed securely during initialization
- **Access Control**: All operations respect the system's permission model
- **Validation**: All inputs are validated before execution

## Performance

- **Sequential Execution**: Commands are executed in order, one at a time
- **Efficient Parsing**: Uses regex for fast command parsing
- **Memory Management**: Tokens and store IDs are managed efficiently
- **Error Recovery**: Fast failure prevents unnecessary processing

---

For more information about the underlying services and domain model, refer to the individual service documentation in the `src/main/java/ServiceLayer/` directory.
