package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.ProductPresenter;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;


@Route("/shoppingcart")
public class ShoppingCartUI extends VerticalLayout {

    private final ProductPresenter productPresenter;
    private final ButtonPresenter buttonPresenter;

    @Autowired
    public ShoppingCartUI(RegisteredService configuredRegisteredService, UserService configuredUserService, IToken configuredTokenService, UserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService,configuredUserRepository);
        buttonPresenter = new ButtonPresenter(configuredRegisteredService);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        add(new HorizontalLayout(buttonPresenter.signOutButton(token), new H1("Shopping cart"), buttonPresenter.homePageButton()));

        add(productPresenter.getShoppingCart(token));

        add(new Button("purchase cart", e -> {UI.getCurrent().navigate("/purchasecartfinal");}));

        setPadding(true);
        setAlignItems(Alignment.CENTER);

    }
}