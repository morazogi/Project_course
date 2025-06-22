package PresentorLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.DiscountPolicyMicroservice;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.UserRepository;
import ServiceLayer.EventLogger;
import ServiceLayer.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductPresenter {

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();
    private IToken tokenService;
    private UserRepository userRepository;


    @Autowired
    public ProductPresenter(UserService userService, IToken tokenService, UserRepository userRepository) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public VerticalLayout getShoppingCart(String token) {
        VerticalLayout shoppingCartList = new VerticalLayout();
        List<ShoppingBag> shoppingBags = userService.getShoppingCart(token);
        shoppingCartList.add(new VerticalLayout(new Span("store"), new Span("product\namount\nprice")));
        double totalPayment = 0;
        for (ShoppingBag shoppingBag: shoppingBags) {
            VerticalLayout productList = new VerticalLayout();
            try {
                String storeId = shoppingBag.getStoreId();
                String jsonStore = userService.getStoreById(token, storeId);
                Store store = mapper.readValue(jsonStore, Store.class);
                String storeName = store.getName();
                productList.add(new Span(storeName));
            } catch (Exception e) {
                Notification.show("store with id: " + shoppingBag.getStoreId() + "does not exist");
            }
            for (Map.Entry<String, Integer> product : shoppingBag.getProducts().entrySet()) {
                productList.add(new Span(userService.getProductById(product.getKey()).get().getName() + "\n" + product.getValue() + "\n" + userService.getProductById(product.getKey()).get().getPrice()));
            }
            shoppingCartList.add(productList, new Span("total payment :" + userService.calculateCartPrice(token)));
        }

        return shoppingCartList;
    }

    public VerticalLayout getProductPage(String productId, String storeId) {
        VerticalLayout productPage = new VerticalLayout();
        if (userService.getProductById(productId).isPresent()) {
            Product product = userService.getProductById(productId).get();
            String token = (String) UI.getCurrent().getSession().getAttribute("token");
            String storeName = null;
            try {
                String jsonStore = userService.getStoreById(token, storeId);
                Store store = mapper.readValue(jsonStore, Store.class);
                storeName = store.getName();
            } catch (Exception e) {
                Notification.show("store with id: " + storeId + "does not exist");
            }

            HorizontalLayout upwardsPage = new HorizontalLayout(new H1(product.getName()),new H1(storeName));
            upwardsPage.setAlignItems(FlexComponent.Alignment.CENTER);

            Button addToCart = new Button("add to cart", e -> {
                Notification.show(userService.addToCart(token, storeId, productId, 1));
            });

            HorizontalLayout bottomDescription = new HorizontalLayout(new Span(product.getDescription()), new Span("" + product.getPrice()));

            productPage.add(new VerticalLayout(addToCart, bottomDescription));

            productPage.setPadding(true);
            productPage.setAlignItems(FlexComponent.Alignment.CENTER);

        } else {
            productPage.add(new Span("No product with id:" + productId));
        }
        return productPage;
    }

    public VerticalLayout searchProductByName(String token, String productName, String lowestPrice, String highestPrice, String lowestProductRating, String highestProductRating, String category, String lowestStoreRating, String highestStoreRating) {
        VerticalLayout productList = new VerticalLayout();
        try {
            List<Product> items = userService.getAllProducts(token);
            List<Product> products = items.stream().map(item -> {
                        try {
                            if (item.getName().equals(productName)) {
                                return item;
                            }
                            return null;
                        } catch (Exception exception) {
                            return null;
                        }
                    }).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice))
                    .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice))
                    .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating))
                    .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating))
                    .filter(item -> category.equals("") ? true : item.getCategory().equals(category))
                    .filter(item -> {
                        try {
                            return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .filter(item -> {
                        try {
                            return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .toList();
            for (Product product : products) {
                productList.add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
            }
        } catch (Exception exception) {
            return new VerticalLayout(new Span(exception.getMessage()));
        }
        return productList;
    }

    public VerticalLayout searchProductByCategory(String token, String categoryName, String lowestPrice, String highestPrice, String lowestProductRating, String highestProductRating, String category, String lowestStoreRating, String highestStoreRating) {
        VerticalLayout productList = new VerticalLayout();
        try {
            List<Product> items = userService.getAllProducts(token);
            List<Product> products = items.stream().map(item -> {
                        try {
                            if (item.getCategory().equals(categoryName)) {
                                return item;
                            }
                            return null;
                        } catch (Exception exception) {
                            return null;
                        }
                    }).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice))
                    .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice))
                    .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating))
                    .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating))
                    .filter(item -> category.equals("") ? true : item.getCategory().equals(category))
                    .filter(item -> {
                        try {
                            return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .filter(item -> {
                        try {
                            return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .toList();
            for (Product product : products) {
                productList.add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
            }
        } catch (Exception exception) {
            return new VerticalLayout(new Span(exception.getMessage()));
        }
        return productList;
    }

    public VerticalLayout searchProductInStoreByName(String token, String storeid, String productName, String lowestPrice, String highestPrice, String lowestProductRating, String highestProductRating, String category, String lowestStoreRating, String highestStoreRating, String storeId) {
        VerticalLayout productList = new VerticalLayout();
        try {
            List<Product> items = userService.getProductsInStore(storeid);
            System.out.println(items);
            List<Product> products = items.stream().map(item -> {
                        try {
                            return item;
                        } catch (Exception exception) {
                            return null;
                        }
                    }).filter(item -> productName.equals("") ? true : item.getName().equals(productName))
                    .filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice))
                    .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice))
                    .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating))
                    .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating))
                    .filter(item -> category.equals("") ? true : item.getCategory().equals(category))
                    .filter(item -> {
                        try {
                            return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .filter(item -> {
                        try {
                            return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .toList();
            for (Product product : products) {
                productList.add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
            }
        } catch (Exception exception) {
            return new VerticalLayout(new Span(exception.getMessage()));
        }
        return productList;
    }

    public VerticalLayout searchProductInStoreByCategory(String token, String categoryName, String lowestPrice, String highestPrice, String lowestProductRating, String highestProductRating, String category, String lowestStoreRating, String highestStoreRating, String storeId) {
        VerticalLayout productList = new VerticalLayout();
        try {
            List<String> items = userService.findProduct(token, "", categoryName);
            List<Product> products = items.stream().map(item -> {
                        try {
                            if (mapper.readValue(item, Product.class).getStoreId().equals(storeId)) {
                                return mapper.readValue(item, Product.class);
                            }
                            return null;
                        } catch (Exception exception) {
                            return null;
                        }
                    }).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice))
                    .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice))
                    .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating))
                    .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating))
                    .filter(item -> category.equals("") ? true : item.getCategory().equals(category))
                    .filter(item -> {
                        try {
                            return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .filter(item -> {
                        try {
                            return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating);
                        } catch (JsonProcessingException ex) {
                            Notification.show(ex.getMessage());
                            return false;
                        }
                    })
                    .toList();
            for (Product product : products) {
                productList.add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
            }
        } catch (Exception exception) {
            return new VerticalLayout(new Span(exception.getMessage()));
        }
        return productList;
    }

    public VerticalLayout searchStore(String storeName, String token) {
        VerticalLayout storeList = new VerticalLayout();
        try {
            List<Store> items = userService.searchStoreByName(token, storeName);
            List<Store> stores = items.stream().map(item -> {
                try {
                    return item;
                } catch (Exception exception) {
                    return null;
                }
            }).toList();
            for (Store store : stores) {
                storeList.add(new Button(store.getName() , choose -> {UI.getCurrent().navigate("/store/" + store.getId());}));
            }
        } catch (Exception exception) {
            return new VerticalLayout(new Span(exception.getMessage()));
        }
        return storeList;
    }

    public VerticalLayout getAllProductsInStore(String token, String storeId) {
        VerticalLayout productList = new VerticalLayout();
        List<Product> items = userService.getAllProducts(token);
        List<Product> products = items.stream().map(item -> {
            try {
                if (item.getStoreId().equals(storeId)) {
                    return item;
                }
                return null;
            } catch (Exception exception) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
        for (Product product : products) {
            productList.add(new Button(product.getName() + "\n" + product.getPrice(), e -> {
                UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));

        }
        return productList;
    }

    public VerticalLayout getStorePage(String token, String storeId) {
        VerticalLayout storePage = new VerticalLayout();
        if(!userService.getStoreById(token, storeId).isEmpty()) {
            try {
                Store store = mapper.readValue(userService.getStoreById(token, storeId), Store.class);
                storePage.add(new Span("is the store open now: " + store.isOpen()));
                storePage.add(new Span("store rating: " + store.getRating()));
                storePage.add(new HorizontalLayout(new H1(store.getName()), new Button("search in store", e -> {
                    UI.getCurrent().navigate("/" + "searchproduct" + "/" + storeId);
                })), getAllProductsInStore(token, storeId));
            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        } else {
            return new VerticalLayout(new Span("store does not exist"));
        }
        return storePage;
    }

    @Transactional
    public List<ShoppingBag> userGetShoppingBag(String token) {
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {

        }
        ShoppingCart shoppingCart = user.getShoppingCart();
        List<ShoppingBag> shoppingBag = shoppingCart.getShoppingBags();
        shoppingBag.size();
        for(ShoppingBag shoppingBagd : shoppingBag) {
            shoppingBagd.getProducts().size();
        }
        return shoppingBag;
    }

    //public boolean addProduct(String name, String description,double price, int quantity, double rating, String category, String storeName){
    //   String storeId = "";
    //    for(Map.Entry<String, String> entry : this.storeRepository.getStores().entrySet()){
    //        if (entry.getValue().contains(storeName)) {
    //            storeId = entry.getKey();
    //       }
    //    }
    //    if (storeId.equals("")) {
    //        EventLogger.logEvent("", "adding product failed we found no store named " + storeName);
    //    }
    //    this.storeRepository.
    //    this.productRepository.save();
    //}

}