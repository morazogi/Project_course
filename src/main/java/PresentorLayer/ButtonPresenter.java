package PresentorLayer;

import DomainLayer.IToken;
import ServiceLayer.RegisteredService;
import ServiceLayer.TokenService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;

public class ButtonPresenter {

    private final RegisteredService registeredService;
    private final IToken tokenService;
    public ButtonPresenter(RegisteredService registeredService, IToken tokenService) {
        this.registeredService = registeredService;
        this.tokenService = tokenService;
    }

    public Button signOutButton(String token) {
        Button signOut = new Button("Sign out", e -> {
            try {
                UI.getCurrent().getSession().setAttribute("token", registeredService.logoutRegistered(token));
                UI.getCurrent().navigate("");
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        }
        );
        return signOut;
    }

    public Button loginButton() {
        Button login = new Button("Login", e -> {
            UI.getCurrent().navigate("/login");
        });
        return login;
    }

    public Button homePageButton(String token) {
        Button homePage = new Button("Home page", e -> {
            if (tokenService.extractUsername(token).equals("Guest")) {
                UI.getCurrent().navigate("/guesthomepage");
            } else {
                UI.getCurrent().navigate("/registeredhomepage");
            }
        });
        return homePage;
    }


}