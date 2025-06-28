package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
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

@Route("/addnewproduct")
public class AddProductToStoreUI extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;
    private final ButtonPresenter buttonPresenter;


    @Autowired
    public AddProductToStoreUI(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository, StoreRepository storeRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        TextField storeName = new TextField("store name");
        TextField productName = new TextField("product name");
        TextField description = new TextField("description");
        TextField price = new TextField("price");
        TextField quantity = new TextField("quantity");
        TextField category = new TextField("category");
        Span result = new Span("");
        Button addNewProductToStore =  new Button("add new product to store", e -> {
            result.setText(userConnectivityPresenter.addNewProductToStore(token, storeName.getValue(), productName.getValue(), description.getValue(), price.getValue(), quantity.getValue(), category.getValue()));
        });
        connectToWebSocket(token);

        add(new HorizontalLayout(new H1("add product"), buttonPresenter.homePageButton(token)), new HorizontalLayout(storeName, productName, description, price, quantity, category), addNewProductToStore, result);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
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