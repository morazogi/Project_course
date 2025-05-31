package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Route("/searchproduct/:storeid")
public class SearchProductInStoreUI extends VerticalLayout implements BeforeEnterObserver {

    private final ProductPresenter productPresenter;
    @Autowired
    public SearchProductInStoreUI(UserService configuredUserService, IToken configuredTokenService, IUserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService, configuredUserRepository);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
        String storeId;
        if (parameters.get("storeid").isPresent()) {
            storeId = parameters.get("storeid").get();
        } else {
            storeId = null;
            add(new Span("No fitting store"));
        }
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        TextField lowestPrice = new TextField("lowest price");
        TextField highestPrice = new TextField("highest price");
        TextField lowestProductRating = new TextField("lowest product rating");
        TextField highestProductRating = new TextField("highest product rating");
        TextField category = new TextField("category");
        TextField lowestStoreRating = new TextField("lowest store rating");
        TextField highestStoreRating = new TextField("highest store rating");

        TextField productName = new TextField("product name");
        Button searchProduct = new Button("search product by name", e -> {
            add(productPresenter.searchProductInStoreByName(token, productName.getValue(), lowestPrice.getValue(), highestPrice.getValue(), lowestProductRating.getValue(), highestProductRating.getValue(), category.getValue(),lowestStoreRating.getValue(), highestStoreRating.getValue(), storeId));
        });

        TextField categoryName = new TextField("category name");
        Button searchProductByCategory = new Button("search product by category", e -> {
            add(productPresenter.searchProductInStoreByCategory(token, productName.getValue(), lowestPrice.getValue(), highestPrice.getValue(), lowestProductRating.getValue(), highestProductRating.getValue(), category.getValue(),lowestStoreRating.getValue(), highestStoreRating.getValue(), storeId));
        });


        add(new H1("search products"), new HorizontalLayout(lowestPrice, highestPrice, lowestProductRating, highestProductRating, category), new HorizontalLayout(productName, searchProduct), new HorizontalLayout(categoryName, searchProductByCategory));

    }

}