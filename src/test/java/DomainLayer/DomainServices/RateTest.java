package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Rate}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateTest {

    /* -------- mocked collaborators -------- */
    @Mock IToken            tokener;
    @Mock StoreRepository   storeRepo;
    @Mock UserRepository    userRepo;
    @Mock ProductRepository productRepo;

    private Rate service;                      // system under test
    private RegisteredUser dummyUser;          // will be spied per-test

    @BeforeEach
    void init() {
        service   = new Rate(tokener, storeRepo, userRepo, productRepo);
        dummyUser = Mockito.mock(RegisteredUser.class);
    }

    /* =============================================================
                           rateStore
       ============================================================= */
    @Test
    void rateStore_happyPath_updatesRepoAndReturnsTrue() throws Exception {
        String token   = "tok";
        String user    = "alice";
        String storeId = "s1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Store store = mock(Store.class);
        when(storeRepo.getById(storeId)).thenReturn(store);
        when(userRepo.getById(user)).thenReturn(dummyUser);
        when(store.rate(4)).thenReturn(true);

        assertTrue(service.rateStore(token, storeId, 4));
        verify(storeRepo).update(store);
    }

    @Test
    void rateStore_invalidRateValue_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rateStore("t", "s", 6));   // >5
    }

    @Test
    void rateStore_nonExistingStore_throws() {
        when(tokener.extractUsername("tok")).thenReturn("user");
        doNothing().when(tokener).validateToken("tok");
        when(storeRepo.getById("noStore")).thenReturn(null);
        when(userRepo.getById("user")).thenReturn(dummyUser);

        assertThrows(IllegalArgumentException.class,
                () -> service.rateStore("tok", "noStore", 3));
    }

    /* =============================================================
                           rateProduct
       ============================================================= */
    @Test
    void rateProduct_happyPath_removesFromListAndSaves() {
        String token = "tok";
        String user  = "bob";
        String pid   = "p1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Product prod = mock(Product.class);
        when(productRepo.getById(pid)).thenReturn(prod);

        /* user owns that product */
        List<String> products = new ArrayList<>(List.of(pid));
        when(dummyUser.getProducts()).thenReturn(products);
        when(userRepo.getById(user)).thenReturn(dummyUser);
        when(prod.addRating(user, 5)).thenReturn(true);   // ignored but harmless

        assertTrue(service.rateProduct(token, pid, 5));
        verify(productRepo).save(prod);
        verify(userRepo).update(dummyUser);

        /* product ID must have been removed */
        assertFalse(products.contains(pid));
    }

    @Test
    void rateProduct_notPurchasedOrAlreadyRated_throws() {
        String token = "tok";
        String user  = "bob";
        String pid   = "p1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Product prod = mock(Product.class);
        when(productRepo.getById(pid)).thenReturn(prod);

        /* user does NOT own that product */
        List<String> products = new ArrayList<>();        // empty list
        when(dummyUser.getProducts()).thenReturn(products);
        when(userRepo.getById(user)).thenReturn(dummyUser);

        assertThrows(IllegalArgumentException.class,
                () -> service.rateProduct(token, pid, 4));

        verify(productRepo, never()).save(any());
        verify(userRepo,  never()).update(any());
    }
}
