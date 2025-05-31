/*
package DomainLayer;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;





public class DiscountPolicy {

    private List<Discount> discounts = new ArrayList<>();


    public float applyDiscounts(Map<Product , Integer> productQuantity){

        float originalPrice = 0f;
        for (Map.Entry<Product, Integer> entry : productQuantity.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            originalPrice += product.getPrice() * quantity;
        }


        Map<Product, Float> productDiscount = new HashMap<>();
        for (Product product : productQuantity.keySet()) {
            productDiscount.put(product, 1.0f);
        }

        for (Discount d : discounts) {
            productDiscount = d.applyDiscount(productDiscount, originalPrice, productQuantity);
        }

        float total = 0f;
        for (Map.Entry<Product, Float> entry : productDiscount.entrySet()) {
            Product product = entry.getKey();
            float priceMultiplier = entry.getValue();
            int quantity = productQuantity.get(product);
            total += product.getPrice() * priceMultiplier * quantity;
        }

        return total;
    }



    public boolean removeDiscount(String discountId) {
        boolean removed = false;

        // Remove the discount from the top-level list if it exists
        Iterator<Discount> iterator = discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getId().equals(discountId)) {
                iterator.remove();
                removed = true;
            }
        }

        // Process each discount in the current list to remove the discountId from their nested discounts
        List<Discount> currentDiscounts = new ArrayList<>(discounts); // Create a copy to iterate safely
        for (Discount d : currentDiscounts) {
            boolean childRemoved = removeDiscountFromDiscount(d, discountId);
            removed = removed || childRemoved;
        }

        return removed;
    }

    private boolean removeDiscountFromDiscount(Discount parentDiscount, String discountId) {
        boolean removed = false;
        Iterator<Discount> iterator = parentDiscount.discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getId().equals(discountId)) {
                iterator.remove();
                removed = true;
            } else {
                // Recursively process the nested discounts of the current discount
                boolean childRemoved = removeDiscountFromDiscount(d, discountId);
                removed = removed || childRemoved;
            }
        }
        return removed;
    }


    public void addDiscount(
            String Id,
            float level,
            float logicComposition,
            float numericalComposition,
            List<String> discountsId,
            float percentDiscount,
            String discounted,
            float conditional,
            float limiter,
            String conditionalDiscounted
    ) {

         List<Discount> discountsWithId = discounts.stream()
                .filter(discount -> discountsId.contains(discount.getId()))
                .collect(Collectors.toList());

        Discount discount = new Discount(
                Id,
                level,
                logicComposition,
                numericalComposition,
                discountsWithId,     //Should be initialized as all the discounts with the same id as List<String> discounts
                percentDiscount,
                discounted,
                conditional,
                limiter,
                conditionalDiscounted
        );
        this.discounts.add(discount);

    }



}


class Discount {

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
        MIN_QUANTITY    // minimum quantity of items
    }


    // Tracks if this discount has been applied (prevents reuse in 'Maximum' composition)
    boolean alreadyUsed = false;

    // Unique identifier for the discount instance
    String Id;

    // Scope of the discount:
    // 1 = Product-level, 2 = Category-level, 3 = Store-wide
    Level  level;

    // Logical combination with nested discounts:
    // 1 = XOR (exactly one condition must be true),
    // 2 = AND (all conditions must be true),
    // 3 = OR (any condition can be true)
    LogicComposition logicComposition;

    // How discount percentages are combined numerically:
    // 1 = Maximum (use highest discount),
    // 2 = Multiplication (stack discounts multiplicatively)
    NumericalComposition  numericalComposition;

    // Nested discounts for complex discount combinations
    List<Discount> discounts = new ArrayList<>();

    // Discount percentage to apply (e.g., 0.15 = 15% off)
    float percentDiscount = 0;

    // Target of the discount (interpretation depends on level):
    // - Product-level: product name
    // - Category-level: category name
    // - Store-wide: unused (applies to all)
    String discounted = "";

    // Condition type to activate discount:
    // -1 = No condition,
    // 1 = Minimum total price,
    // 2 = Minimum quantity of items
    ConditionalType  conditional;

    // Threshold value for the condition:
    // - For price condition: minimum total required
    // - For quantity condition: minimum items required
    float limiter = -1;

    // Target of the condition check:
    // - For price condition: unused
    // - For quantity condition: product name to check
    String conditionalDiscounted = "";





    public Discount(
            String Id,
            float level,
            float logicComposition,
            float numericalComposition,
            List<Discount> discounts,
            float percentDiscount,
            String discounted,
            float conditional,
            float limiter,
            String conditionalDiscounted
    ) {
        this.Id = Id;
        if(level == 1){
            this.level = Level.PRODUCT;
        }
        else if(level == 2){
            this.level = Level.CATEGORY;
        }
        else if(level == 3){
            this.level = Level.STORE;
        }
        else {
            this.level = Level.UNDEFINED;
        }

        if(logicComposition == 1){
            this.logicComposition = LogicComposition.XOR;
        }
        else if(logicComposition == 2){
            this.logicComposition = LogicComposition.AND;
        }
        else if(logicComposition == 3){
            this.logicComposition = LogicComposition.OR;
        }
        else {
            this.logicComposition = LogicComposition.UNDEFINED;
        }


        if(numericalComposition == 1){
            this.numericalComposition = NumericalComposition.MAXIMUM;
        }
        else if(numericalComposition == 2){
            this.numericalComposition = NumericalComposition.MULTIPLICATION;
        }
        else {
            this.numericalComposition = NumericalComposition.UNDEFINED;
        }


        this.discounts = discounts != null ? discounts : new ArrayList<>();
        this.percentDiscount = percentDiscount;
        this.discounted = discounted != null ? discounted : "";

        if(conditional == 1){
            this.conditional = ConditionalType.MIN_PRICE;
        }
        else if(conditional == 2){
            this.conditional = ConditionalType.MIN_QUANTITY;
        }
        else {
            this.conditional = ConditionalType.UNDEFINED;
        }

        this.limiter = limiter;
        this.conditionalDiscounted = conditionalDiscounted != null ? conditionalDiscounted : "";
    }


    public String getId() {
        return this.Id;
    }




    public Map<Product, Float> applyDiscount(Map<Product, Float> productDiscounts, float originalPrice, Map<Product, Integer> productsQuantity ){

        if(alreadyUsed)
            return productDiscounts;



        if(logicComposition == LogicComposition.UNDEFINED){
            if (checkConditinal(originalPrice, productsQuantity)) {
                return applyNewMultiplier(productDiscounts, productsQuantity);
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

            if (predict % 2 == 1){
                return this.applyNewMultiplier(productDiscounts, productsQuantity);

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
                return this.applyNewMultiplier(productDiscounts, productsQuantity);
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
                return this.applyNewMultiplier(productDiscounts, productsQuantity);
            }
        }


        return productDiscounts;

    }



    public Map<Product, Float> applyNewMultiplier(Map<Product, Float> productDiscounts, Map<Product, Integer> productsQuantity){

        if (level == Level.PRODUCT){       //product
            if(numericalComposition == NumericalComposition.MAXIMUM){        //1 = Maximum
                // Find maximum discount percentage among nested discounts
                float maxDiscount = this.percentDiscount;
                for (Discount d : discounts) {
                    if (d.percentDiscount > maxDiscount) {
                        maxDiscount = d.percentDiscount;
                    }
                }

                // Apply the maximum discount found
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted) || "".equals(this.discounted)) {
                        float discountedValue = value - maxDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }

                // Mark all discounts as used except the one with max percentage
                this.alreadyUsed = true;
                for (Discount d : discounts) {
                    d.alreadyUsed = true; // Prevent all nested discounts from reapplying
                }

                return productDiscounts;
            }


            else if(numericalComposition == NumericalComposition.MULTIPLICATION){     //2 = Multiplication
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted ) || "".equals(this.discounted ) ) {
                        float discountedValue =  value * (1 - percentDiscount);
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }

            else if(numericalComposition == NumericalComposition.UNDEFINED){
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted ) || "".equals(this.discounted ) ) {
                        System.out.println("Discounted " + product.getName());
                        float discountedValue =  value - percentDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }
        }













        else if (level == Level.CATEGORY){  //category
            if(numericalComposition == NumericalComposition.MAXIMUM){        //1 = Maximum
                // Find maximum discount percentage among nested discounts
                float maxDiscount = this.percentDiscount;
                for (Discount d : discounts) {
                    if (d.percentDiscount > maxDiscount) {
                        maxDiscount = d.percentDiscount;
                    }
                }

                // Apply the maximum discount found
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted) || "".equals(this.discounted)) {
                        float discountedValue = value - maxDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }

                // Mark all discounts as used except the one with max percentage
                this.alreadyUsed = true;
                for (Discount d : discounts) {
                    d.alreadyUsed = true; // Prevent all nested discounts from reapplying
                }

                return productDiscounts;
            }













            else if(numericalComposition == NumericalComposition.MULTIPLICATION){     //2 = Multiplication
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getCategory().toString().equals(this.discounted)) {
                        float discountedValue =  value * (1 - percentDiscount);
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }









            else if(numericalComposition == NumericalComposition.UNDEFINED){

                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted)) {
                        float discountedValue =  value - percentDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }
        }
















        else if (level == Level.STORE){  //store
            if(numericalComposition == NumericalComposition.MAXIMUM){        //1 = Maximum
                // Find maximum discount percentage among nested discounts
                float maxDiscount = this.percentDiscount;
                for (Discount d : discounts) {
                    if (d.percentDiscount > maxDiscount) {
                        maxDiscount = d.percentDiscount;
                    }
                }

                // Apply the maximum discount found
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (product.getName().equals(this.discounted) || "".equals(this.discounted)) {
                        float discountedValue = value - maxDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }

                // Mark all discounts as used except the one with max percentage
                this.alreadyUsed = true;
                for (Discount d : discounts) {
                    d.alreadyUsed = true; // Prevent all nested discounts from reapplying
                }

                return productDiscounts;
            }












            else if(numericalComposition == NumericalComposition.MULTIPLICATION){     //2 = Multiplication
                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (true) {
                        float discountedValue =  value * (1 - percentDiscount);
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }












            else if(numericalComposition == NumericalComposition.UNDEFINED){

                for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
                    Product product = entry.getKey();
                    float value = entry.getValue();

                    if (true) {
                        float discountedValue =  value - percentDiscount;
                        productDiscounts.put(product, discountedValue);
                    }
                }
                return productDiscounts;
            }

        }
        return productDiscounts;

    }








    private float calculateOriginalPriceP(Map<Product, Float> productDiscounts) {
        float total = 0;
        for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
            if(entry.getKey().getName().equals(this.discounted))
                total += (float) entry.getKey().getPrice();
        }
        return total;
    }


    private float calculateOriginalPriceC(Map<Product, Float> productDiscounts) {
        float total = 0;
        for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
            if(entry.getKey().getCategory().toString().equals(this.discounted))
                total += (float) entry.getKey().getPrice();
        }
        return total;
    }


    private float calculateOriginalPriceS(Map<Product, Float> productDiscounts) {
        float total = 0;
        for (Map.Entry<Product, Float> entry : productDiscounts.entrySet()) {
            total += (float) entry.getKey().getPrice();
        }
        return total;
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
        else{
            return false;
        }
    }
}*/
