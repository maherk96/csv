```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * ServiceRunner is responsible for managing the lifecycle of multiple services.
 * It starts all services automatically at application startup and provides 
 * mechanisms to start, stop, and monitor individual services dynamically.
 * <p>
 * Implements {@link SmartLifecycle} to ensure services are properly initialized and 
 * gracefully shutdown when the Spring context stops.
 * </p>
 */
@Component
public class ServiceRunner implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ServiceRunner.class);
    private static final int THREAD_POOL_SIZE = 5;
    private static final String LOGS_DIR = "logs";

    private final Environment env;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Map<String, Future<?>> runningServices = new ConcurrentHashMap<>();
    private final Map<String, String> serviceStates = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    private final List<String> services = List.of(
            "fix-gateway", "exchange-manager", "md-feed", "order-processor", "price-manager"
    );

    /**
     * Constructor to initialize the service runner.
     * Ensures the logs directory exists and initializes service states.
     *
     * @param env The Spring environment configuration.
     */
    public ServiceRunner(Environment env) {
        this.env = env;
        ensureLogDirectoryExists();
        services.forEach(service -> serviceStates.put(service, "STOPPED"));
    }

    /**
     * Ensures that the logs directory exists to avoid issues with logging.
     */
    private void ensureLogDirectoryExists() {
        File logDir = new File(LOGS_DIR);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                log.info("Created logs directory at {}", logDir.getAbsolutePath());
            } else {
                log.warn("Failed to create logs directory. Ensure it exists.");
            }
        }
    }

    /**
     * Starts all services automatically when the application starts.
     * This method is invoked by Spring as part of the {@link SmartLifecycle} interface.
     */
    @Override
    public void start() {
        log.info("Starting all services...");
        running = true;
        services.forEach(this::startService);
    }

    /**
     * Starts an individual service asynchronously.
     *
     * @param serviceName The name of the service to start.
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

                // Execute the service (blocking call)
                AmbrosiaConfig.run(serviceEnvironment, "services.yaml", serviceName);

                log.info("Service '{}' is now running.", serviceName);
            } catch (InterruptedException e) {
                log.warn("Service '{}' was manually stopped (Interrupted).", serviceName);
                Thread.currentThread().interrupt();
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
     *
     * @param serviceName The name of the service to stop.
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
     * Returns the current status of all services.
     *
     * @return A map where the key is the service name and the value is "RUNNING" or "STOPPED".
     */
    public Map<String, String> getServiceStatus() {
        return new ConcurrentHashMap<>(serviceStates);
    }

    /**
     * Stops all running services when the application is shutting down.
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
     * Called by Spring when the application shuts down asynchronously.
     * Ensures all services are stopped before continuing.
     *
     * @param callback A callback to signal when shutdown is complete.
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * Checks whether this lifecycle component is currently running.
     *
     * @return {@code true} if the service is running, otherwise {@code false}.
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Determines if the service should start automatically when the application starts.
     *
     * @return {@code true}, indicating automatic startup.
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Defines the phase of this component in the Spring lifecycle.
     *
     * @return The phase order, where lower values start earlier and stop later.
     */
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
