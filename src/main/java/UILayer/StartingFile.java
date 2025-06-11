package UILayer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // Add this import

@SpringBootApplication
@EnableJpaRepositories(basePackages = "DomainLayer") // Add this annotation
@EntityScan(basePackages = {"DomainLayer", "utils"})  // Add this line
public class StartingFile {

    public static void main(String[] args) {
        SpringApplication.run(StartingFile.class, args);
    }

    @Bean
    public ServletWebServerFactory webServerFactory() {
        // Set the server port to 8080 or any other port
        ConfigurableServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(8080);  // Ensure this is the port your WebSocket is expecting
        return factory;
    }

    @Bean
    public CommandLineRunner checkDatabaseContent(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("\n📦 Table: " + tableName);

                    try (ResultSet rows = conn.createStatement().executeQuery("SELECT * FROM " + tableName)) {
                        int columnCount = rows.getMetaData().getColumnCount();
                        while (rows.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = rows.getMetaData().getColumnName(i);
                                String value = rows.getString(i);
                                System.out.print(columnName + ": " + value + " | ");
                            }
                            System.out.println();
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Failed to read table '" + tableName + "': " + e.getMessage());
                    }
                }
            }
        };
    }


}