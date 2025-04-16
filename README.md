```java
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.util.EventQueue;
import java.util.function.Consumer;
import java.util.concurrent.Executor;

public abstract class BaseTestDataProcessor<T> implements TestDataProcessor<T> {

    protected void doProcess(EventQueue<T> queue, Executor executor, Consumer<T> callback) {
        executor.execute(() -> {
            while (true) {
                try {
                    T launchData = queue.take();
                    if (launchData != null) {
                        callback.accept(launchData);
                    }
                } catch (Exception e) {
                    handleProcessingError(e);
                }
            }
        });
    }

    protected void handleProcessingError(Exception e) {
        System.err.println("Error processing data: " + e.getMessage());
        e.printStackTrace(); // Replace with actual logger
    }

    @PreDestroy
    public void shutdown(Executor executor) {
        if (executor instanceof ThreadPoolTaskExecutor) {
            ((ThreadPoolTaskExecutor) executor).shutdown();
        }
    }

    @Override
    public void processData() {
        throw new UnsupportedOperationException("processData() must be implemented by subclass.");
    }
}
```
