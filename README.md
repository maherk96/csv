```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class EventQueueTest {

    @Test
    void shouldAddAndRetrieveData() throws InterruptedException {
        EventQueue<String> queue = new EventQueue<>();

        queue.put("test-message");

        String result = queue.take();

        assertThat(result).isEqualTo("test-message");
    }

    @Test
    void shouldBlockUntilDataIsAvailable() throws InterruptedException {
        EventQueue<String> queue = new EventQueue<>();

        var executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                Thread.sleep(300); // Simulate delay
                queue.put("delayed-message");
            } catch (InterruptedException ignored) {}
        });

        long start = System.currentTimeMillis();
        String result = queue.take();
        long duration = System.currentTimeMillis() - start;

        assertThat(result).isEqualTo("delayed-message");
        assertThat(duration).isGreaterThanOrEqualTo(300);

        executor.shutdown();
    }

    @Test
    void shouldReturnNullIfInterruptedDuringPut() {
        EventQueue<String> queue = new EventQueue<>() {
            @Override
            public void put(String data) {
                Thread.currentThread().interrupt(); // Simulate interruption
                super.put(data);
            }
        };

        queue.put("interrupted-message"); // Should handle internally without crash
        // No exception expected
    }

    @Test
    void shouldReturnNullIfInterruptedDuringTake() {
        EventQueue<String> queue = new EventQueue<>() {
            @Override
            public String take() {
                Thread.currentThread().interrupt(); // Simulate interruption
                return super.take();
            }
        };

        String result = queue.take(); // Should return null gracefully
        assertThat(result).isNull();
    }
}
```
