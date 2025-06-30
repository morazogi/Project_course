package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-tests that validate the behaviour of a single {@link Discount}.
 * No repositories or Spring context involved.
 */
class DiscountTest {

    /* ────────────────── helpers ────────────────── */

    private Product apple;      // price 10, category Food
    private Product banana;     // price 6,  category Food
    private Product headset;    // price 22, category Tech

    private Map<Product,Integer> qty;
    private Map<Product,Float>   mul;

    @BeforeEach
    void freshMaps() {
        apple   = new Product("s","Apple",  "",10f,100,0d,"Food");
        banana  = new Product("s","Banana", "", 6f,100,0d,"Food");
        headset = new Product("s","Head",   "",22f,100,0d,"Tech");

        qty = new HashMap<>();
        mul = new HashMap<>();
    }
    private float original() {
        return qty.entrySet().stream()
                .map(e -> e.getKey().getPrice() * e.getValue())
                .reduce(0f, Float::sum);
    }

    /* ────────────────── section 1: simple voucher ─────────── */

    @Test @DisplayName("Product-level 10 % touches only that product")
    void productVoucher() {
        qty.put(apple,2);  qty.put(headset,1);
        mul.put(apple,1f); mul.put(headset,1f);

        Discount d = new Discount("s",1,0,0,List.of(),
                .10f,"Apple",-1,-1,"");

        var out = d.applyDiscount(original(), qty, mul, List.of());
        assertEquals(0.9f,out.get(apple));
        assertEquals(1.0f,out.get(headset));
    }

    /* ────────────────── section 2: numerical MAXIMUM ──────── */

    @Test @DisplayName("Category MAXIMUM picks highest pct")
    void categoryMaximum() {
        qty.put(apple,1);  mul.put(apple,1f);

        Discount in10 = new Discount("s",1,0,0,List.of(),.10f,"",0,0,"");
        Discount in20 = new Discount("s",1,0,0,List.of(),.20f,"",0,0,"");
        Discount parent = new Discount("s",2,0,1,   // MAXIMUM
                List.of(), .05f,"Food",0,0,"");

        var out = parent.applyDiscount(original(), qty, mul, List.of(in10,in20));
        assertEquals(0.8f,out.get(apple),0.0001);     // 20 % wins
    }

    /* ────────────────── section 3: quantity gates ─────────── */

    @Nested class QuantityGates {

        @Test void minQuantity() {
            Discount d = new Discount("s",1,0,0,List.of(),
                    .15f,"Apple",2,3,"Apple");   // ≥3

            qty.put(apple,2); mul.put(apple,1f);
            assertEquals(1f, d.applyDiscount(original(),qty,mul,List.of()).get(apple));

            qty.put(apple,3); mul.put(apple,1f);
            assertEquals(0.85f,d.applyDiscount(original(),qty,mul,List.of()).get(apple));
        }

        @Test void maxQuantity() {
            Discount d = new Discount("s",1,0,0,List.of(),
                    .20f,"Banana",3,5,"Banana"); // ≤5

            qty.put(banana,7); mul.put(banana,1f);
            assertEquals(1f, d.applyDiscount(original(),qty,mul,List.of()).get(banana));

            qty.put(banana,4); mul.put(banana,1f);
            assertEquals(0.8f,d.applyDiscount(original(),qty,mul,List.of()).get(banana));
        }
    }

    /* ────────────────── section 4: MIN_PRICE gate ─────────── */

    @Test void minPriceCondition() {
        Discount d = new Discount("s",3,0,2,List.of(),
                .25f,"",1,40,"");      // 25 % if subtotal ≥40

        qty.put(apple,3); mul.put(apple,1f);           // subtotal 30
        assertEquals(1f,d.applyDiscount(original(),qty,mul,List.of()).get(apple));

        qty.put(headset,1);                            // subtotal 52
        mul.put(apple,1f); mul.put(headset,1f);
        var out = d.applyDiscount(original(),qty,mul,List.of());
        assertEquals(0.75f,out.get(apple));
        assertEquals(0.75f,out.get(headset));
    }

    /* ────────────────── section 5: logical comp. ──────────── */

    @Nested class LogicalComp {

        Discount min2, max4;   // non-overlapping for XOR demo

        @BeforeEach
        void mk() {
            min2 = new Discount("s",1,0,0,List.of(),0f,"Apple",2,2,"Apple"); // ≥2
            max4 = new Discount("s",1,0,0,List.of(),0f,"Apple",3,4,"Apple"); // ≤4
        }

        @Test @DisplayName("OR – gate opens when **any** child true")
        void logicOr() {

            /* helper lambdas so we get fresh objects each time */
            java.util.function.Supplier<Discount> min2 =
                    () -> new Discount("s", 1,0,0,List.of(),0f,"Apple",2,2,"Apple");
            java.util.function.Supplier<Discount> max4 =
                    () -> new Discount("s", 1,0,0,List.of(),0f,"Apple",3,4,"Apple");
            java.util.function.Supplier<Discount> parent =
                    () -> new Discount("s", 1,3,0,List.of(),.10f,"Apple",0,0,"");   // OR

            /* --- case 1: 7 Apples (only MIN ≥2 true) --- */
            qty.put(apple,7); mul.put(apple,1f);
            assertEquals(0.9f,
                    parent.get().applyDiscount(
                            original(), qty, mul,
                            List.of(min2.get(), max4.get())
                    ).get(apple));

            /* --- case 2: 1 Apple (only MAX ≤4 true) --- */
            qty.put(apple,1); mul.put(apple,1f);
            assertEquals(0.9f,
                    parent.get().applyDiscount(
                            original(), qty, mul,
                            List.of(min2.get(), max4.get())
                    ).get(apple));
        }

        @Test @DisplayName("XOR – gate opens when **exactly one** child true")
        void logicXor() {
            Discount p = new Discount("s",1,1,0,List.of(),.20f,"Apple",0,0,"");

            qty.put(apple,7); mul.put(apple,1f);        // only min2 true
            assertEquals(0.8f,
                    p.applyDiscount(original(),qty,mul,List.of(min2,max4)).get(apple));

            qty.put(apple,3); mul.put(apple,1f);        // both true → closed
            assertEquals(1f,
                    p.applyDiscount(original(),qty,mul,List.of(min2,max4)).get(apple));
        }

        @Test @DisplayName("AND – gate opens only when **all** true")
        void logicAnd() {
            Discount p = new Discount("s",1,2,0,List.of(),.15f,"Apple",0,0,"");

            qty.put(apple,7); mul.put(apple,1f);        // only one true
            assertEquals(1f,
                    p.applyDiscount(original(),qty,mul,List.of(min2,max4)).get(apple));

            qty.put(apple,3); mul.put(apple,1f);        // both true
            assertEquals(0.85f,
                    p.applyDiscount(original(),qty,mul,List.of(min2,max4)).get(apple));
        }
    }

    /* ────────────────── section 6: alreadyUsed ─────────────── */

    @Test void alreadyUsed() {
        Discount d = new Discount("s",1,0,0,List.of(),.3f,"Apple",0,0,"");

        qty.put(apple,1); mul.put(apple,1f);
        d.applyDiscount(original(),qty,mul,List.of());
        assertTrue(d.isAlreadyUsed());

        mul.put(apple,0.7f);
        assertEquals(0.7f,
                d.applyDiscount(original(),qty,mul,List.of()).get(apple));
    }

    /* ────────────────── section 7: multiplication & clamp ─── */

    @Test void multiplication() {
        Discount c10 = new Discount("s",1,0,0,List.of(),.1f,"Apple",0,0,"");
        Discount c20 = new Discount("s",1,0,0,List.of(),.2f,"Apple",0,0,"");
        Discount p   = new Discount("s",1,0,2,List.of(),0f,"Apple",0,0,"");

        qty.put(apple,1); mul.put(apple,1f);
        var out = p.applyDiscount(original(),qty,mul,List.of(c10,c20));
        assertEquals(0.72f,out.get(apple),0.001);
    }

    @Test void additiveClamp() {
        Discount d70 = new Discount("s",1,0,0,List.of(),.7f,"Apple",0,0,"");
        Discount d40 = new Discount("s",1,0,0,List.of(),.4f,"Apple",0,0,"");
        Discount p   = new Discount("s",1,0,0,List.of(),0f,"Apple",0,0,"");

        qty.put(apple,1); mul.put(apple,1f);
        var out = p.applyDiscount(original(),qty,mul,List.of(d70,d40));
        assertEquals(0f,out.get(apple));   // clamp at zero
    }
}
