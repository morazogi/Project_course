package utils;

import DomainLayer.Product;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;

/** Tell Jackson how to turn the JSON map‑key back into a Product object. */
public class ProductKeyDes extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctx) throws IOException {
        // simplest safe stub – keep whole key as product‑id
        return new Product(
                key,   // id
                "",    // storeId  (not needed for cart logic)
                key,   // name     (also not needed)
                 0, 0, 0.0 , ""     // description, price, qty, rating
        );
    }
}