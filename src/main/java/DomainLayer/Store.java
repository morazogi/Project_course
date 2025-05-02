package DomainLayer;
import ServiceLayer.PaymentService;
import ServiceLayer.ProductService;

import java.util.*;

public class Store {
    private String id;
    private PurchasePolicy purchasePolicy;
    private List<User> users = new ArrayList<>();
    private Map<Product, Integer> products = new HashMap<>();
    private ProductService productService;
    private PaymentService paymentService;
    private boolean openNow;
    private int rating;

    public Store() {
        this.id = "-1"; //currently doesnt have id as it gets one only when its added to the store repository
        openNow = true;
    }

    /**
     * use this function to detect if the store is open now so the logic is not depended on the boolean itself.
     * for example a store that despite being open would like to automatically open and close in certain hours.
     * @return a boolean that says if the store is open right now
     */
    public boolean isOpenNow() {
        return openNow;
    }
    public void openTheStore() {
        openNow = true;
    }
    public void closeTheStore() {
        openNow = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;

    }
    
    public int getRating(){
        return rating;
    }

    public void setRating(int rating){
        this.rating = rating;
    }

    public String getName() {
        return id;
    }


    public Boolean registerUser(User user) {
        if(users.contains(user)) {
            return false;
        }
        users.add(user);
        return true;
    }

    public boolean increaseProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        int currentQuantity = products.get(product);
        products.put(product, Integer.valueOf(currentQuantity + quantity));
        return true;
    }


    public boolean decreaseProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        int currentQuantity = products.get(product);
        if (quantity > currentQuantity) {
            return false;
        }

        int updatedQuantity = currentQuantity - quantity;

        products.put(product, Integer.valueOf(updatedQuantity));

        productService.decreaseQuantity(product.getId(), quantity);
        return true;
    }


    public boolean changeProductQuantity(Product product, int newQuantity) {
        if (newQuantity < 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        if (newQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(newQuantity));
        }

        return true;
    }


    public boolean removeProduct(Product product) {
        if (!products.containsKey(product)) {
            return false;
        }

        products.remove(product);
        return true;
    }


    public boolean addNewProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product) || products.get(product) == 0) {
            products.put(product, Integer.valueOf(quantity));
            return true;
        }

        return false; // Product already exists with quantity > 0
    }


    private Product findProductById(String productId) {
        for (Product product : products.keySet()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }



    public boolean increaseProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        int currentQuantity = products.get(product);
        products.put(product, Integer.valueOf(currentQuantity + quantity));
        return true;
    }

    public boolean decreaseProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        int currentQuantity = products.get(product);
        if (quantity > currentQuantity) {
            return false;
        }

        int updatedQuantity = currentQuantity - quantity;
        products.put(product, Integer.valueOf(updatedQuantity));

        productService.decreaseQuantity(product.getId(), quantity);
        return true;
    }

    public boolean changeProductQuantity(String productId, int newQuantity) {
        if (newQuantity < 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        if (newQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(newQuantity));
        }

        return true;
    }

    public boolean removeProduct(String productId) {
        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        products.remove(product);
        return true;
    }

    public boolean addNewProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        if (!products.containsKey(product) || products.get(product) == 0) {
            products.put(product, Integer.valueOf(quantity));
            return true;
        }

        return false;
    }


    public int calculateProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return -1;
        }
        if (!products.containsKey(product)) {
            return -1;
        }
        if (quantity > products.get(product)) {
            return -1;
        }

        return product.getPrice() * quantity;            //got to decide how price works
    }


    public int sellProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return -1;
        }
        if (!products.containsKey(product)) {
            return -1;
        }
        if (quantity > products.get(product)) {
            return -1;
        }

        int updatedQuantity = products.get(product) - quantity;
        if (updatedQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(updatedQuantity));
        }
        productService.decreaseQuantity(product.getId(), quantity);       //Changed according productService implementation
        return product.getPrice() * quantity;            //got to decide how price works
    }

    public boolean availableProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        return products.containsKey(product) && products.get(product) >= quantity;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nUsers:\n");
        for (User user : users) {
            sb.append(user.toString()).append("\n");
        }

        sb.append("\nAll Products in Store:\n");
        for (Product product : products.keySet()) {
            sb.append(product.toString()).append("\n");
        }

        return sb.toString();
    }

    public String getOrderHistory() {
        //returns an order history
        return "";
    }

    public void close() {
        openNow = false;
    }

    public void setProductService(ProductService mockProductService) {
        productService=mockProductService;
    }
}
