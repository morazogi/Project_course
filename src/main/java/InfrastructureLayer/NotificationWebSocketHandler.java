package InfrastructureLayer;

import ServiceLayer.TokenService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private UI ui;

    @Autowired
    private TokenService tokenService;

    public NotificationWebSocketHandler() {}

    public void registerUi(UI ui) { this.ui = ui; }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String resolved = payload;
        try {
            JsonNode node = mapper.readTree(payload);
                        if (node.has("message"))
                                resolved = node.get("message").asText();
        } catch (Exception ignored) {}

        final String textToShow = resolved;              // effectively-final for the lambda
                if (ui != null) {
                        ui.access(() ->
                                    Notification.show(textToShow, 5000, Notification.Position.TOP_CENTER)
                                        );
                    }
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getUri().getQuery().substring("token=".length());
        String userId = tokenService.extractUsername(token);
        userSessions.put(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String token = session.getUri().getQuery().substring("token=".length());
        String userId = tokenService.extractUsername(token);
        userSessions.remove(userId);
    }

    public void sendNotificationToClient(String userId, String payload) {
        WebSocketSession clientSession = userSessions.get(userId);
        if (clientSession != null && clientSession.isOpen()) {
            try {
                clientSession.sendMessage(new TextMessage(payload));
            } catch (Exception ignored) {}
        }
    }
}
