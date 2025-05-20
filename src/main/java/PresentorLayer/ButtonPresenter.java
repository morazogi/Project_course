package PresentorLayer;

import ServiceLayer.RegisteredService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;

public class ButtonPresenter {

    private final RegisteredService registeredService;
    public ButtonPresenter(RegisteredService registeredService) {
        this.registeredService = registeredService;
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

    public Button homePageButton() {
        Button homePage = new Button("Home page", e -> {
            UI.getCurrent().navigate("/home");
        });
        return homePage;
    }


}
