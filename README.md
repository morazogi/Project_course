# E-Commerce Management System

A comprehensive e-commerce platform built with Spring Boot, Vaadin UI, and microservices architecture. This system provides store management, user authentication, product management, discount policies, auctions, and more.

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+** or higher
- **Git**

### System Initialization

1. **Clone the repository**
   ```bash
   git clone https://github.com/morazogi/Project_course.git
   cd Project_course
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Main application: http://localhost:8080
   - H2 Database Console: http://localhost:8080/h2-console

## 📋 Configuration Files

### Application Properties (`src/main/resources/application.properties`)

The main configuration file that defines database settings, server configuration, and application behavior:

```properties
# Database Configuration
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:file:./data/mydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Server Configuration
server.port=8080
```

#### Configuration Options

| Property | Description | Default Value |
|----------|-------------|---------------|
| `spring.datasource.url` | Database connection URL | `jdbc:h2:file:./data/mydb` |
| `spring.datasource.username` | Database username | `sa` |
| `spring.datasource.password` | Database password | (empty) |
| `spring.jpa.hibernate.ddl-auto` | Database schema generation | `update` |
| `server.port` | Application server port | `8080` |

### Database Configuration

The system uses H2 database by default, but supports MySQL and PostgreSQL:

#### H2 Database (Default)
```properties
spring.datasource.url=jdbc:h2:file:./data/mydb
spring.datasource.driverClassName=org.h2.Driver
```

#### MySQL Database
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 🏗️ System Architecture

### Layer Structure

```
src/main/java/
├── DomainLayer/           # Business logic and entities
│   ├── domainServices/    # Microservices
│   ├── Roles/            # User role definitions
│   └── ...
├── InfrastructureLayer/   # Data access and external services
├── ServiceLayer/         # Business services
├── UILayer/             # Vaadin UI components
├── PresentorLayer/      # UI presenters
└── utils/               # Utility classes
```

### Key Components

- **Domain Layer**: Core business entities (User, Store, Product, Order, etc.)
- **Microservices**: Discount policies, inventory management, user cart, etc.
- **UI Layer**: Vaadin-based web interface
- **Infrastructure**: Repository implementations and external service proxies

## 🔧 Initial State Configuration

### Database Initialization

The system automatically creates database tables on startup using JPA/Hibernate. The `ddl-auto=update` setting ensures schema updates without data loss.

### Logging Configuration

The system includes comprehensive logging:

- **Error Logs**: `src/main/resources/logs/error-log.txt`
- **Event Logs**: `src/main/resources/logs/event-log.txt`

## 🚀 Running the Application

### Development Mode
```bash
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package
java -jar target/my-project-1.0-SNAPSHOT.jar
```

### Testing
```bash
mvn test
```

## 🌐 External Services

The system integrates with external services:
- **Payment Service**: Credit card processing
- **Shipping Service**: Delivery management
- **Notification Service**: Real-time notifications via WebSocket

🏬 Stores and Owners
🛍️ rina
Store: shoes – Fashion Shoes Store

Manager: moshe

Products:

Running Shoes – Comfortable running shoes – $89.99 – Qty: 50 – Category: Shoes

Dress Shoes – Elegant dress shoes – $129.99 – Qty: 30 – Category: Shoes

💻 river
Store: techStore – Tech Store

Manager: shoham

Products:

mac – Apple computer – $12,999.99 – Qty: 20 – Category: Tech

iphone – Apple iPhone – $1,299.99 – Qty: 30 – Category: Tech

🛒 mor
Store: morStore – Grocery Store

Manager: daniel

Products:

apple – Green apple – $2.99 – Qty: 100 – Category: Fruits

mango – Green mango – $5.99 – Qty: 50 – Category: Fruits

👗 hadar
Store: hadarStore – Clothing Store

Manager: yuval

Products:

tshirt – Blue shirt – $18.99 – Qty: 200 – Category: Clothing

jeans – Black shirt – $28.99 – Qty: 200 – Category: Clothing

