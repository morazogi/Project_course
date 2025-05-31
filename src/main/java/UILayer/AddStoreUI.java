package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/addstore")
public class AddStoreUI extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;
    @Autowired
    public AddStoreUI(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository, StoreRepository storeRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        TextField storeName = new TextField("store name");
        Span error = new Span("");
        Button addStore = new Button("add store", e -> {
            try {
                userConnectivityPresenter.addStore(token, storeName.getValue());
            } catch (Exception exception) {
                error.setText(exception.getMessage());
            }
        });
        add(new H1("add store"), storeName, addStore, error);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}