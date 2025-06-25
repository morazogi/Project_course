package DomainLayer.DomainServices;

import ServiceLayer.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // Stores userId -> WebSocketSession mapping
    private static final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenService tokenService;

    @Autowired
    public NotificationWebSocketHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // This method handles incoming WebSocket messages (not used for notifications, but can be extended)
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String resolved = payload;

        // Try to parse the message as JSON
        try {
            JsonNode node = mapper.readTree(payload);
            if (node.has("message")) {
                resolved = node.get("message").asText();
            }
        } catch (Exception ignored) {}

        // Display the message in the UI (for debugging or any future use case)
        sendMessageToUI(session, resolved);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        System.out.println("Extracted token: " + token);  // Log token

        if (token != null) {
            String userId = tokenService.extractUsername(token);  // Extract userId
            System.out.println("Extracted userId: " + userId);  // Log userId

            if (userId != null) {
                userSessions.put(userId, session);  // Store session
                System.out.println("Established WebSocket connection for userId: " + userId);
            } else {
                System.err.println("Failed to extract userId from token.");
            }
        } else {
            System.err.println("Token is null, WebSocket connection failed.");
        }
    }

    // Remove the session when the connection is closed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String token = extractToken(session);
        String userId = tokenService.extractUsername(token);  // Extract username from token

        if (userId != null) {
            userSessions.remove(userId);  // Remove the session
            System.out.println("Closed WebSocket connection for userId: " + userId);
        } else {
            System.err.println("Failed to extract userId from token.");
        }
    }

    // Utility method to extract the token from the WebSocket session URL
    private String extractToken(WebSocketSession session) {
        String query = session.getUri().getQuery(); // e.g., token=abc123
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring("token=".length());
        }
        System.out.println("Extracted token: " + token);  // Log the token for debugging
        return token;
    }

    // Send a notification to the frontend UI (via WebSocket)
    public void sendNotificationToClient(String userId, String payload) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(payload));  // Send message to client
                System.out.println("Sent message to userId: " + userId + ", payload: " + payload);
            } catch (Exception e) {
                System.err.println("Error sending message to userId: " + userId);
                e.printStackTrace();
            }
        } else {
            System.err.println("No active session found for userId: " + userId);  // If no session found
        }
    }

    // Utility method to send message directly to the UI (for logging or debugging purposes)
    private void sendMessageToUI(WebSocketSession session, String message) {
        // You can handle how the UI should process the message here
        // For now, just log it to the console for debugging
        System.out.println("Received message from user: " + message);
    }
}