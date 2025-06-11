package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidUserPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/Bid")
public class Bid extends VerticalLayout {

    private final BidUserPresenter presenter;
    private final Div bidsDisplay = new Div();
    private final Span message = new Span();

    public Bid(IToken tokenService) {
        presenter = new BidUserPresenter();

        add(new H1("Available Bids"));
        updateBidsDisplay();

        TextField productIdField = new TextField("Product ID");
        TextField bidAmountField = new TextField("Your Bid Amount");

        Button placeBidButton = new Button("Place Bid", e -> {
            try {
                String productId = productIdField.getValue().trim();
                double bidAmount = Double.parseDouble(bidAmountField.getValue().trim());

                String resultMessage = presenter.placeBid(productId, bidAmount);
                message.setText(resultMessage);
                updateBidsDisplay();

            } catch (NumberFormatException ex) {
                message.setText("Please enter a valid number for bid amount.");
            }
        });

        add(productIdField, bidAmountField, placeBidButton, message);
        add(bidsDisplay);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    private void updateBidsDisplay() {
        bidsDisplay.removeAll();
        bidsDisplay.add(presenter.getBidsComponent());
    }
}
