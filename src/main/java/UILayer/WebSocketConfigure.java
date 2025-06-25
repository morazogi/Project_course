package UILayer;

import DomainLayer.DomainServices.NotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers /ws before Vaadin’s forward controller.
 * Placed in UILayer so Spring picks it up automatically.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfigure implements WebSocketConfigurer, Ordered {

    private final NotificationWebSocketHandler handler;

    public WebSocketConfigure(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws").setAllowedOrigins("*");
    }

    /* Highest precedence – runs before any MVC mapping */
    @Override
    public int getOrder() { return -100; }
}
