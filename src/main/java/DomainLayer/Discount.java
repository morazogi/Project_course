package DomainLayer;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class Discount {

    enum Level {
        UNDEFINED,
        PRODUCT,
        CATEGORY,
        STORE
    }

    enum LogicComposition {
        UNDEFINED,
        XOR,
        AND,
        OR
    }

    enum NumericalComposition {
        UNDEFINED,
        MAXIMUM,
        MULTIPLICATION
    }

    enum ConditionalType {
        UNDEFINED,
        NONE,
        MIN_PRICE,
        MIN_QUANTITY,
        MAX_QUANTITY
    }


    public boolean alreadyUsed = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String Id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Enumerated(EnumType.STRING)
    public Level level;

    @Enumerated(EnumType.STRING)
    public LogicComposition logicComposition;

    @Enumerated(EnumType.STRING)
    public NumericalComposition numericalComposition;

    @ElementCollection
    @CollectionTable(
            name = "discount_strings",
            joinColumns = @JoinColumn(name = "discount_id")
    )
    @Column(name = "discount_value")
    private List<String> discountsString = new ArrayList<>();

    public float percentDiscount = 0;
    public String discounted = "";

    @Enumerated(EnumType.STRING)
    public ConditionalType conditional;

    public float limiter = -1;
    public String conditionalDiscounted = "";

    public Discount() { }

    public Discount(
            String storeId,
            float level,
            float logicComposition,
            float numericalComposition,
            List<String> discounts,
            float percentDiscount,
            String discounted,
            float conditional,
            float limiter,
            String conditionalDiscounted
    ) {
        this.storeId = storeId;
        if (level == 1)       this.level = Level.PRODUCT;
        else if (level == 2)  this.level = Level.CATEGORY;
        else if (level == 3)  this.level = Level.STORE;
        else                  this.level = Level.UNDEFINED;

        if (logicComposition == 1)       this.logicComposition = LogicComposition.XOR;
        else if (logicComposition == 2)  this.logicComposition = LogicComposition.AND;
        else if (logicComposition == 3)  this.logicComposition = LogicComposition.OR;
        else                             this.logicComposition = LogicComposition.UNDEFINED;

        if (numericalComposition == 1)       this.numericalComposition = NumericalComposition.MAXIMUM;
        else if (numericalComposition == 2)  this.numericalComposition = NumericalComposition.MULTIPLICATION;
        else                                 this.numericalComposition = NumericalComposition.UNDEFINED;

        this.discountsString = discounts != null ? discounts : new ArrayList<>();
        this.percentDiscount = percentDiscount;
        this.discounted      = discounted != null ? discounted : "";

        if (conditional == 1)       this.conditional = ConditionalType.MIN_PRICE;
        else if (conditional == 2)  this.conditional = ConditionalType.MIN_QUANTITY;
        else if (conditional == 3)  this.conditional = ConditionalType.MAX_QUANTITY;
        else                        this.conditional = ConditionalType.UNDEFINED;

        this.limiter = limiter;
        this.conditionalDiscounted = conditionalDiscounted != null ? conditionalDiscounted : "";
    }

    public String  getStoreId()                   { return storeId; }
    public boolean isAlreadyUsed()                { return alreadyUsed; }
    public String  getId()                        { return Id; }
    public Level   getLevel()                     { return level; }
    public LogicComposition getLogicComposition() { return logicComposition; }
    public NumericalComposition getNumericalComposition() { return numericalComposition; }
    public List<String> getDiscounts()            { return discountsString; }
    public float  getPercentDiscount()            { return percentDiscount; }
    public String getDiscounted()                 { return discounted; }
    public ConditionalType getConditional()       { return conditional; }
    public float  getLimiter()                    { return limiter; }
    public String getConditionalDiscounted()      { return conditionalDiscounted; }

    public synchronized void setAlreadyUsed(boolean alreadyUsed)      { this.alreadyUsed = alreadyUsed; }
    public synchronized void setId(String Id)                         { this.Id = Id; }
    public synchronized void setLevel(Level level)                    { this.level = level; }
    public synchronized void setLogicComposition(LogicComposition l)  { this.logicComposition = l; }
    public synchronized void setNumericalComposition(NumericalComposition n){ this.numericalComposition = n; }
    public synchronized void setDiscounts(List<String> discounts)     { this.discountsString = discounts; }
    public synchronized void setPercentDiscount(float percentDiscount){ this.percentDiscount = percentDiscount; }
    public synchronized void setDiscounted(String discounted)         { this.discounted = discounted; }
    public synchronized void setConditional(ConditionalType c)        { this.conditional = c; }
    public synchronized void setLimiter(float limiter)                { this.limiter = limiter; }
    public synchronized void setConditionalDiscounted(String c)       { this.conditionalDiscounted = c; }

    /* ─────────────────────────────  CHANGED SECTION  ───────────────────────────── */

    public Map<Product, Float> applyDiscount(
            Float originalPrice,
            Map<Product,Integer> productsQuantity,
            Map<Product,Float>   productDiscounts,
            List<Discount>       nested)
    {
        if (alreadyUsed) return productDiscounts;

        /* ------------- gather every discount whose condition matters ------------- */
        List<Discount> evaluated = new ArrayList<>(nested);
        if (this.conditional != ConditionalType.UNDEFINED)
            evaluated.add(this);                      // parent counts only if it has a real condition

        int satisfied = (int) evaluated.stream()
                .filter(d -> d.checkConditinal(originalPrice, productsQuantity))
                .count();

        boolean gateOpens = switch (logicComposition) {
            case UNDEFINED -> this.checkConditinal(originalPrice, productsQuantity);
            case AND       -> evaluated.isEmpty() || satisfied == evaluated.size();
            case OR        -> satisfied > 0;
            case XOR       -> satisfied == 1;
        };

        if (!gateOpens) return productDiscounts;

        /* when gate opens, *all* discounts inside the block apply */
        return applyNewMultiplier(originalPrice, productsQuantity, productDiscounts, nested);
    }

    /* ─────────────────────────────  UNCHANGED BELOW  ───────────────────────────── */

    public Map<Product, Float> applyNewMultiplier(
            Float originalPrice,
            Map<Product,Integer> productsQuantity,
            Map<Product, Float> productDiscounts,
            List<Discount> nested)
    {
        List<Discount> all = new ArrayList<>();
        all.add(this);
        all.addAll(nested);

        java.util.function.Predicate<Product> inScope = p -> switch (level) {
            case PRODUCT   -> discounted.isBlank() || p.getName().equals(discounted);
            case CATEGORY  -> discounted.isBlank() || p.getCategory().equals(discounted);
            case STORE, UNDEFINED -> true;
        };

        float newMultiplier;
        switch (numericalComposition) {
            case MAXIMUM -> {
                float maxPct = all.stream()
                        .map(d -> d.percentDiscount)
                        .max(Float::compare)
                        .orElse(0f);
                newMultiplier = Math.max(0f, 1f - maxPct);
            }
            case MULTIPLICATION -> {
                newMultiplier = 1f;
                for (Discount d : all) newMultiplier *= (1f - d.percentDiscount);
            }
            default -> {
                float totalPct = 0f;
                for (Discount d : all) totalPct += d.percentDiscount;
                totalPct = Math.min(totalPct, 1f);
                newMultiplier = 1f - totalPct;
            }
        }

        for (Map.Entry<Product, Float> e : productDiscounts.entrySet()) {
            Product p = e.getKey();
            float curr = e.getValue();
            if (inScope.test(p)) productDiscounts.put(p, curr * newMultiplier);
        }

        this.alreadyUsed = true;
        for (Discount d : nested) d.alreadyUsed = true;

        return productDiscounts;
    }

    boolean checkConditinal(float originalPrice, Map<Product , Integer> products) {
        if (this.conditional == ConditionalType.UNDEFINED) {
            return true;
        }
        else if (this.conditional == ConditionalType.MIN_PRICE) {
            return originalPrice >= limiter;
        }
        else if (this.conditional == ConditionalType.MIN_QUANTITY) {
            for (Map.Entry<Product, Integer> entry : products.entrySet()) {
                Product product = entry.getKey();
                int quantityToBuy = entry.getValue();
                if (product.getName().equals(this.conditionalDiscounted)) {
                    return quantityToBuy >= limiter;
                }
            }
            return false;
        }
        else if (this.conditional == ConditionalType.MAX_QUANTITY) {
            for (Map.Entry<Product, Integer> entry : products.entrySet()) {
                Product product = entry.getKey();
                int quantityToBuy = entry.getValue();
                if (product.getName().equals(this.conditionalDiscounted)) {
                    return quantityToBuy <= limiter;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }
}
