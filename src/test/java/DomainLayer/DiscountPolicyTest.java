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
    private Product p1;
    private Product p2;

    /* ------------ discount IDs ------------ */
    private final String productDiscId   = "disc-prod";
    private final String innerCatDiscId1 = "disc-cat-10";
    private final String innerCatDiscId2 = "disc-cat-20";
    private final String catDiscId       = "disc-cat";
    private final String storeDiscId     = "disc-store";
    private final String qMin3Id         = "disc-qMin3";
    private final String qMax6AndId      = "disc-qMax6";

    @BeforeEach
    void init() {
        service = new DiscountPolicyMicroservice(
                storeRepo, userRepo, productRepo, discountRepo);

        store = new Store("store-1", "owner-1");
        when(storeRepo.getById(store.getId())).thenReturn(store);

        p1 = new Product(store.getId(), "Apple", "", 15f, 100, 0d, "Food");
        p1.setId("p1");
        p2 = new Product(store.getId(), "Headphones", "", 22f, 100, 0d, "Tech");
        p2.setId("p2");
        when(productRepo.getById(p1.getId())).thenReturn(p1);
        when(productRepo.getById(p2.getId())).thenReturn(p2);

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

        Discount qMin3 = new Discount(
                store.getId(), 1, 0, 2,
                List.of(),
                .10f, "Apple",
                2, 3, "Apple");
        qMin3.setId(qMin3Id);
        when(discountRepo.getById(qMin3Id)).thenReturn(qMin3);

        Discount qMax6And = new Discount(
                store.getId(), 1, 2, 2,
                List.of(qMin3Id),
                .15f, "Apple",
                3, 6, "Apple");
        qMax6And.setId(qMax6AndId);
        when(discountRepo.getById(qMax6AndId)).thenReturn(qMax6And);
    }

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
        assertEquals(49f, total);
    }

    @Test
    void calculatePrice_categoryLevelMaximum_takesHighestOfNested() {
        store.setDiscountPolicy(List.of(catDiscId));
        float total = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1, p2.getId(), 1));
        assertEquals(34f, total);
    }

    @Test
    void calculatePrice_storeWideMinPriceCondition_onlyIfThresholdMet() {
        store.setDiscountPolicy(List.of(storeDiscId));

        float below = service.calculatePrice(store.getId(),
                Map.of(p1.getId(), 1));
        assertEquals(15f, below);

        float above = service.calculatePrice(store.getId(),
                Map.of(p1.getId(), 2, p2.getId(), 1));
        assertEquals(52f * 0.85f, above);
    }

    @Test
    void compositeMinAndMaxQuantity_onlyBetweenThreeAndSix() {
        store.setDiscountPolicy(List.of(qMax6AndId));

        float twoApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 2));
        assertEquals(30f, twoApples);

        float fourApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 4));
        assertEquals(4 * 15f * 0.765f, fourApples, 0.001);

        float sixApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 6));
        assertEquals(6 * 15f * 0.765f, sixApples, 0.001);

        float sevenApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 7));
        assertEquals(105f, sevenApples);
    }

    @Test
    void orComposition_allDiscountsApplyWhenAnyConditionTrue() {
        Discount d10 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .10f, "Apple", 2, 2, "Apple");
        d10.setId("d10or");
        when(discountRepo.getById("d10or")).thenReturn(d10);

        Discount d15 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .15f, "Apple", 2, 5, "Apple");
        d15.setId("d15or");
        when(discountRepo.getById("d15or")).thenReturn(d15);

        Discount orGroup = new Discount(store.getId(), 1, 3, 2,
                List.of("d10or", "d15or"), 0f, "", 0,0,"");
        orGroup.setId("or");
        when(discountRepo.getById("or")).thenReturn(orGroup);

        store.setDiscountPolicy(List.of("or"));

        float threeApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 3));
        assertEquals(3 * 15f * 0.9f * 0.85f, threeApples, 0.001);

        float oneApple = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1));
        assertEquals(15f, oneApple);
    }

    @Test
    void xorComposition_allDiscountsApplyOnlyWhenExactlyOneConditionTrue() {
        Discount d10 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .10f, "Apple", 2, 2, "Apple");
        d10.setId("d10");
        when(discountRepo.getById("d10")).thenReturn(d10);

        Discount d15 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .15f, "Apple", 2, 5, "Apple");
        d15.setId("d15");
        when(discountRepo.getById("d15")).thenReturn(d15);

        Discount xorGroup = new Discount(store.getId(), 1, 1, 2,
                List.of("d10", "d15"), 0f, "", 0,0,"");
        xorGroup.setId("xor");
        when(discountRepo.getById("xor")).thenReturn(xorGroup);

        store.setDiscountPolicy(List.of("xor"));

        float threeApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 3));
        assertEquals(3 * 15f * 0.9f * 0.85f, threeApples, 0.001);

        float fiveApples = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 5));
        assertEquals(75f, fiveApples);
    }

    @Test
    void andComposition_allDiscountsApplyWhenAllConditionsTrue() {
        Discount d10 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .10f, "Apple", 2, 1, "Apple");
        d10.setId("and10");
        when(discountRepo.getById("and10")).thenReturn(d10);

        Discount d15 = new Discount(store.getId(), 3, 0, 2,
                List.of(), .15f, "", 1, 20, "");
        d15.setId("and15");
        when(discountRepo.getById("and15")).thenReturn(d15);

        Discount andGroup = new Discount(store.getId(), 1, 2, 2,
                List.of("and10", "and15"), 0f, "", 0,0,"");
        andGroup.setId("and");
        when(discountRepo.getById("and")).thenReturn(andGroup);

        store.setDiscountPolicy(List.of("and"));

        float price = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 2, p2.getId(), 1));
        assertEquals((2 * 15f + 22f) * 0.9f * 0.85f, price, 0.001);
    }

    @Test
    void andComposition_noDiscountWhenAnyConditionFalse() {
        Discount d10 = new Discount(store.getId(), 1, 0, 2,
                List.of(), .10f, "Apple", 2, 5, "Apple");
        d10.setId("andFalse10");
        when(discountRepo.getById("andFalse10")).thenReturn(d10);

        Discount d15 = new Discount(store.getId(), 3, 0, 2,
                List.of(), .15f, "", 1, 100, "");
        d15.setId("andFalse15");
        when(discountRepo.getById("andFalse15")).thenReturn(d15);

        Discount andGroup = new Discount(store.getId(), 1, 2, 2,
                List.of("andFalse10", "andFalse15"), 0f, "", 0,0,"");
        andGroup.setId("andFalse");
        when(discountRepo.getById("andFalse")).thenReturn(andGroup);

        store.setDiscountPolicy(List.of("andFalse"));

        float price = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1));
        assertEquals(15f, price);
    }

    @Test
    void undefinedSingleDiscount_conditionFalse_noDiscount() {
        Discount big = new Discount(store.getId(), 3, 0, 2,
                List.of(), .30f, "", 1, 100, "");
        big.setId("single");
        when(discountRepo.getById("single")).thenReturn(big);

        store.setDiscountPolicy(List.of("single"));

        float price = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1));
        assertEquals(15f, price);
    }

    @Test
    void additiveClamp_totalCannotGoBelowZero() {
        Discount d70 = new Discount(store.getId(), 1, 0, 0,
                List.of(), .70f, "Apple", 0,0,"");
        d70.setId("d70");
        when(discountRepo.getById("d70")).thenReturn(d70);

        Discount d40 = new Discount(store.getId(), 1, 0, 0,
                List.of(), .40f, "Apple", 0,0,"");
        d40.setId("d40");
        when(discountRepo.getById("d40")).thenReturn(d40);

        Discount group = new Discount(store.getId(), 1, 3, 0,
                List.of("d70", "d40"), 0f, "", 0,0,"");
        group.setId("addClamp");
        when(discountRepo.getById("addClamp")).thenReturn(group);

        store.setDiscountPolicy(List.of("addClamp"));

        float price = service.calculatePrice(
                store.getId(), Map.of(p1.getId(), 1));
        assertEquals(0f, price);
    }

}
