//package UILayer;
//
//import DomainLayer.IToken;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.router.Route;
//import InfrastructureLayer.NotificationClientRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Route("/notification")
//public class NotificationSUI extends VerticalLayout {
//
//    private NotificationClientRepository notificationClientRepository;
//    private IToken tokenService;
//
//    @Autowired
//    public NotificationSUI(NotificationClientRepository configuredNotificationClientRepository, IToken configuredTokenService) {
//        this.notificationClientRepository = configuredNotificationClientRepository;
//        this.tokenService = configuredTokenService;
//        TextField username = new TextField("username");
//        TextField notification = new TextField("notification to send");
//        add(username);
//
//        add(notification);
//
//        add(new Button("send notification", e -> {String token = tokenService.generateToken(username.getValue());
//            notificationClientRepository.connectToServer(token);
//            notificationClientRepository.sendNotification(notification.getValue());
//        }));
//    }
//}