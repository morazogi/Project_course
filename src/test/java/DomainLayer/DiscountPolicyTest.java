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
@MockitoSettings(strictness = Strictness.LENIENT)
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

    /* ------------ discount IDs ------------ */
    private final String productDiscId   = "disc-prod";
    private final String innerCatDiscId1 = "disc-cat-10";
    private final String innerCatDiscId2 = "disc-cat-20";
    private final String catDiscId       = "disc-cat";
    private final String storeDiscId     = "disc-store";

    /* --- NEW composite-condition IDs --- */
    private final String qMin3Id         = "disc-qMin3";   // ≥3 Apples  → 10 %
    private final String qMax6AndId      = "disc-qMax6";   // AND with qMin3, ≤6 → 15 %

    @BeforeEach
    void init() {
        service = new DiscountPolicyMicroservice(
                storeRepo, userRepo, productRepo, discountRepo);

        /* ---------- store ---------- */
        store = new Store("store-1", "owner-1");
        when(storeRepo.getById(store.getId())).thenReturn(store);

        /* ---------- products ---------- */
        p1 = new Product(store.getId(), "Apple", "", 15f, 100, 0d, "Food");
        p1.setId("p1");
        p2 = new Product(store.getId(), "Headphones", "", 22f, 100, 0d, "Tech");
        p2.setId("p2");
        when(productRepo.getById(p1.getId())).thenReturn(p1);
        when(productRepo.getById(p2.getId())).thenReturn(p2);

        /* ---------- existing discounts (from original file) ---------- */
        Discount productDisc = new Discount(
                store.getId(), 1, 0, 2,
                List.of(), .10f, "Apple",
                0, 0, "");
        productDisc.setId(productDiscId);
        when(discountRepo.getById(productDiscId)).thenReturn(productDisc);

        Discount inner10 = new Discount(store.getId(), 1,0,0,
                List.of(), .10f, "", 0,0,"");
        inner10.setId(innerCatDiscId1);
        Discount inner20 = new Discount(store.getId(), 1,0,0,
                List.of(), .20f, "", 0,0,"");
        inner20.setId(innerCatDiscId2);
        when(discountRepo.getById(innerCatDiscId1)).thenReturn(inner10);
        when(discountRepo.getById(innerCatDiscId2)).thenReturn(inner20);

        Discount catDisc = new Discount(
                store.getId(), 2, 0, 1,
                List.of(innerCatDiscId1, innerCatDiscId2),
                .05f, "Food",
                0, 0, "");
        catDisc.setId(catDiscId);
        when(discountRepo.getById(catDiscId)).thenReturn(catDisc);

        Discount storeDisc = new Discount(
                store.getId(), 3, 0, 2,
                List.of(), .15f, "",
                1, 50, "");
        storeDisc.setId(storeDiscId);
        when(discountRepo.getById(storeDiscId)).thenReturn(storeDisc);

        /* ---------- NEW composite-condition discounts ---------- */

        // ❶ first: ≥3 Apples → 10 %  (MIN_QUANTITY)
        Discount qMin3 = new Discount(
                store.getId(), 1, 0, 2,              // PRODUCT / UNDEF / MULTIPLICATION
                List.of(),
                .10f, "Apple",
                2, 3, "Apple");                      // MIN_QUANTITY (code 2) , limiter 3
        qMin3.setId(qMin3Id);
        when(discountRepo.getById(qMin3Id)).thenReturn(qMin3);

        // ❷ second: ≤6 Apples  AND  must satisfy qMin3
        Discount qMax6And = new Discount(
                store.getId(), 1, 2, 2,              // PRODUCT / AND / MULTIPLICATION
                List.of(qMin3Id),
                .15f, "Apple",
                3, 6, "Apple");                      // MAX_QUANTITY (code 3) , limiter 6
        qMax6And.setId(qMax6AndId);
        when(discountRepo.getById(qMax6AndId)).thenReturn(qMax6And);
    }

    /* ------------------------------------------------------------------
                                 BASE TESTS
       ------------------------------------------------------------------ */
    @Test
    void calculatePrice_noDiscounts_returnsFullPrice() {
        store.setDiscountPolicy(List.of());
        float total = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1, p2.getId(), 1));
        assertEquals(37f, total);
    }

    @Test
    void calculatePrice_productLevelMultiplication_appliesOnlyToThatProduct() {
        store.setDiscountPolicy(List.of(productDiscId));
        float total = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 2, p2.getId(), 1));
        assertEquals(49f, total);           // (15*0.9*2) + 22
    }

    @Test
    void calculatePrice_categoryLevelMaximum_takesHighestOfNested() {
        store.setDiscountPolicy(List.of(catDiscId));
        float total = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1, p2.getId(), 1));
        assertEquals(34f, total);           // (15*0.8) + 22
    }

    @Test
    void calculatePrice_storeWideMinPriceCondition_onlyIfThresholdMet() {
        store.setDiscountPolicy(List.of(storeDiscId));

        float below = service.calculatePrice(store.getId(),
                Map.of(p1.getId(), 1));      // 15 €
        assertEquals(15f, below);

        float above = service.calculatePrice(store.getId(),
                Map.of(p1.getId(), 2, p2.getId(), 1)); // 52 €
        assertEquals(52f * 0.85f, above);
    }

    /* ------------------------------------------------------------------
                     NEW COMBINATION TEST  –  (3 ≤ qty ≤ 6)
       ------------------------------------------------------------------ */
    @Test
    void compositeMinAndMaxQuantity_onlyBetweenThreeAndSix() {
        store.setDiscountPolicy(List.of(qMax6AndId));     // top-level discount

        /* qty = 2  →  NO discount */
        float twoApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 2));
        assertEquals(30f, twoApples);                     // 2 × 15

        /* qty = 4  →  BOTH discounts (0.90 × 0.85 = 0.765) */
        float fourApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 4));
        assertEquals(4 * 15f * 0.765f, fourApples, 0.001);

        /* qty = 6  →  BOTH discounts still valid */
        float sixApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 6));
        assertEquals(6 * 15f * 0.765f, sixApples, 0.001);

        /* qty = 7  →  exceeds max, so NO discount applied */
        float sevenApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 7));
        assertEquals(105f, sevenApples);
    }
}
