```java
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for managing scheduled concurrent execution of message sending.
 * Used via composition by message senders like TibrvMessageSender and SolaceMessageSender.
 */
@Slf4j
public class ConcurrentMessageScheduler {
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Schedules a market data snapshot to be sent periodically.
     *
     * @param snapshot The market data snapshot
     * @param intervalSeconds The interval (in seconds) between sends
     * @param sender The message sender that will send the snapshot
     */
    public void startScheduledSending(DspMarketDataSnapShot snapshot, long intervalSeconds, MessageSender sender) {
        String key = snapshot.getSymbol();

        lock.lock();
        try {
            stopScheduledSending(key); // Stop existing schedule if needed

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> sender.sendMarketData(snapshot),
                    0, intervalSeconds, TimeUnit.SECONDS);

            scheduledTasks.put(key, future);
            log.info("Started scheduled sending for {} every {} seconds", key, intervalSeconds);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops scheduled sending for a specific symbol.
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
     * Stops all scheduled market data snapshots.
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
}
```
