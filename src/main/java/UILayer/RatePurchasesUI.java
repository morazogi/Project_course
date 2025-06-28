package UILayer;

import DomainLayer.IToken;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.RatePurchasesPresenter;
import ServiceLayer.RegisteredService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/rate")
public class RatePurchasesUI extends VerticalLayout {

    private final RatePurchasesPresenter presenter;
    private final Span status = new Span();

    @Autowired
    public RatePurchasesUI(IToken tokenSvc,
                           RegisteredService regSvc,
                           InfrastructureLayer.UserRepository userRepo,
                           InfrastructureLayer.ProductRepository prodRepo,
                           InfrastructureLayer.StoreRepository storeRepo) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        presenter = new RatePurchasesPresenter(token, regSvc, userRepo, prodRepo, storeRepo);
        ButtonPresenter btns = new ButtonPresenter(regSvc, tokenSvc);

        /* ── show any “store rated” message persisted from last refresh ─ */
        String persistedMsg = (String) VaadinSession.getCurrent().getAttribute("rateStoreMsg");
        if (persistedMsg != null) {
            Notification.show(persistedMsg);
            VaadinSession.getCurrent().setAttribute("rateStoreMsg", null);
        }

        /* ── product rating widgets ──────────────────────────────────── */
        ComboBox<RatePurchasesPresenter.Item> products = new ComboBox<>("Product");
        products.setItems(presenter.list());
        products.setItemLabelGenerator(RatePurchasesPresenter.Item::productName);

        IntegerField prodRate = new IntegerField("Rate product (1-5)");
        prodRate.setMin(1); prodRate.setMax(5);

        Button rateP = new Button("Rate product", e -> {
            try {
                var it = products.getValue();
                presenter.rateProduct(it.productId(), prodRate.getValue());
                Notification.show("Product rated");
                prodRate.clear(); status.setText("");
            } catch (Exception ex) { status.setText(ex.getMessage()); }
        });

        /* ── store rating widgets ────────────────────────────────────── */
        ComboBox<RatePurchasesPresenter.Item> stores = new ComboBox<>("Store");
        stores.setItems(presenter.list());
        stores.setItemLabelGenerator(RatePurchasesPresenter.Item::storeName);

        IntegerField storeRate = new IntegerField("Rate store (1-5)");
        storeRate.setMin(1); storeRate.setMax(5);

        Button rateS = new Button("Rate store", e -> {
            try {
                var it = stores.getValue();
                presenter.rateStore(it.storeId(), storeRate.getValue());
                /* persist message, then refresh UI */
                VaadinSession.getCurrent().setAttribute("rateStoreMsg", "Store rated");
                UI.getCurrent().getPage().reload();
            } catch (Exception ex) { status.setText(ex.getMessage()); }
        });

        /* ── layout ──────────────────────────────────────────────────── */
        add(new HorizontalLayout(new H1("Rate Your Purchases"),
                        btns.homePageButton(token)),
                products, prodRate, rateP,
                stores,   storeRate, rateS,
                status);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
