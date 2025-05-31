//package InfrastructureLayer;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfigure implements WebSocketConfigurer {
//
//      @Autowired
//    private NotificationWebSocketHandler notificationWebSocketHandler;
//
//    public WebSocketConfigure() {
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(notificationWebSocketHandler,"/ds-").setAllowedOrigins("*").withSockJS();;
//    }
//
////    @Bean
////    public WebSocketHandler notificationWebSocketHandler() {
////        return new NotificationWebSocketHandler();
////    }
//}