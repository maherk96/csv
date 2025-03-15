```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ServiceRunner {
    private static final Logger log = LoggerFactory.getLogger(ServiceRunner.class);

    @Autowired
    private Environment env;

    // Thread pool for managing service startup
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void runServices() {
        var profile = env.getProperty("ambrosia.profile");
        assert profile != null;

        List<String> services = List.of("fix-gateway", "exchange-manager", "md-feed", "order-processor", "price-manager");

        services.forEach(service ->
            executorService.submit(() -> {
                try {
                    log.info("Starting service: {}", service);
                    final var serviceEnvironment = ServiceEnvironment.newEnvironment()
                        .withResources(profile).build();

                    // Start service (assuming AmbrosiaConfig.run() keeps it running)
                    AmbrosiaConfig.run(serviceEnvironment, "services.yaml", service);

                    log.info("Service started: {} and is now running indefinitely.", service);
                } catch (Exception e) {
                    log.error("Error starting service: {}", service, e);
                }
            })
        );

        log.info("All services have been triggered to start and will remain running.");
        
        // Register a shutdown hook to clean up on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down all services...");
            shutdownExecutor();
        }));
    }

    private void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Forcing executor shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
```
