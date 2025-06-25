package UILayer;

import DomainLayer.IToken;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("home")
public class HomePageUI extends VerticalLayout {

    private final IToken tokenService;

    public HomePageUI(IToken tokenService) {
        this.tokenService = tokenService;
        UI.getCurrent().getSession().setAttribute("token", tokenService.generateToken("Guest"));
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);

        // Title
        H1 title = new H1("🛍️ Welcome to MarketX");

        // Subtitle
        H3 subtitle = new H3("Your all-in-one platform to manage stores, products, and discounts.");

        // Features list
        VerticalLayout features = new VerticalLayout(
                new Span("✔️ Add and manage products"),
                new Span("✔️ Set smart discounts"),
                new Span("✔️ Track inventory & rating"),
                new Span("✔️ Secure login and access control")
        );
        features.setSpacing(false);
        features.setPadding(false);
        features.setAlignItems(Alignment.START);

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        Button loginButton = new Button("🔐 Login", e -> UI.getCurrent().navigate("/login"));
        Button registerButton = new Button("📝 Register", e -> UI.getCurrent().navigate("/signup"));
        Button searchStoreButton = new Button("Search store", e -> UI.getCurrent().navigate("/searchstore"));
        Button searchProductButton = new Button("Search product", e -> UI.getCurrent().navigate("/searchproduct"));
        buttons.add(loginButton, registerButton);

        // Roles section
        //Span rolesTitle = new Span("👥 Who can use MarketX?");
        VerticalLayout roles = new VerticalLayout(
                new Span("• 👤 User – Browse and buy products"),
                new Span("• 🧑💼 Store Manager – Manage store catalog, discounts & sales"),
                new Span("• 🧑💼 Store Owner – Own multiple stores, assign managers"),
                new Span("• 🛠️ Admin – Oversee system operations and support")
        );
        roles.setSpacing(false);
        roles.setPadding(false);
        roles.setAlignItems(Alignment.START);

        // Footer
        //Span footer = new Span("© 2025 MarketX Project");

        // Add everything to layout
        connectToWebSocket(token);

        //add(title, subtitle, features, buttons, rolesTitle, roles, footer);
        add(title, subtitle, features, buttons, new HorizontalLayout(searchStoreButton, searchProductButton), roles);
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