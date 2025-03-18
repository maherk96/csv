```java
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class for message senders that require scheduled concurrent execution.
 * Handles scheduling logic for sending market data snapshots at regular intervals.
 */
@Slf4j
public abstract class AbstractConcurrentMessageSender implements MessageSender {
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Sends market data immediately.
     * Subclasses must implement this to define the actual messaging transport logic.
     *
     * @param snapshot The market data snapshot to send
     */
    protected abstract void sendMessage(DspMarketDataSnapShot snapshot);

    @Override
    public void sendMarketData(DspMarketDataSnapShot snapshot) {
        CompletableFuture.runAsync(() -> sendMessage(snapshot));
    }

    @Override
    public void startScheduledSending(DspMarketDataSnapShot snapshot, long intervalSeconds) {
        String key = snapshot.getSymbol();

        lock.lock();
        try {
            stopScheduledSending(key); // Stop any existing schedule for this symbol

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> sendMessage(snapshot),
                    0, intervalSeconds, TimeUnit.SECONDS);

            scheduledTasks.put(key, future);
            log.info("Started scheduled sending for {} every {} seconds", key, intervalSeconds);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates the interval of a scheduled snapshot.
     *
     * @param symbol The currency pair symbol
     * @param newIntervalSeconds The new interval in seconds
     */
    public void updateScheduledInterval(String symbol, long newIntervalSeconds) {
        stopScheduledSending(symbol);
        log.info("Updating interval for {} to {} seconds", symbol, newIntervalSeconds);
        DspMarketDataSnapShot snapshot = getLastSnapshotForSymbol(symbol);
        startScheduledSending(snapshot, newIntervalSeconds);
    }

    /**
     * Stops a scheduled snapshot for a specific symbol.
     *
     * @param symbol The currency pair symbol
     */
    public void stopScheduledSending(String symbol) {
        lock.lock();
        try {
            ScheduledFuture<?> future = scheduledTasks.remove(symbol);
            if (future != null) {
                future.cancel(false);
                log.info("Stopped scheduled sending for {}", symbol);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops all scheduled snapshots.
     */
    public void stopAllScheduledSending() {
        lock.lock();
        try {
            scheduledTasks.values().forEach(future -> future.cancel(false));
            scheduledTasks.clear();
            log.info("Stopped all scheduled tasks.");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the last sent snapshot for a given symbol.
     * Subclasses may override this to provide actual storage or caching logic.
     *
     * @param symbol The currency pair symbol
     * @return The last known snapshot (default implementation returns a new empty snapshot)
     */
    protected DspMarketDataSnapShot getLastSnapshotForSymbol(String symbol) {
        // In a real implementation, retrieve this from a cache or database
        return new DspMarketDataSnapShot();
    }
}
```
