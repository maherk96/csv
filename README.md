```java
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListDuplicateCheckerAwaitility {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Logger logger = Logger.getLogger(ListDuplicateCheckerAwaitility.class.getName());
    private final List<Future<?>> runningTasks = new CopyOnWriteArrayList<>();

    /**
     * Checks for duplicates in the supplied list in the background using Awaitility.
     * Allows flexible handling via a Consumer.
     *
     * @param listSupplier        Supplies the list to check.
     * @param uniqueKeyExtractor  Extracts the unique key from each element.
     * @param onDuplicatesFound   Consumer to handle duplicates (logging, assertion failure, etc.).
     * @param <T>                 Type of elements in the list.
     * @param <R>                 Type of unique key (e.g., String, Integer).
     */
    public <T, R> void checkForDuplicatesWithAwaitility(
            Supplier<List<T>> listSupplier,
            Function<T, R> uniqueKeyExtractor,
            Consumer<List<T>> onDuplicatesFound) {

        Future<?> task = executorService.submit(() -> {
            Set<R> seen = new HashSet<>();

            Awaitility.await()
                    .atMost(10, TimeUnit.SECONDS)  // Max wait time
                    .pollInterval(Duration.ofSeconds(1))  // Check every second
                    .until(() -> {
                        List<T> list = listSupplier.get(); // Always get the latest list
                        List<T> duplicates = list.stream()
                                .filter(e -> !seen.add(uniqueKeyExtractor.apply(e))) // Check uniqueness
                                .collect(Collectors.toList());

                        if (!duplicates.isEmpty()) {
                            logger.warning("Duplicates found: " + duplicates);
                            onDuplicatesFound.accept(duplicates); // Delegate handling to the user
                            return true;  // Stop polling
                        }
                        return false; // Keep polling
                    });

        });

        runningTasks.add(task);
    }

    /**
     * Cancels all running duplicate checks.
     */
    public void cancelChecks() {
        for (Future<?> task : runningTasks) {
            task.cancel(true);
        }
        runningTasks.clear();
    }

    /**
     * Gracefully shuts down the executor service.
     */
    public void shutdown() {
        cancelChecks();
        executorService.shutdown();
    }
}
```
