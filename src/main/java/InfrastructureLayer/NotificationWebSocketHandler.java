package InfrastructureLayer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static HashMap<String, WebSocketSession> clientSessions = new HashMap<String, WebSocketSession>();

    public NotificationWebSocketHandler() {

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String notification = message.getPayload();
        UI.getCurrent().access(() -> Notification.show(notification));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getUri().getQuery().substring("token=".length());
        clientSessions.put(token, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clientSessions.remove(session.getUri().getQuery().substring("token=".length()));
    }

    public void sendNotificationToClient(String token, String notificationToClient) {
        for (String tokens : clientSessions.keySet()) {
            if (tokens.equals(token)) {
                WebSocketSession clientSession = clientSessions.get(token);
                if(clientSession != null && clientSession.isOpen()) {
                    try {
                        clientSession.sendMessage(new TextMessage(notificationToClient));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());

                    }
                }
            }
        }
    }

}
