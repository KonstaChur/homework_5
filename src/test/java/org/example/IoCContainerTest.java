package org.example;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IoCContainerTest {

    private IoCContainer container;
    private Map<String, Object> scope;
    String hello1;
    String hello2;

    @Before
    public void setUp() {
        container = new IoCContainer();
        scope = new HashMap<>();
    }

    @Test
    public void testRegister() {
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("Hello");

        container.register("hello", supplier);

        String hello = container.resolve("hello");

        assertEquals("Hello", hello);
    }

    @Test
    public void testScope() {
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("Hello");

        container.register("hello", supplier);

        container.scopes.set(scope);

        String hello = container.resolve("hello");

        assertEquals("Hello", hello);
    }

    @Test
    public void testParentScope() {
        Supplier<String> parentSupplier = mock(Supplier.class);
        when(parentSupplier.get()).thenReturn("Parent");

        container.register("hello", parentSupplier);

        Supplier<String> childSupplier = mock(Supplier.class);
        when(childSupplier.get()).thenReturn("Child");

        Map<String, Object> childScope = new HashMap<>();

        container.scopes.set(scope);
        container.scopes.set(childScope);

        container.register("hello", childSupplier);

        String greeting = container.resolve("hello");

        assertEquals("Child", greeting);
    }

    @Test
    public void testUnboundKey() {
        try {
            container.resolve("unknown");
        } catch (IllegalArgumentException e) {
            assertEquals("Отсутствует ключ: unknown", e.getMessage());
        }
    }

    @Test
    public void testClearScope() {
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("Hello");
        container.register("hello", supplier);

        String hello = container.resolve("hello");

        assertEquals("Hello", hello);

        container.scopes.remove();

        assertNull(container.scopes.get());
    }

    @Test
    public void testTwoThreads() throws InterruptedException {
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("Hello");
        container.register("hello", supplier);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            hello1 = container.resolve("hello");
            assertEquals("Hello", hello1);
        });

        executorService.submit(() -> {
            hello2 = container.resolve("hello");
            assertEquals("Hello", hello2);
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(hello1, hello2);
    }
}
