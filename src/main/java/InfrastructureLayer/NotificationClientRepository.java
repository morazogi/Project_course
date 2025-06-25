package InfrastructureLayer;

import DomainLayer.DomainServices.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.net.URI;


@Component
public class NotificationClientRepository {

    private WebSocketSession session;
    @Autowired
    private WebSocketClient clientWebSocket;
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    public NotificationClientRepository() {
    }


    public void connectToServer(String token) {
        try {
            this.session = clientWebSocket.execute(notificationWebSocketHandler, URI.create("ws://localhost:8080/ds-?token=" + token).toString()).get();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendNotification(String notification) {
        try {
            this.session.sendMessage(new TextMessage(notification));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}