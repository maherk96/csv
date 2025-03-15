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
    private final Map<String, Future<?>> runningServices = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    private final List<String> services = List.of(
        "fix-gateway", "exchange-manager", "md-feed", "order-processor", "price-manager"
    );

    public ServiceRunner(Environment env) {
        this.env = env;
    }

    /**
     * Called automatically by Spring when the application starts.
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
                final var serviceEnvironment = ServiceEnvironment.newEnvironment()
                    .withResources(env.getProperty("ambrosia.profile")).build();
                AmbrosiaConfig.run(serviceEnvironment, "services.yaml", serviceName);
                log.info("Service '{}' is now running.", serviceName);
            } catch (Exception e) {
                log.error("Error starting service '{}'", serviceName, e);
            }
        });

        runningServices.put(serviceName, future);
    }

    /**
     * Stops all services gracefully.
     */
    @Override
    public void stop() {
        log.info("Stopping all services...");
        running = false;
        runningServices.keySet().forEach(this::stopService);
        shutdownExecutor();
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
        log.info("Service '{}' has been stopped.", serviceName);
    }

    /**
     * Checks if the lifecycle component is running.
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Ensures that this component starts automatically.
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Shutdown logic for the executor service.
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

    /**
     * Stops services asynchronously with a callback.
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * Returns the phase for SmartLifecycle components.
     */
    @Override
    public int getPhase() {
        return 0;
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceRunner serviceRunner;

    @PostMapping("/start/{serviceName}")
    public String startService(@PathVariable String serviceName) {
        serviceRunner.startService(serviceName);
        return "Service " + serviceName + " started.";
    }

    @PostMapping("/stop/{serviceName}")
    public String stopService(@PathVariable String serviceName) {
        serviceRunner.stopService(serviceName);
        return "Service " + serviceName + " stopped.";
    }

    @GetMapping("/status")
    public Map<String, Boolean> getServiceStatus() {
        return serviceRunner.getServiceStatus();
    }
}

```
