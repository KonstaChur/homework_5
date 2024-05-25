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

public class IoCContainerThreadLocalTest {

    private IoCContainer container;

    @Before
    public void setUp() {
        container = new IoCContainer();
    }

    @Test
    public void shouldResolveBindingsFromDifferentThreads() throws InterruptedException {
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("Hello World!");

        container.register("greeting", supplier);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            String greeting = container.resolve("greeting");
            assertEquals("Hello World!", greeting);
        });

        executorService.submit(() -> {
            String greeting = container.resolve("greeting");
            assertEquals("Hello World!", greeting);
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldResolveBindingsFromDifferentScopes() throws InterruptedException {
        Supplier<String> parentSupplier = mock(Supplier.class);
        when(parentSupplier.get()).thenReturn("Hello Parent!");

        container.register("greeting", parentSupplier);

        Supplier<String> childSupplier = mock(Supplier.class);
        when(childSupplier.get()).thenReturn("Hello Child!");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            container.scopes.set(new HashMap<>());

            container.register("greeting", childSupplier);

            String greeting = container.resolve("greeting");

            assertEquals("Hello Child!", greeting);
        });

        executorService.submit(() -> {
            String greeting = container.resolve("greeting");

            assertEquals("Hello Parent!", greeting);
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldClearCurrentScopeAfterThreadCompletion() throws InterruptedException {
        Map<String, Object> scope = new HashMap<>();
        scope.put("greeting", "Hello Scope!");

        container.scopes.set(scope);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            String greeting = container.resolve("greeting");

            assertEquals("Hello Scope!", greeting);
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertNull(container.scopes.get());
    }
}
