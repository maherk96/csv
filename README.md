```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class ServiceRunner {
    private static final Logger log = LoggerFactory.getLogger(ServiceRunner.class);

    @Autowired
    private Environment env;

    // Thread pool for running services
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Track running services
    private final Map<String, Future<?>> runningServices = new ConcurrentHashMap<>();

    private static final List<String> SERVICES = List.of(
            "fix-gateway", "exchange-manager", "md-feed", "order-processor", "price-manager"
    );

    @PostConstruct
    public void startAllServices() {
        log.info("Starting all services by default...");
        SERVICES.forEach(this::startService);
    }

    public void startService(String serviceName) {
        if (runningServices.containsKey(serviceName)) {
            log.warn("Service {} is already running.", serviceName);
            return;
        }

        Future<?> future = executorService.submit(() -> {
            try {
                log.info("Starting service: {}", serviceName);
                var profile = env.getProperty("ambrosia.profile");
                assert profile != null;

                var serviceEnvironment = ServiceEnvironment.newEnvironment()
                        .withResources(profile).build();

                // Run service (assuming it is a blocking call)
                AmbrosiaConfig.run(serviceEnvironment, "services.yaml", serviceName);
                
                log.info("Service {} is now running indefinitely.", serviceName);
            } catch (Exception e) {
                log.error("Error starting service {}: ", serviceName, e);
            }
        });

        runningServices.put(serviceName, future);
    }

    public void stopService(String serviceName) {
        Future<?> future = runningServices.remove(serviceName);
        if (future == null) {
            log.warn("Service {} is not running.", serviceName);
            return;
        }

        // Cancel the service execution
        future.cancel(true);
        log.info("Service {} has been stopped.", serviceName);
    }

    public Map<String, Boolean> getServiceStatus() {
        Map<String, Boolean> status = new ConcurrentHashMap<>();
        runningServices.forEach((service, future) -> status.put(service, !future.isCancelled()));
        return status;
    }

    @PreDestroy
    public void shutdownAllServices() {
        log.info("Shutting down all services...");
        runningServices.keySet().forEach(this::stopService);
        executorService.shutdown();
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

    @PostMapping("/stop/{serviceName}")
    public String stopService(@PathVariable String serviceName) {
        serviceRunner.stopService(serviceName);
        return "Service " + serviceName + " stopped.";
    }

    @PostMapping("/start/{serviceName}")
    public String startService(@PathVariable String serviceName) {
        serviceRunner.startService(serviceName);
        return "Service " + serviceName + " started.";
    }

    @GetMapping("/status")
    public Map<String, Boolean> getServiceStatus() {
        return serviceRunner.getServiceStatus();
    }
}

```
