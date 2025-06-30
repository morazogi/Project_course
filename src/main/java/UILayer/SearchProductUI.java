package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.ProductPresenter;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("/searchproduct")
public class SearchProductUI extends VerticalLayout {

    private final ProductPresenter productPresenter;

    @Autowired
    public SearchProductUI(UserService configuredUserService, IToken configuredTokenService, UserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService, configuredUserRepository);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);

        TextField lowestPrice = new TextField("lowest price");
        TextField highestPrice = new TextField("highest price");
        TextField lowestProductRating = new TextField("lowest product rating");
        TextField highestProductRating = new TextField("highest product rating");
        TextField category = new TextField("category");
        TextField lowestStoreRating = new TextField("lowest store rating");
        TextField highestStoreRating = new TextField("highest store rating");

        TextField productName = new TextField("product name");
        Button searchProduct = new Button("search product by name", e -> {
            add(productPresenter.searchProductByName(token, productName.getValue(), lowestPrice.getValue(), highestPrice.getValue(), lowestProductRating.getValue(), highestProductRating.getValue(), category.getValue(),lowestStoreRating.getValue(), highestStoreRating.getValue()));
        });

        TextField categoryName = new TextField("category name");
        Button searchProductByCategory = new Button("search product by category", e -> {
            add(productPresenter.searchProductByCategory(token, productName.getValue(), lowestPrice.getValue(), highestPrice.getValue(), lowestProductRating.getValue(), highestProductRating.getValue(), category.getValue(),lowestStoreRating.getValue(), highestStoreRating.getValue()));
        });


        add(new H1("search products"), new HorizontalLayout(lowestPrice, highestPrice, lowestProductRating, highestProductRating, category, lowestStoreRating, highestStoreRating), new HorizontalLayout(productName, searchProduct), new HorizontalLayout(categoryName, searchProductByCategory));
    }
    public void connectToWebSocket(String token) {
        UI.getCurrent().getPage().executeJs("""
                window._shopWs?.close();
                window._shopWs = new WebSocket('ws://'+location.host+'/ws?token='+$0);
                window._shopWs.onmessage = ev => {
                  const txt = (()=>{try{return JSON.parse(ev.data).message}catch(e){return ev.data}})();
                  const n = document.createElement('vaadin-notification');
                  n.renderer = r => r.textContent = txt;
                  n.duration = 5000;
                  n.position = 'top-center';
                  document.body.appendChild(n);
                  n.opened = true;
                };
                """, token);
    }
}