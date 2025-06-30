package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ProductPresenter;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/searchstore")
public class SearchStoreUI extends VerticalLayout {

    private final ProductPresenter productPresenter;

    @Autowired
    public SearchStoreUI(UserService configuredUserService, IToken configuredTokenService, UserRepository configuredUserRepository) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService, configuredUserRepository);
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);

        TextField storeName = new TextField("store name");
        Button searchStore = new Button("search store", e -> {
            add(productPresenter.searchStore(storeName.getValue(), token));
        });

        add(storeName, searchStore);

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