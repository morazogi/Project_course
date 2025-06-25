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

@Route("/addstore")
public class AddStoreUI extends VerticalLayout {

    private final UserConnectivityPresenter userConnectivityPresenter;
    private final ButtonPresenter buttonPresenter;

    @Autowired
    public AddStoreUI(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository, StoreRepository storeRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);
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
        connectToWebSocket(token);

        add(new HorizontalLayout(new H1("add store"), buttonPresenter.homePageButton(token)), storeName, addStore, error);
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