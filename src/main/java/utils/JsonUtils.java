// utils/JsonUtils.java
package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;

public final class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    private JsonUtils() {}

    /* ---------- the names Response expects ---------- */

    /** Alias for toJson */
    public static <T> String serialize(T obj) {
        return toJson(obj);
    }

    /** Alias (Class<T> version) */
    public static <T> T deserialize(String json, Class<T> clazz) {
        return fromJson(json, clazz);
    }

    /** Alias (java.lang.reflect.Type version) */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String json, Type type) {
        try {
            // Jackson can handle TypeReference constructed from Type
            return mapper.readValue(json, mapper.constructType(type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization error", e);
        }
    }

    /* ---------- original helpers ---------- */

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization error", e);
        }
    }

    public static ObjectMapper mapper() { return mapper; }
}