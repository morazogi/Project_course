package UILayer;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;

import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Route("/login")
public class LoginUI extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;
    private final NotificationService notificationService;

    @Autowired
    public LoginUI(UserService userService,
                   RegisteredService registeredService,
                   OwnerManagerService ownerManagerService,
                   IToken tokenService,
                   UserRepository userRepository,
                   StoreRepository storeRepository,
                   NotificationService notificationService) {

        this.userConnectivityPresenter = new UserConnectivityPresenter(
                userService, registeredService, ownerManagerService, tokenService, userRepository);
        this.notificationService = notificationService;

        try { userService.signUp("a", "1"); userService.signUp("b", "y"); userService.signUp("1", "admin123"); } catch (Exception ignored) {}

        TextField username = new TextField("username");
        PasswordField password = new PasswordField("password");
        Span error = new Span("");

        Button login = new Button("login", e -> {
            String token = "";
            try {
                token = userConnectivityPresenter.login(username.getValue(), password.getValue());
                UI.getCurrent().getSession().setAttribute("token", token);

                UI ui = UI.getCurrent();

                ui.getSession().setAttribute("token", token);
                UI.getCurrent().navigate("/registeredhomepage");
                //UI.getCurrent().navigate("/" + token);
            } catch (Exception exception) {
                error.setText(exception.getMessage());
                }

        });
        add(new H2("login"), username, password, login, error);
        setAlignItems(Alignment.CENTER);
    }
}