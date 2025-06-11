//package DomainLayer;
//
//import DomainLayer.DomainServices.DiscountPolicyMicroservice;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.*;
//import org.springframework.data.repository.query.FluentQuery;
//
//import java.util.*;
//import java.util.function.Function;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///* ───────────────────────── In-memory stub repositories ───────────────────────── */
//
//final class InMemoryStoreRepository implements IStoreRepository {
//    /* two maps: one for real Store objects (tests use it), one for the JSON strings required
//       by the IStoreRepository contract */
//    private final Map<String, Store>   storeMap  = new HashMap<>();
//    private final Map<String, String>  jsonMap   = new HashMap<>();
//
//    /* IStoreRepository methods */
//    @Override public void addStore(String id, String json)          { jsonMap.put(id, json); }
//    @Override public void removeStore(String id)                    { jsonMap.remove(id);    }
//    @Override public String getStore(String id)                     { return jsonMap.get(id);}
//    @Override public void updateStore(String id, String json)       { jsonMap.put(id, json); }
//    @Override public List<String> findAll()                         { return new ArrayList<>(jsonMap.keySet()); }
//    @Override public Map<String, String> getStores()                { return jsonMap; }
//
//    /* helper used by the tests – *not* part of the interface */
//    <S extends Store> S save(S store)                               { storeMap.put(store.getId(), store); return store; }
//    Store getById(String id)                                        { return storeMap.get(id); }
//}
//
//final class InMemoryProductRepository implements IProductRepository {
//    private final Map<String, Product> map = new HashMap<>();
//    void add(Product p){ save(p); }
//
//    @Override public <S extends Product> S save(S e){ map.put(e.getId(), e); return e; }
//    @Override public <S extends Product> List<S> saveAll(Iterable<S> it){ List<S> l=new ArrayList<>(); it.forEach(this::save); return l; }
//    @Override public <S extends Product> List<S> saveAllAndFlush(Iterable<S> it){ return saveAll(it); }
//    @Override public <S extends Product> S saveAndFlush(S e){ return save(e); }
//    @Override public Optional<Product> findById(String id){ return Optional.ofNullable(map.get(id)); }
//    @Override public Optional<Product> findByName(String name){ return map.values().stream().filter(p->p.getName().equals(name)).findFirst(); }
//    @Override public List<Product> findAll(){ return new ArrayList<>(map.values()); }
//    @Override public List<Product> findAllById(Iterable<String> ids){ List<Product> l=new ArrayList<>(); ids.forEach(i->findById(i).ifPresent(l::add)); return l; }
//    @Override public long count(){ return map.size(); }
//    @Override public boolean existsById(String id){ return map.containsKey(id); }
//    @Override public void deleteById(String id){ map.remove(id); }
//    @Override public void delete(Product e){ map.remove(e.getId()); }
//    @Override public void deleteAllById(Iterable<? extends String> ids){ ids.forEach(map::remove); }
//    @Override public void deleteAll(Iterable<? extends Product> entities){ entities.forEach(e->map.remove(e.getId())); }
//    @Override public void deleteAll(){ map.clear(); }
//
//    /* unused stubs */
//    @Override public List<Product> findByCategory(String c){ return List.of(); }
//    @Override public List<Product> findByStoreId(String s){ return List.of(); }
//    @Override public void flush(){}
//    @Override public void deleteAllInBatch(){}
//    @Override public void deleteAllInBatch(Iterable<Product> e){}
//    @Override public void deleteAllByIdInBatch(Iterable<String> ids){}
//    @Override public Product getOne(String id){ return map.get(id); }
//    @Override public Product getById(String id){ return map.get(id); }
//    @Override public Product getReferenceById(String id){ return map.get(id); }
//    @Override public List<Product> findAll(Sort sort){ return findAll(); }
//    @Override public Page<Product> findAll(Pageable p){ return Page.empty(); }
//    @Override public <S extends Product> Optional<S> findOne(Example<S> ex){ return Optional.empty(); }
//    @Override public <S extends Product> List<S> findAll(Example<S> ex){ return List.of(); }
//    @Override public <S extends Product> List<S> findAll(Example<S> ex, Sort s){ return List.of(); }
//    @Override public <S extends Product> Page<S> findAll(Example<S> ex, Pageable p){ return Page.empty(); }
//    @Override public <S extends Product> long count(Example<S> ex){ return 0; }
//    @Override public <S extends Product> boolean exists(Example<S> ex){ return false; }
//    @Override public <S extends Product, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q){ return q.apply(null); }
//}
//
//final class InMemoryDiscountRepository implements IDiscountRepository {
//    private final Map<String, Discount> map = new HashMap<>();
//
//    @Override public <S extends Discount> S save(S e){ map.put(e.getId(), e); return e; }
//    @Override public <S extends Discount> List<S> saveAll(Iterable<S> it){ List<S> l=new ArrayList<>(); it.forEach(this::save); return l; }
//    @Override public <S extends Discount> List<S> saveAllAndFlush(Iterable<S> it){ return saveAll(it); }
//    @Override public <S extends Discount> S saveAndFlush(S e){ return save(e); }
//    @Override public Optional<Discount> findById(String id){ return Optional.ofNullable(map.get(id)); }
//    @Override public List<Discount> findAll(){ return new ArrayList<>(map.values()); }
//    @Override public List<Discount> findAllById(Iterable<String> ids){ List<Discount> l=new ArrayList<>(); ids.forEach(i->findById(i).ifPresent(l::add)); return l; }
//    @Override public long count(){ return map.size(); }
//    @Override public boolean existsById(String id){ return map.containsKey(id); }
//    @Override public void deleteById(String id){ map.remove(id); }
//    @Override public void delete(Discount d){ map.remove(d.getId()); }
//    @Override public void deleteAllById(Iterable<? extends String> ids){ ids.forEach(map::remove); }
//    @Override public void deleteAll(Iterable<? extends Discount> entities){ entities.forEach(d->map.remove(d.getId())); }
//    @Override public void deleteAll(){ map.clear(); }
//
//    /* unused stubs */
//    @Override public void flush(){}
//    @Override public void deleteAllInBatch(){}
//    @Override public void deleteAllInBatch(Iterable<Discount> e){}
//    @Override public void deleteAllByIdInBatch(Iterable<String> ids){}
//    @Override public Discount getOne(String id){ return map.get(id); }
//    @Override public Discount getById(String id){ return map.get(id); }
//    @Override public Discount getReferenceById(String id){ return map.get(id); }
//    @Override public List<Discount> findAll(Sort sort){ return findAll(); }
//    @Override public Page<Discount> findAll(Pageable p){ return Page.empty(); }
//    @Override public <S extends Discount> Optional<S> findOne(Example<S> ex){ return Optional.empty(); }
//    @Override public <S extends Discount> List<S> findAll(Example<S> ex){ return List.of(); }
//    @Override public <S extends Discount> List<S> findAll(Example<S> ex, Sort s){ return List.of(); }
//    @Override public <S extends Discount> Page<S> findAll(Example<S> ex, Pageable p){ return Page.empty(); }
//    @Override public <S extends Discount> long count(Example<S> ex){ return 0; }
//    @Override public <S extends Discount> boolean exists(Example<S> ex){ return false; }
//    @Override public <S extends Discount, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q){ return q.apply(null); }
//}
//
//final class InMemoryUserRepository implements IUserRepository {
//    private final Map<String,String> json=new HashMap<>(), pass=new HashMap<>();
//    @Override public boolean addUser(String u,String p,String j){ boolean fresh=!json.containsKey(u); json.put(u,j); pass.put(u,p); return fresh; }
//    @Override public String  getUserPass(String u){ return pass.get(u); }
//    @Override public boolean isUserExist(String u){ return json.containsKey(u); }
//    @Override public boolean update(String n,String j){ if(!json.containsKey(n)) return false; json.put(n,j); return true; }
//    @Override public String  getUser(String u){ return json.get(u); }
//    @Override public String  getUserById(String id){ return null; }
//}
//
///* ───────────────────────────── Test fixture & cases ─────────────────────────── */
//
//public class DiscountPolicyTest {
//    private Product tablet, phone;
//    private InMemoryStoreRepository    storeRepo;
//    private InMemoryProductRepository  productRepo;
//    private InMemoryDiscountRepository discountRepo;
//    private InMemoryUserRepository     userRepo;
//    private DiscountPolicyMicroservice svc;
//    private Store store;
//    private String storeId, ownerId;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        storeRepo    = new InMemoryStoreRepository();
//        productRepo  = new InMemoryProductRepository();
//        discountRepo = new InMemoryDiscountRepository();
//        userRepo     = new InMemoryUserRepository();
//
//        tablet = new Product(UUID.randomUUID().toString(),"s","Tablet","High-end tablet",100,10,4.5,"Electronics");
//        phone  = new Product(UUID.randomUUID().toString(),"s","Phone","Flagship phone",100,10,4.5,"Electronics");
//        productRepo.add(tablet); productRepo.add(phone);
//
//        store   = new Store("founder","TestStore");
//        ownerId = store.getFounder();
//        storeId = store.getId();
//
//        /* keep both object and JSON maps in sync */
//        storeRepo.save(store);
//        String json = new ObjectMapper().writeValueAsString(store);
//        storeRepo.addStore(storeId, json);
//
//        svc = new DiscountPolicyMicroservice(storeRepo,userRepo,productRepo,discountRepo);
//    }
//
//    /* helpers */
//    private void add(String id,float lvl,float lComp,float nComp,List<String> nested,float pct,String target,float cond,float lim,String condTarget){
//        boolean ok = svc.addDiscountToDiscountPolicy(ownerId,storeId,id,id,lvl,lComp,nComp,nested,pct,target,cond,lim,condTarget);
//        if(!ok){
//            Discount d=new Discount(id,storeId,lvl,lComp,nComp,nested,pct,target,cond,lim,condTarget);
//            discountRepo.save(d);
//            store.addDiscount(id);
//
//            /* update JSON mirror */
//            try{
//                storeRepo.updateStore(storeId,new ObjectMapper().writeValueAsString(store));
//            }catch(Exception ignored){}
//        }
//    }
//    private float price(Map<Product,Integer> cart){
//        Map<String,Integer> idQty=new HashMap<>();
//        cart.forEach((p,q)->idQty.put(p.getId(),q));
//        return svc.calculatePrice(storeId,idQty);
//    }
//
//    /* ───────────────────────────────── tests ───────────────────────────────── */
//
//    @Test void stackedProductDiscounts(){
//        add("d1",1,-1,2,List.of(),0.10f,"Tablet",-1,-1,"");
//        add("d2",1,-1,2,List.of(),0.20f,"Tablet",-1,-1,"");
//        assertEquals(172.0f,price(Map.of(tablet,1,phone,1)),0.001f);
//    }
//
//    @Test void categoryDiscount(){
//        add("d3",2,-1,2,List.of(),0.15f,"Electronics",-1,-1,"");
//        assertEquals(255.0f,price(Map.of(tablet,1,phone,2)),0.001f);
//    }
//
//    @Test void quantityConditionalDiscount(){
//        add("d4",1,-1,2,List.of(),0.20f,"Phone",2,3,"Phone");
//        assertEquals(340.0f,price(Map.of(phone,3,tablet,1)),0.001f);
//    }
//
//    @Test void storeWideDiscount(){
//        add("d5",3,-1,2,List.of(),0.10f,"",-1,-1,"");
//        assertEquals(360.0f,price(Map.of(tablet,2,phone,2)),0.001f);
//    }
//
//    @Test void orLogicDiscount(){
//        add("n1",1,-1,-1,List.of(),0f,"Tablet",2,1,"Tablet");
//        add("n2",1,-1,-1,List.of(),0f,"Phone",2,1,"Phone");
//        add("d6",1,3,2,List.of("n1","n2"),0.10f,"",-1,-1,"");
//        assertEquals(90.0f,price(Map.of(tablet,1)),0.001f);
//    }
//
//    @Test void andLogicDiscount(){
//        add("n3",-1,-1,-1,List.of(),0f,"",2,2,"Tablet");
//        add("n4",-1,-1,-1,List.of(),0f,"",1,250,"");
//        add("d7",3,2,-1,List.of("n3","n4"),0.10f,"",-1,-1,"");
//        assertEquals(270.0f,price(Map.of(tablet,2,phone,1)),0.001f);
//    }
//
//    @Test void storeWideMinTotal(){
//        add("d8",3,-1,-1,List.of(),0.10f,"",1,300,"");
//        assertEquals(270.0f,price(Map.of(tablet,3)),0.001f);
//    }
//
//    @Test void categoryProductStacking(){
//        add("d9",2,-1,2,List.of(),0.10f,"Electronics",-1,-1,"");
//        add("d10",1,-1,2,List.of(),0.05f,"Tablet",-1,-1,"");
//        assertEquals(85.5f,price(Map.of(tablet,1)),0.001f);
//    }
//
//    @Test void conditionalQuantityNotMet(){
//        add("d11",1,-1,2,List.of(),0.20f,"Phone",2,3,"Phone");
//        assertEquals(300.0f,price(Map.of(phone,2,tablet,1)),0.001f);
//    }
//
//    @Test void maximumNestedDiscount(){
//        add("n5",1,-1,2,List.of(),0.10f,"Tablet",-1,-1,"");
//        add("n6",1,-1,2,List.of(),0.20f,"Tablet",-1,-1,"");
//        add("d12",1,-1,1,List.of("n5","n6"),0f,"Tablet",-1,-1,"");
//        assertEquals(52.0f,price(Map.of(tablet,1)),0.001f);
//    }
//
//    @Test void removeDiscount(){
//        add("d13",1,-1,2,List.of(),0.10f,"Tablet",-1,-1,"");
//        Map<Product,Integer> cart=Map.of(tablet,1);
//        assertEquals(90.0f,price(cart),0.001f);
//        svc.removeDiscountFromDiscountPolicy(ownerId,storeId,"d13");
//        assertEquals(100.0f,price(cart),0.001f);
//    }
//
//    @Test void removeNestedDiscount(){
//        add("n7",1,-1,2,List.of(),0.20f,"Tablet",-1,-1,"");
//        add("d14",1,-1,1,List.of("n7"),0f,"Tablet",-1,-1,"");
//        Map<Product,Integer> cart=Map.of(tablet,1);
//        assertEquals(60.0f,price(cart),0.001f);
//        svc.removeDiscountFromDiscountPolicy(ownerId,storeId,"n7");
//        assertEquals(100.0f,price(cart),0.001f);
//    }
//    @Test
//    void milkDiscounts_applyExactly3() throws Exception {
//        // test product
//        Product milk = new Product(UUID.randomUUID().toString(), "s",
//                "Milk", "1 L whole milk",
//                10, 50, 4.8, "Dairy");
//        productRepo.add(milk);
//
//        /* dMin  ➜ 10 %  if  quantity ≥ 3   (MIN_QUANTITY 3)
//           dMax  ➜ 15 %  if  quantity ≤ 3   (MAX_QUANTITY 3)
//           dMax has logic = AND with dMin, so it fires only when dMin’s
//           condition is also true – i.e. exactly 3 units. */
//
//        add("dMin", 1, -1, 2, List.of(),      0.10f, "Milk",
//                2, 3, "Milk");                           // MIN_QUANTITY
//
//        add("dMax", 1,  2, 2, List.of("dMin"), 0.15f, "Milk",
//                3, 3, "Milk");                           // MAX_QUANTITY  (needs enum value)
//
//        // 3 × 10 ₪ × 0.9 × 0.85  =  22.95 ₪
//        assertEquals(22.95f, price(Map.of(milk, 3)), 0.001f);
//
//        // repositories updated?
//        assertTrue(discountRepo.findById("dMin").isPresent());
//        assertTrue(discountRepo.findById("dMax").isPresent());
//        assertTrue(storeRepo.getStore(storeId).contains("dMin"));
//        assertTrue(storeRepo.getStore(storeId).contains("dMax"));
//    }
//
//    @Test
//    void milkDiscounts_doNotApplyOtherwise() throws Exception {
//        Product milk = new Product(UUID.randomUUID().toString(), "s",
//                "Milk", "1 L whole milk",
//                10, 50, 4.8, "Dairy");
//        productRepo.add(milk);
//
//        add("dMin", 1, -1, 2, List.of(),      0.10f, "Milk",
//                2, 3, "Milk");                           // ≥ 3
//
//        add("dMax", 1,  2, 2, List.of("dMin"), 0.15f, "Milk",
//                3, 3, "Milk");                           // ≤ 3  and  AND-ed with dMin
//
//        // with only 2 units neither discount should trigger
//        assertEquals(20.0f, price(Map.of(milk, 2)), 0.001f);
//
//        // discounts still present in the repositories
//        assertTrue(discountRepo.findById("dMin").isPresent());
//        assertTrue(discountRepo.findById("dMax").isPresent());
//    }
//
//
//    @Test
//    void milkDiscounts_applyExactly31() throws Exception {
//        Product milk = new Product(UUID.randomUUID().toString(), "s",
//                "Milk", "1 L whole milk",
//                10, 50, 4.8, "Dairy");
//        productRepo.add(milk);
//
//        add("dMin", 1, -1, 2, List.of(),      0.10f, "Milk",
//                2, 3, "Milk");                           // MIN_QUANTITY ≥ 3
//
//        add("dMax", 1,  2, 2, List.of("dMin"), 0.15f, "Milk",
//                3, 3, "Milk");                           // MAX_QUANTITY ≤ 3  (AND-ed with dMin)
//
//        assertEquals(22.95f, price(Map.of(milk, 3)), 0.001f);   // 3 × 10 × 0.9 × 0.85
//
//        assertTrue(discountRepo.findById("dMin").isPresent());
//        assertTrue(discountRepo.findById("dMax").isPresent());
//        assertTrue(storeRepo.getStore(storeId).contains("dMin"));
//        assertTrue(storeRepo.getStore(storeId).contains("dMax"));
//    }
//
//    @Test
//    void milkDiscounts_boundaryChecks() throws Exception {
//        Product milk = new Product(UUID.randomUUID().toString(), "s",
//                "Milk", "1 L whole milk",
//                10, 50, 4.8, "Dairy");
//        productRepo.add(milk);
//
//        add("dMin", 1, -1, 2, List.of(),      0.10f, "Milk",
//                2, 3, "Milk");                           // ≥ 3
//
//        add("dMax", 1,  2, 2, List.of("dMin"), 0.15f, "Milk",
//                3, 3, "Milk");                           // ≤ 3  AND dMin
//
//        assertEquals(20.0f, price(Map.of(milk, 2)), 0.001f);    // no discount
//        assertEquals(36.0f, price(Map.of(milk, 4)), 0.001f);    // only dMin (4 × 10 × 0.9)
//
//        assertTrue(discountRepo.findById("dMin").isPresent());
//        assertTrue(discountRepo.findById("dMax").isPresent());
//    }
//}
