```java
public class TestDataProcessorBase<T> implements TestDataProcessor<T> {

    protected void processData(EventQueue<T> queue, TaskExecutor executor, Consumer<T> callback) {
        executor.execute(() -> {
            while (true) {
                try {
                    T launchData = queue.take();
                    if (launchData != null) {
                        callback.accept(launchData);
                    }
                } catch (Exception e) {
                    // log error
                }
            }
        });
    }

    @PreDestroy
    public void shutdown(TaskExecutor executor) {
        if (executor instanceof ThreadPoolTaskExecutor) {
            ((ThreadPoolTaskExecutor) executor).shutdown();
        }
    }
}
```
