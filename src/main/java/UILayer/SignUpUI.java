package UILayer;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/signup")
public class SignUpUI extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;

    @Autowired
    public SignUpUI(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository, StoreRepository storeRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);

        TextField username = new TextField("username");
        PasswordField password = new PasswordField("password");
        Span error = new Span("");
        Button login = new Button("sign up", e -> {
            try {
                userConnectivityPresenter.signUp(username.getValue(), password.getValue());
                UI.getCurrent().navigate("");
            } catch (Exception exception) {
                error.setText(exception.getMessage());
            }
        });
        add(new H2("sign up"), username, password, login, error);
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

}