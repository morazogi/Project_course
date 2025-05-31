package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Store;
import PresentorLayer.ProductPresenter;
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

@Route("/store/:storeid")
public class StorePageUI extends VerticalLayout implements BeforeEnterObserver {

    private final ProductPresenter productPresenter;

    @Autowired
    public StorePageUI(UserService configuredUserService, IToken configuredTokenService, IUserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService,configuredUserRepository);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
        if (parameters.get("storeid").isPresent()) {
            UI.getCurrent().getSession().setAttribute("storeId", parameters.get("storeid").get());
        } else {
            add(new Span("No fitting store"));
        }
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        add(productPresenter.getStorePage(token, (String) UI.getCurrent().getSession().getAttribute("storeId")));
    }
}