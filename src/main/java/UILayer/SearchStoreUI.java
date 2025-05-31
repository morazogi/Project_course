package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Store;
import PresentorLayer.ProductPresenter;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route("/searchstore")
public class SearchStoreUI extends VerticalLayout {

    private final ProductPresenter productPresenter;

    @Autowired
    public SearchStoreUI(UserService configuredUserService, IToken configuredTokenService, IUserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService, configuredUserRepository);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        TextField storeName = new TextField("store name");
        Button searchStore = new Button("search store", e -> {
            add(productPresenter.searchStore(storeName.getValue(), token));
        });

        add(storeName, searchStore);

    }
}