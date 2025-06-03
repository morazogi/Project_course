package UILayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "UILayer",
        "ServiceLayer",
        "DomainLayer",
        "InfrastructureLayer" })

@SpringBootApplication
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


}
