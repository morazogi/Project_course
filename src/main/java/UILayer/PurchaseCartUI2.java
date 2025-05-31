package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/purchasecartfinal")
public class PurchaseCartUI2 extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;

    @Autowired
    public PurchaseCartUI2(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        TextField paymentMethod = new TextField("payment method");
        TextField cardNumber = new TextField("card number");
        TextField expirationDate = new TextField("expiration date");
        TextField cvv = new TextField("cvv");
        TextField state = new TextField("state");
        TextField city = new TextField("city");
        TextField street = new TextField("street");
        TextField homeNumber = new TextField("home number");
        Span error = new Span("");
        Button purchaseCart =  new Button("purchase cart", e -> {
            try {
                userConnectivityPresenter.purchaseCart(token, paymentMethod.getValue(), cardNumber.getValue(), expirationDate.getValue(), cvv.getValue(), state.getValue(), city.getValue(), street.getValue(), homeNumber.getValue());
            } catch (Exception exception) {
                error.setText(exception.getMessage());
            }
        });

        add(new H1("purchase cart"), new HorizontalLayout(paymentMethod, cardNumber, expirationDate, cvv), new HorizontalLayout(state, city, street, homeNumber), purchaseCart, error);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}