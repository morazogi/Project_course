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
        UNDEFINED,      // not yet set
        NONE,           // no condition
        MIN_PRICE,      // minimum total price
        MIN_QUANTITY,   // minimum quantity of items
        MAX_QUANTITY    // maximum quantity of items
    }


    // Tracks if this discount has been applied (prevents reuse in 'Maximum' composition)
    public boolean alreadyUsed = false;

    // Unique identifier for the discount instance
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String Id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    // Scope of the discount:
    // 1 = Product-level, 2 = Category-level, 3 = Store-wide
    @Enumerated(EnumType.STRING)
    public Level level;

    // Logical combination with nested discounts:
    // 1 = XOR (exactly one condition must be true),
    // 2 = AND (all conditions must be true),
    // 3 = OR (any condition can be true)
    @Enumerated(EnumType.STRING)
    public LogicComposition logicComposition;

    // How discount percentages are combined numerically:
    // 1 = Maximum (use highest discount),
    // 2 = Multiplication (stack discounts multiplicatively)
    @Enumerated(EnumType.STRING)
    public NumericalComposition numericalComposition;

    // Nested discounts for complex discount combinations
    @ElementCollection
    @CollectionTable(
            name = "discount_strings",
            joinColumns = @JoinColumn(name = "discount_id")
    )
    @Column(name = "discount_value")
    private List<String> discountsString = new ArrayList<>();

    // Discount percentage to apply (e.g., 0.15 = 15% off)
    public float percentDiscount = 0;

    // Target of the discount (interpretation depends on level):
    // - Product-level: product name
    // - Category-level: category name
    // - Store-wide: unused (applies to all)
    public String discounted = "";

    // Condition type to activate discount:
    // -1 = No condition,
    // 1 = Minimum total price,
    // 2 = Minimum quantity of item
    // 3 = Maximum quantity of item
    @Enumerated(EnumType.STRING)
    public ConditionalType conditional;

    // Threshold value for the condition:
    // - For price condition: minimum total required
    // - For quantity condition: minimum items required
    public float limiter = -1;

    // Target of the condition check:
    // - For price condition: unused
    // - For quantity condition: product name to check
    public String conditionalDiscounted = "";

    public Discount() {
        // Required by JPA
    }

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
        if (level == 1) {
            this.level = Level.PRODUCT;
        } else if (level == 2) {
            this.level = Level.CATEGORY;
        } else if (level == 3) {
            this.level = Level.STORE;
        } else {
            this.level = Level.UNDEFINED;
        }

        if (logicComposition == 1) {
            this.logicComposition = LogicComposition.XOR;
        } else if (logicComposition == 2) {
            this.logicComposition = LogicComposition.AND;
        } else if (logicComposition == 3) {
            this.logicComposition = LogicComposition.OR;
        } else {
            this.logicComposition = LogicComposition.UNDEFINED;
        }


        if (numericalComposition == 1) {
            this.numericalComposition = NumericalComposition.MAXIMUM;
        } else if (numericalComposition == 2) {
            this.numericalComposition = NumericalComposition.MULTIPLICATION;
        } else {
            this.numericalComposition = NumericalComposition.UNDEFINED;
        }


        this.discountsString = discounts != null ? discounts : new ArrayList<>();
        this.percentDiscount = percentDiscount;
        this.discounted = discounted != null ? discounted : "";

        if (conditional == 1) {
            this.conditional = ConditionalType.MIN_PRICE;
        } else if (conditional == 2) {
            this.conditional = ConditionalType.MIN_QUANTITY;
        } else if (conditional == 3) {
            this.conditional = ConditionalType.MAX_QUANTITY;
        } else {
            this.conditional = ConditionalType.UNDEFINED;
        }

        this.limiter = limiter;
        this.conditionalDiscounted = conditionalDiscounted != null ? conditionalDiscounted : "";
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public boolean isAlreadyUsed() { return alreadyUsed; }
    public String getId() { return Id; }
    public Level getLevel() { return level; }
    public LogicComposition getLogicComposition() { return logicComposition; }
    public NumericalComposition getNumericalComposition() { return numericalComposition; }
    public List<String> getDiscounts() { return discountsString; }
    public float getPercentDiscount() { return percentDiscount; }
    public String getDiscounted() { return discounted; }
    public ConditionalType getConditional() { return conditional; }
    public float getLimiter() { return limiter; }
    public String getConditionalDiscounted() { return conditionalDiscounted; }

    public synchronized void setAlreadyUsed(boolean alreadyUsed) { this.alreadyUsed = alreadyUsed; }
    public synchronized void setId(String Id) { this.Id = Id; }
    public synchronized void setLevel(Level level) { this.level = level; }
    public synchronized void setLogicComposition(LogicComposition logicComposition) { this.logicComposition = logicComposition; }
    public synchronized void setNumericalComposition(NumericalComposition numericalComposition) { this.numericalComposition = numericalComposition; }
    public synchronized void setDiscounts(List<String> discounts) { this.discountsString = discounts; }
    public synchronized void setPercentDiscount(float percentDiscount) { this.percentDiscount = percentDiscount; }
    public synchronized void setDiscounted(String discounted) { this.discounted = discounted; }
    public synchronized void setConditional(ConditionalType conditional) { this.conditional = conditional; }
    public synchronized void setLimiter(float limiter) { this.limiter = limiter; }
    public synchronized void setConditionalDiscounted(String conditionalDiscounted) { this.conditionalDiscounted = conditionalDiscounted; }

    public Map<Product, Float> applyDiscount(Float originalPrice, Map<Product , Integer> productsQuantity, Map<Product, Float> productDiscounts, List<Discount> discounts){
        if(alreadyUsed) {
            return productDiscounts;
        }

        if(logicComposition == LogicComposition.UNDEFINED){
            if (checkConditinal(originalPrice, productsQuantity)) {
                return this.applyNewMultiplier(originalPrice, productsQuantity, productDiscounts, discounts);
            } else {
                return productDiscounts;
            }
        }

        else if(logicComposition == LogicComposition.XOR){   //xor
            float predict = 0;         //even false, odd true
            if(this.checkConditinal(originalPrice, productsQuantity)){
                predict = predict + 1;
            }
            for(Discount d : discounts){
                if(d.checkConditinal(originalPrice, productsQuantity)){
                    predict = predict + 1;
                }
            }

            if (predict == 1){
                return this.applyNewMultiplier(originalPrice, productsQuantity, productDiscounts, discounts);

            }
        }

        else if(logicComposition == LogicComposition.AND){  //and
            boolean predict = true;
            if(!this.checkConditinal(originalPrice, productsQuantity)){
                predict = false;
            }
            for(Discount d : discounts){
                if(!d.checkConditinal(originalPrice, productsQuantity)){
                    predict = false;
                }
            }
            if (predict){
                return this.applyNewMultiplier(originalPrice, productsQuantity, productDiscounts, discounts);
            }
        }

        else if(logicComposition == LogicComposition.OR){   //or
            boolean predict = false;
            if(this.checkConditinal(originalPrice, productsQuantity)){
                predict = true;
            }
            for(Discount d : discounts){
                if(d.checkConditinal(originalPrice, productsQuantity)){
                    predict = true;
                }
            }

            if (predict){
                return this.applyNewMultiplier(originalPrice, productsQuantity, productDiscounts, discounts);
            }
        }
        return productDiscounts;
    }


    public Map<Product, Float> applyNewMultiplier(
            Float originalPrice,
            Map<Product,Integer> productsQuantity,
            Map<Product, Float> productDiscounts,
            List<Discount> nested)
    {
        /* ---------------- collect every relevant percentage ---------------- */
        List<Discount> all = new ArrayList<>();
        all.add(this);
        all.addAll(nested);

        /* helper that says whether a given product is inside this discount’s scope */
        java.util.function.Predicate<Product> inScope = p -> switch (level) {
            case PRODUCT   -> discounted.isBlank() || p.getName().equals(discounted);
            case CATEGORY  -> discounted.isBlank() || p.getCategory().equals(discounted);
            case STORE, UNDEFINED -> true;
        };

        /* ------------------------------------------------------------------ *
         *  Calculate the effective multiplier to apply
         * ------------------------------------------------------------------ */
        float newMultiplier;           // will be multiplied into / assigned to map entries

        switch (numericalComposition) {

            /* ---------- 1. Take the single maximum percentage ---------- */
            case MAXIMUM -> {
                float maxPct = all.stream()
                        .map(d -> d.percentDiscount)
                        .max(Float::compare)
                        .orElse(0f);
                newMultiplier = Math.max(0f, 1f - maxPct);
            }

            /* ---------- 2. Multiply every (1 - p) ---------- */
            case MULTIPLICATION -> {
                newMultiplier = 1f;
                for (Discount d : all) {
                    newMultiplier *= (1f - d.percentDiscount);
                }
            }

            /* ---------- 3. Add all percentages, then subtract ---------- */
            default -> {   // UNDEFINED
                float totalPct = 0f;
                for (Discount d : all) totalPct += d.percentDiscount;
                totalPct = Math.min(totalPct, 1f);          // clamp
                newMultiplier = 1f - totalPct;
            }
        }

        /* ---------------- apply to every eligible product ---------------- */
        for (Map.Entry<Product, Float> e : productDiscounts.entrySet()) {
            Product p   = e.getKey();
            float  curr = e.getValue();

            if (inScope.test(p)) {
                productDiscounts.put(p, curr * newMultiplier);
            }
        }

        /* ---------------- mark all these discounts as used ---------------- */
        this.alreadyUsed = true;
        for (Discount d : nested) d.alreadyUsed = true;

        return productDiscounts;
    }
        boolean checkConditinal(float originalPrice, Map<Product , Integer> products){
        if(this.conditional == ConditionalType.UNDEFINED){
            return true;
        }
        else if(this.conditional == ConditionalType.MIN_PRICE){
            return originalPrice >= limiter;
        }
        else if(this.conditional == ConditionalType.MIN_QUANTITY){

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
        else{
            return false;
        }
    }
}