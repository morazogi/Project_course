package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.ProductPresenter;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;


@Route("/shoppingcart")
public class ShoppingCartUI extends VerticalLayout {

    private final ProductPresenter productPresenter;
    private final ButtonPresenter buttonPresenter;

    @Autowired
    public ShoppingCartUI(RegisteredService configuredRegisteredService, UserService configuredUserService, IToken configuredTokenService, IUserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService,configuredUserRepository);
        buttonPresenter = new ButtonPresenter(configuredRegisteredService);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        add(new HorizontalLayout(buttonPresenter.signOutButton(token), new H1("Shopping cart"), buttonPresenter.homePageButton()));

        add(productPresenter.getShoppingCart(token));

        add(new Button("purchase cart", e -> {UI.getCurrent().navigate("/purchasecart");}));

        setPadding(true);
        setAlignItems(Alignment.CENTER);

    }
}
