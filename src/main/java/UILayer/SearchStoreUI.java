package UILayer;

import DomainLayer.Store;
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

@Route("/searchstore")
public class SearchStoreUI extends VerticalLayout {

    private final UserService userService;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SearchStoreUI(UserService configuredUserService) {

        this.userService = configuredUserService;
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        TextField storeName = new TextField("store name");
        Button searchStore = new Button("search store", e -> {
            try {
                String jsonItems = userService.searchStoreByName(storeName.getValue());
                List<String> items = (List<String>) mapper.readValue(jsonItems, List.class);
                List<Store> stores = items.stream().map(item -> {
                    try {
                        return mapper.readValue(item, Store.class);
                    } catch (Exception exception) {
                        return null;
                    }
                }).toList();
                for (Store store : stores) {
                    add(new Button(store.getName() , choose -> {UI.getCurrent().navigate("/store/" + store.getId());}));
                }
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        });

        add(storeName, searchStore);

    }
}
