package PresentorLayer;

import ServiceLayer.BidService;
import ServiceLayer.UserService;
import DomainLayer.BidSale;
import DomainLayer.Product;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.List;

public class BidUserPresenter {

    private final String username;
    private final String token;
    private final BidService bidService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BidUserPresenter(String username,
                            String token,
                            BidService bidService,
                            UserService userService) {
        this.username   = username;
        this.token      = token;
        this.bidService = bidService;
        this.userService= userService;
    }

    public Component getBidsComponent() {
        Div box = new Div();
        List<BidSale> list = bidService.open();
        if (list.isEmpty()) { box.add(new Span("No bids available")); return box; }

        for (BidSale b : list) {
            String storeName="?"; String productName="?";
            try {
                Store s = mapper.readValue(
                        userService.getStoreById(token, b.getStoreId()), Store.class);
                Product p = userService.getProductById(b.getProductId()).orElse(null);
                storeName = s.getName();  productName = p!=null? p.getName():"?";
            } catch(Exception ignored){}

            Span line = new Span(
                    storeName + " – " + productName +
                            " | $" + b.getCurrentPrice() +
                            (b.getCurrentBidder()!=null ? " ("+ b.getCurrentBidder()+")":"") +
                            (b.isAwaitingPayment() ? " [awaiting payment]":"")
            );
            box.add(line);

            if (b.isAwaitingPayment() && username.equals(b.getWinner())) {
                Span pay = new Span("  → Pay now");
                pay.getStyle().set("color","blue").set("cursor","pointer");
                String id=b.getId();
                pay.addClickListener(e -> UI.getCurrent().navigate("/bidpay/"+id));
                box.add(pay);
            }
            box.add(new Div());
        }
        return box;
    }

    public String placeBid(String storeName,String productName,double amount) {
        for (BidSale b : bidService.open()) {
            try {
                Store s = mapper.readValue(
                        userService.getStoreById(token, b.getStoreId()), Store.class);
                Product p = userService.getProductById(b.getProductId()).orElse(null);

                if (s.getName().equalsIgnoreCase(storeName)
                        && p!=null && p.getName().equalsIgnoreCase(productName)
                        && !b.isAwaitingPayment()) {
                    bidService.place(b.getId(), username, amount);
                    return "Bid placed!";
                }
            } catch(Exception ignored){}
        }
        return "Matching bid not found / finished.";
    }

    public void pay(String bidId, String token, String name, String cardNumber, String expirationDate, String cvv, String state, String city, String address, String id, String zip) {
        bidService.pay(bidId, token, name, cardNumber, expirationDate, cvv, state, city, address, id, zip);
    }
}
