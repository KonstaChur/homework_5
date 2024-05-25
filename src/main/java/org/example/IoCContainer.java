package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IoCContainer {

    private final Map<String, Supplier<Object>> bindings = new HashMap<>();
    final ThreadLocal<Map<String, Object>> scopes = new ThreadLocal<>();

    public <T> void register(String key, Supplier<T> supplier) {
        bindings.put(key, (Supplier<Object>) supplier);
    }

    public <T> T resolve(String key, Object... args) {
        Map<String, Object> scope = scopes.get();
        if (scope == null) {
            scope = new HashMap<>();
            scopes.set(scope);
        }
        Object instance = scope.get(key);
        if (instance == null) {
            Supplier<Object> supplier = bindings.get(key);
            if (supplier == null) {
                throw new IllegalArgumentException("Отсутствует ключ: " + key);
            }
            instance = supplier.get();
            scope.put(key, instance);
        }
        return (T) instance;
    }
}



