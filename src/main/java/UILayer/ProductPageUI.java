package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Store;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.ProductPresenter;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@Route("/product/:productid/:storeid")
public class ProductPageUI extends VerticalLayout implements BeforeEnterObserver {

    private final ProductPresenter productPresenter;
    private final ButtonPresenter buttonPresenter;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ProductPageUI(UserService configuredUserService, IToken configuredTokenService, IUserRepository configuredUserRepository, RegisteredService configuredRegisteredService) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService, configuredUserRepository);
        buttonPresenter = new ButtonPresenter(configuredRegisteredService);
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
        if(parameters.get("productid").isPresent()) {
            String productId = (String) parameters.get("productid").get();
            if (parameters.get("storeid").isPresent()) {
                String storeId = parameters.get("storeid").get();
                String token = (String) UI.getCurrent().getSession().getAttribute("token");
                add(new HorizontalLayout(buttonPresenter.signOutButton(token), buttonPresenter.homePageButton()));
                add(productPresenter.getProductPage(productId, storeId));
                setPadding(true);
                setAlignItems(Alignment.CENTER);
            } else {
                add(new Span("No fitting store"));
            }
        } else {
            add(new Span("No fitting product"));
        }
    }
}