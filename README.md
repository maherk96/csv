```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class ServiceRunner implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ServiceRunner.class);

    private final Environment env;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Tracks running service threads
    private final Map<String, Future<?>> runningServices = new ConcurrentHashMap<>();

    // Tracks current status: "RUNNING" or "STOPPED"
    private final Map<String, String> serviceStates = new ConcurrentHashMap<>();

    private volatile boolean running = false;

    private final List<String> services = List.of(
        "fix-gateway", "exchange-manager", "md-feed", "order-processor", "price-manager"
    );

    public ServiceRunner(Environment env) {
        this.env = env;
        // Initialize all services as STOPPED
        services.forEach(service -> serviceStates.put(service, "STOPPED"));
    }

    /**
     * Automatically called when Spring starts the application.
     */
    @Override
    public void start() {
        log.info("Starting all services...");
        running = true;
        services.forEach(this::startService);
    }

    /**
     * Starts an individual service.
     */
    public void startService(String serviceName) {
        if (runningServices.containsKey(serviceName)) {
            log.warn("Service '{}' is already running.", serviceName);
            return;
        }

        Future<?> future = executorService.submit(() -> {
            try {
                log.info("Starting service: {}", serviceName);
                final var profile = env.getProperty("ambrosia.profile");
                final var serviceEnvironment = ServiceEnvironment.newEnvironment()
                    .withResources(profile).build();

                // Simulate blocking service run (replace with real logic)
                AmbrosiaConfig.run(serviceEnvironment, "services.yaml", serviceName);
                
                log.info("Service '{}' is running.", serviceName);
            } catch (Exception e) {
                log.error("Error starting service '{}'", serviceName, e);
            }
        });

        runningServices.put(serviceName, future);
        serviceStates.put(serviceName, "RUNNING");
        log.info("Service '{}' has been started.", serviceName);
    }

    /**
     * Stops an individual service.
     */
    public void stopService(String serviceName) {
        Future<?> future = runningServices.remove(serviceName);
        if (future == null) {
            log.warn("Service '{}' is not running.", serviceName);
            return;
        }

        future.cancel(true);
        serviceStates.put(serviceName, "STOPPED");
        log.info("Service '{}' has been stopped.", serviceName);
    }

    /**
     * Provides current status of all services.
     */
    public Map<String, String> getServiceStatus() {
        return new ConcurrentHashMap<>(serviceStates);
    }

    /**
     * Gracefully stop all services when Spring shuts down.
     */
    @Override
    public void stop() {
        log.info("Stopping all services...");
        running = false;
        runningServices.keySet().forEach(this::stopService);
        shutdownExecutor();
        log.info("All services stopped.");
    }

    /**
     * Stops services asynchronously (called by Spring).
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * Gracefully shuts down the ExecutorService.
     */
    private void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Forcing executor shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
```
