package DomainLayer;

public class ProxyShipping implements IShipping{
    private IShipping shippingSystem;
    public ProxyShipping(IShipping shippingSystem) {
        this.shippingSystem = shippingSystem;
    }
    public void processShipping(String state, String city, String street, String homeNumber) throws Exception {
        shippingSystem.processShipping(state, city, street, homeNumber);
    }
}
