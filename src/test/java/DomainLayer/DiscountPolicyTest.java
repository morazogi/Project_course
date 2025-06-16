package DomainLayer;

import DomainLayer.DomainServices.DiscountPolicyMicroservice;
import InfrastructureLayer.DiscountRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // <-- suppress “unused stubbing” noise
class DiscountPolicyTest {

    /* ------------ mocked repositories ------------ */
    @Mock StoreRepository    storeRepo;
    @Mock UserRepository     userRepo;
    @Mock ProductRepository  productRepo;
    @Mock DiscountRepository discountRepo;

    private DiscountPolicyMicroservice service;

    /* ------------ domain helpers ------------ */
    private Store   store;
    private Product p1;      // Apple
    private Product p2;      // Headphones

    /* ------------ discount IDs (strings) ------------ */
    private final String productDiscId   = "disc-prod";
    private final String innerCatDiscId1 = "disc-cat-10";
    private final String innerCatDiscId2 = "disc-cat-20";
    private final String catDiscId       = "disc-cat";
    private final String storeDiscId     = "disc-store";

    @BeforeEach
    void init() {
        service = new DiscountPolicyMicroservice(
                storeRepo, userRepo, productRepo, discountRepo);

        /* ---------- store ---------- */
        store = new Store("store-1", "owner-1");
        when(storeRepo.getById(store.getId())).thenReturn(store);

        /* ---------- products (use the real ctor) ---------- */
        // args: storeId, name, description, price, quantity, rating, category
        p1 = new Product(store.getId(), "Apple", "", 15f, 100, 0d, "Food");
        p1.setId("p1");

        p2 = new Product(store.getId(), "Headphones", "", 22f, 100, 0d, "Tech");
        p2.setId("p2");

        when(productRepo.getById(p1.getId())).thenReturn(p1);
        when(productRepo.getById(p2.getId())).thenReturn(p2);

        /* ---------- discounts ---------- */
        // product-level 10 % multiplication on “Apple”
        Discount productDisc = new Discount(
                store.getId(), 1, 0, 2,           // PRODUCT / UNDEF / MULTIPLICATION
                List.of(),
                .10f, "Apple",
                0, 0, ""                          // no condition
        );
        productDisc.setId(productDiscId);
        when(discountRepo.getById(productDiscId)).thenReturn(productDisc);

        // two inner discounts 10 % and 20 %
        Discount inner10 = new Discount(store.getId(), 1,0,0,
                List.of(), .10f, "", 0,0,"");
        inner10.setId(innerCatDiscId1);
        Discount inner20 = new Discount(store.getId(), 1,0,0,
                List.of(), .20f, "", 0,0,"");
        inner20.setId(innerCatDiscId2);
        when(discountRepo.getById(innerCatDiscId1)).thenReturn(inner10);
        when(discountRepo.getById(innerCatDiscId2)).thenReturn(inner20);

        // category discount – maximum of nested
        Discount catDisc = new Discount(
                store.getId(), 2, 0, 1,
                List.of(innerCatDiscId1, innerCatDiscId2),
                .05f, "Food",
                0, 0, ""
        );
        catDisc.setId(catDiscId);
        when(discountRepo.getById(catDiscId)).thenReturn(catDisc);

        // store-wide 15 % if total ≥ 50
        Discount storeDisc = new Discount(
                store.getId(), 3, 0, 2,
                List.of(),
                .15f, "",
                1, 50, ""          // MIN_PRICE 50
        );
        storeDisc.setId(storeDiscId);
        when(discountRepo.getById(storeDiscId)).thenReturn(storeDisc);
    }

    /* -------------------------------------------------------------
                               TESTS
       ------------------------------------------------------------- */

    @Test
    void calculatePrice_noDiscounts_returnsFullPrice() {
        store.setDiscountPolicy(List.of());                    // no discounts
        float total = service.calculatePrice(
                store.getId(),
                Map.of(p1.getId(), 1, p2.getId(), 1)
        );
        assertEquals(37f, total);                              // 15 + 22
    }

    @Test
    void calculatePrice_productLevelMultiplication_appliesOnlyToThatProduct() {
        store.setDiscountPolicy(List.of(productDiscId));
        float total = service.calculatePrice(
                store.getId(),
                Map.of(p1.getId(), 2, p2.getId(), 1)           // 2 Apples, 1 Headphones
        );
        // Apples: 15 € * 0.9 * 2 = 27
        // Headphones: 22 €
        assertEquals(49f, total);
    }

    @Test
    void calculatePrice_categoryLevelMaximum_takesHighestOfNested() {
        store.setDiscountPolicy(List.of(catDiscId));
        float total = service.calculatePrice(
                store.getId(),
                Map.of(p1.getId(), 1, p2.getId(), 1)
        );
        // Apple (Food): 15 – 20 % → 12
        // Headphones   : 22
        assertEquals(34f, total);   // 12 + 22
    }

    @Test
    void calculatePrice_storeWideMinPriceCondition_onlyIfThresholdMet() {
        store.setDiscountPolicy(List.of(storeDiscId));

        /* ---- below the 50 € threshold: no discount ---- */
        float below = service.calculatePrice(
                store.getId(),
                Map.of(p1.getId(), 1)              // 15 €
        );
        assertEquals(15f, below);

        /* ---- above the threshold: 15 % off entire cart ---- */
        float above = service.calculatePrice(
                store.getId(),
                Map.of(p1.getId(), 2, p2.getId(), 1)  // 15*2 + 22 = 52 €
        );
        assertEquals(52f * 0.85f, above);
    }
}
