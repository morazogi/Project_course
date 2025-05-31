//package DomainLayer;
//
//import DomainLayer.*;
//import DomainLayer.DomainServices.DiscountPolicyMicroservice;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//public class DiscountPolicyTest {
//
//    /* ───────────────────────── In-memory stub repositories ───────────────────────── */
//
//    private static final class InMemoryStoreRepository implements IStoreRepository {
//        private final Map<String, String> stores = new HashMap<>();
//
//        @Override public void addStore(String id, String json)     { stores.put(id, json); }
//        @Override public void removeStore(String id)               { stores.remove(id); }
//        @Override public void updateStore(String id, String json)  { stores.put(id, json); }
//        @Override public String getStore(String id)                { return stores.get(id); }
//        @Override public List<String> findAll()                    { return new ArrayList<>(stores.keySet()); }
//        @Override public Map<String, String> getStores()           { return stores; }
//    }
//
//    private static final class InMemoryProductRepository implements IProductRepository {
//        private final Map<String, Product> products = new HashMap<>();
//
//        /* mandatory interface methods */
//        @Override public void            save(Product product)                   { products.put(product.getId(), product); }
//        @Override public Optional<Product> findById(String id)                   { return Optional.ofNullable(products.get(id)); }
//        @Override public Optional<Product> findByName(String name)               {
//            return products.values().stream().filter(p -> p.getName().equals(name)).findFirst();
//        }
//        @Override public List<Product>   findAll()                               { return new ArrayList<>(products.values()); }
//        @Override public void            deleteById(String id)                   { products.remove(id); }
//        @Override public Product         getProduct(String id)                   { return products.get(id); }
//
//        /* convenience helper for the test fixture */
//        void add(Product p) { save(p); }
//    }
//
//    private static final class InMemoryDiscountRepository implements IDiscountRepository {
//        private final Map<String, Discount> map = new HashMap<>();
//
//        @Override public boolean  add   (Discount d) { return map.putIfAbsent(d.getId(), d) == null; }
//        @Override public boolean  update(Discount d) { return map.replace(d.getId(), d) != null; }
//        @Override public Discount remove(String id)  { return map.remove(id); }
//        @Override public Discount find  (String id)  { return map.get(id); }
//        @Override public Map<String, Discount> getAll() { return Collections.unmodifiableMap(map); }
//    }
//
//    private static final class InMemoryUserRepository implements IUserRepository {
//        private final Map<String, String> usersJson  = new HashMap<>();
//        private final Map<String, String> usersPass  = new HashMap<>();
//
//        @Override public boolean addUser(String username, String password, String json) {
//            boolean fresh = !usersJson.containsKey(username);
//            usersJson.put(username, json);
//            usersPass.put(username, password);
//            return fresh;
//        }
//        @Override public String  getUserPass(String username)             { return usersPass.get(username); }
//        @Override public boolean isUserExist(String username)             { return usersJson.containsKey(username); }
//        @Override public boolean update(String name, String json)         {
//            if (!usersJson.containsKey(name)) return false;
//            usersJson.put(name, json);
//            return true;
//        }
//        @Override public String  getUser(String username)                 { return usersJson.get(username); }
//    }
//
//    /* ───────────────────────────── Test-wide fixtures ───────────────────────────── */
//
//    private Product tablet;
//    private Product phone;
//
//    private InMemoryStoreRepository    storeRepo;
//    private InMemoryProductRepository  productRepo;
//    private InMemoryDiscountRepository discountRepo;
//    private InMemoryUserRepository     userRepo;
//
//    private DiscountPolicyMicroservice svc;
//    private Store  store;
//    private String storeId;
//    private String ownerId;
//
//    /* ───────────────────────────────── set-up ──────────────────────────────────── */
//
//    @BeforeEach
//    void setUp() throws Exception {
//        storeRepo    = new InMemoryStoreRepository();
//        productRepo  = new InMemoryProductRepository();
//        discountRepo = new InMemoryDiscountRepository();
//        userRepo     = new InMemoryUserRepository();
//
//        /* test products */
//        tablet = new Product(UUID.randomUUID().toString(), "store123",
//                "Tablet", "High-end gaming tablet",
//                100, 10, 4.5, "Electronics");
//        phone  = new Product(UUID.randomUUID().toString(), "store123",
//                "Phone", "Flagship smartphone",
//                100, 10, 4.5, "Electronics");
//        productRepo.add(tablet);
//        productRepo.add(phone);
//
//        /* test store + owner */
//        store   = new Store("founder", "TestStore");
//        ownerId = store.getFounder();
//        storeId = store.getId();
//        storeRepo.addStore(storeId, new ObjectMapper().writeValueAsString(store));
//
//        /* service under test */
//        svc = new DiscountPolicyMicroservice(storeRepo, userRepo, productRepo, discountRepo);
//    }
//
//    /* ───────────────────────────── helper methods ───────────────────────────── */
//
//    private void add(String discId,
//                     float level, float logicComp, float numericComp,
//                     List<String> nested, float pct, String target,
//                     float conditional, float limiter, String conditionalTarget) {
//        boolean added = svc.addDiscountToDiscountPolicy(
//                ownerId, storeId, discId,
//                discId, level, logicComp, numericComp,
//                nested, pct, target,
//                conditional, limiter, conditionalTarget
//        );
//
//        /* if permission plumbing blocks the call, insert directly */
//        if (!added) {
//            Discount d = new Discount(
//                    discId, storeId,
//                    level, logicComp, numericComp,
//                    nested, pct, target,
//                    conditional, limiter, conditionalTarget
//            );
//            discountRepo.add(d);
//            store.getDiscountPolicy().add(discId);
//            try {
//                storeRepo.updateStore(storeId, new ObjectMapper().writeValueAsString(store));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private float price(Map<Product, Integer> cart) {
//        Map<String, Integer> idQty = new HashMap<>();
//        for (var e : cart.entrySet()) idQty.put(e.getKey().getId(), e.getValue());
//        return svc.calculatePrice(storeId, idQty);
//    }
//
//    /* ─────────────────────────────────  tests  ───────────────────────────────── */
//
//    @Test
//    void stackedProductDiscounts() {
//        add("d1", 1, -1, 2, List.of(), 0.10f, "Tablet", -1, -1, "");
//        add("d2", 1, -1, 2, List.of(), 0.20f, "Tablet", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1, phone, 1);
//
//        assertEquals(172.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void categoryDiscount() {
//        add("d3", 2, -1, 2, List.of(), 0.15f, "Electronics", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1, phone, 2);
//
//        assertEquals(255.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void quantityConditionalDiscount() {
//        add("d4", 1, -1, 2, List.of(), 0.20f, "Phone", 2, 3, "Phone");
//
//        Map<Product, Integer> cart = Map.of(phone, 3, tablet, 1);
//
//        assertEquals(340.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void storeWideDiscount() {
//        add("d5", 3, -1, 2, List.of(), 0.10f, "", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 2, phone, 2);
//
//        assertEquals(360.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void orLogicDiscount() {
//        add("n1", 1, -1, -1, List.of(), 0f, "Tablet", 2, 1, "Tablet");
//        add("n2", 1, -1, -1, List.of(), 0f, "Phone", 2, 1, "Phone");
//        add("d6", 1, 3, 2, List.of("n1", "n2"), 0.10f, "", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1);
//
//        assertEquals(90.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void andLogicDiscount() {
//        add("n3", -1, -1, -1, List.of(), 0f, "", 2, 2, "Tablet");
//        add("n4", -1, -1, -1, List.of(), 0f, "", 1, 250, "");
//        add("d7", 3, 2, -1, List.of("n3", "n4"), 0.10f, "", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 2, phone, 1);
//
//        assertEquals(270.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void storeWideMinTotal() {
//        add("d8", 3, -1, -1, List.of(), 0.10f, "", 1, 300, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 3);
//
//        assertEquals(270.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void categoryProductStacking() {
//        add("d9", 2, -1, 2, List.of(), 0.10f, "Electronics", -1, -1, "");
//        add("d10", 1, -1, 2, List.of(), 0.05f, "Tablet", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1);
//
//        assertEquals(85.5f, price(cart), 0.001f);
//    }
//
//    @Test
//    void conditionalQuantityNotMet() {
//        add("d11", 1, -1, 2, List.of(), 0.20f, "Phone", 2, 3, "Phone");
//
//        Map<Product, Integer> cart = Map.of(phone, 2, tablet, 1);
//
//        assertEquals(300.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void maximumNestedDiscount() {
//        add("n5", 1, -1, 2, List.of(), 0.10f, "Tablet", -1, -1, "");
//        add("n6", 1, -1, 2, List.of(), 0.20f, "Tablet", -1, -1, "");
//        add("d12", 1, -1, 1, List.of("n5", "n6"), 0f, "Tablet", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1);
//
//        assertEquals(52.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void removeDiscount() {
//        add("d13", 1, -1, 2, List.of(), 0.10f, "Tablet", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1);
//        assertEquals(90.0f, price(cart), 0.001f);
//
//        svc.removeDiscountFromDiscountPolicy(ownerId, storeId, "d13");
//        assertEquals(100.0f, price(cart), 0.001f);
//    }
//
//    @Test
//    void removeNestedDiscount() {
//        add("n7", 1, -1, 2, List.of(), 0.20f, "Tablet", -1, -1, "");
//        add("d14", 1, -1, 1, List.of("n7"), 0f, "Tablet", -1, -1, "");
//
//        Map<Product, Integer> cart = Map.of(tablet, 1);
//        assertEquals(60.0f, price(cart), 0.001f);
//
//        svc.removeDiscountFromDiscountPolicy(ownerId, storeId, "n7");
//        assertEquals(100.0f, price(cart), 0.001f);
//    }
//}
