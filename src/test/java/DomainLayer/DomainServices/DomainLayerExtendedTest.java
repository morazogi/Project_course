/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DiscountPolicyExtendedTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import DomainLayer.Discount;
import DomainLayer.DomainServices.DiscountPolicyMicroservice;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscountPolicyExtendedTest {

    /* ---------- repositories (mocks) ---------- */
    @Mock StoreRepository    storeRepo;
    @Mock UserRepository     userRepo;
    @Mock ProductRepository  productRepo;
    @Mock DiscountRepository discountRepo;

    private DiscountPolicyMicroservice svc;
    private Store store;

    @BeforeEach
    void init() {
        svc   = new DiscountPolicyMicroservice(storeRepo, userRepo, productRepo, discountRepo);
        store = new Store("S-1", "founder");
        when(storeRepo.getById(store.getId())).thenReturn(store);

        /* minimal product so calculatePrice() can work */
        Product apple = new Product(store.getId(), "Apple", "", 10f, 50, 0d, "Food");
        apple.setId("p-apple");
        when(productRepo.getById(apple.getId())).thenReturn(apple);
    }

    /* ======================================================================
       1) addDiscountToDiscountPolicy
       ====================================================================== */

    @Test
    void addDiscount_noPermission_returnsFalseAndSavesNothing() {
        Store spyStore = spy(store);
        when(storeRepo.getById(spyStore.getId())).thenReturn(spyStore);
        doReturn(false).when(spyStore).userIsOwner("intruder");
        doReturn(false).when(spyStore).userIsManager("intruder");

        boolean ok = svc.addDiscountToDiscountPolicy(
                "intruder", spyStore.getId(), "",
                1, 0, 0,
                List.of(), 0.05f, "Apple", 0, 0, "");

        assertFalse(ok);
        verify(discountRepo, never()).save(any());
        verify(storeRepo, never()).update(any());
    }

    @Test
    void addDiscount_saveFails_returnsFalseAndStoreUnaffected() {
        when(discountRepo.save(any())).thenReturn(null);  // simulate DB failure

        boolean ok = svc.addDiscountToDiscountPolicy(
                "founder", store.getId(), "",
                1, 0, 0,
                List.of(), 0.05f, "Apple", 0, 0, "");

        assertFalse(ok);
        verify(storeRepo, never()).update(any());
    }






    /* ======================================================================
       4) calculatePrice – failure guards
       ====================================================================== */

    @Test
    void calculatePrice_storeNotFound_throws() {
        when(storeRepo.getById("missing")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> svc.calculatePrice("missing", Map.of()));
    }

    @Test
    void calculatePrice_productNotFound_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> svc.calculatePrice(
                        store.getId(), Map.of("ghost-product", 1)));
    }
}
