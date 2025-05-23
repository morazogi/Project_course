package ServiceLayer;

import DomainLayer.IProductRepository;
import DomainLayer.Product;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class ProductService {
    private final IProductRepository productRepo;

    public ProductService(IProductRepository productRepo){

        this.productRepo = productRepo;

    }

    public boolean addProduct(Product product) {
        try {
            productRepo.save(product);
            return true;
        } catch (Exception e) {
            System.out.println("ERROR adding product:" + e.getMessage());
            return false;
        }
    }

    public Optional<Product> getProductById(String id) {
        try {
            return productRepo.findById(id);
        } catch (Exception e) {
            System.out.println("ERROR finding product by ID:" + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Product> getProductByName(String name) {
        try {
            return productRepo.findById(name);
        } catch (Exception e) {
            System.out.println("ERROR finding product by Name:" + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Product> getAllProducts() {
        try {
            return productRepo.findAll();
        } catch (Exception e) {
            System.out.println("ERROR getting all products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean removeProduct(String id) {
        try {
            productRepo.deleteById(id);
            return true;
        } catch (Exception e) {
            System.out.println("ERROR deleting product:" + e.getMessage());
            return false;
        }
    }

    public boolean increaseQuantity(String productId, int amount) {
        try {
            Optional<Product> optionalProduct = productRepo.findById(productId);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setQuantity(product.getQuantity() + amount);
                productRepo.save(product);
                return true;
            }
        } catch (Exception e) {
            System.out.println("ERROR increasing quantity: " + e.getMessage());
        }
        return false;
    }

    public boolean decreaseQuantity(String productId, int amount) {
        try {
            Optional<Product> optionalProduct = productRepo.findById(productId);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                if (product.getQuantity() >= amount) {
                    product.setQuantity(product.getQuantity() - amount);
                    productRepo.save(product);
                    return true;
                } else {
                    System.out.println("Not enough stock");
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR reducing quantity: " + e.getMessage());
        }
        return false;
    }

    public boolean updateRating(String productId, int newRating) {
        try {
            if (newRating < 0 || newRating > 5) {
                System.out.println("Rating must be between 0 and 5");
                return false;
            }

            Optional<Product> optionalProduct = productRepo.findById(productId);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setRating(newRating);
                productRepo.save(product);
                return true;
            }
        } catch (Exception e) {
            System.out.println("ERROR updating rating: " + e.getMessage());
        }

        return false;
    }

    public List<Product> getProductByCategory(String category) {
        List<Product> products = productRepo.findAll();
        List<Product> producByCategory = new ArrayList<Product>();
        try {
            for (Product product : products) {
                if (product.getCategory().equals(category)) {
                    producByCategory.add(product);
                }
            }
            return producByCategory;
        } catch (Exception e) {
            System.out.println("ERROR finding product by Name:" + e.getMessage());
            return null;
        }
    }
}
