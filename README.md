```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for publishing data to the Solace message broker.
 * Handles common publishing logic across various environments and configurations.
 */
public abstract class AbstractDataPublisher {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDataPublisher.class);

    protected final SolaceConfigLoader configLoader;
    protected final boolean loggingEnabled;

    /**
     * Constructs a new AbstractDataPublisher.
     *
     * @param configFileName   the YAML configuration file to load
     * @param loggingEnabled   whether to enable detailed logging of sent messages
     */
    protected AbstractDataPublisher(String configFileName, boolean loggingEnabled) {
        this.configLoader = new SolaceConfigLoader(configFileName);
        this.loggingEnabled = loggingEnabled;
        logger.info("Initialized {} with config: {} and loggingEnabled: {}",
                this.getClass().getSimpleName(), configFileName, loggingEnabled);
    }

    /**
     * Publishes the given report data to the Solace message broker without an ID suffix.
     *
     * @param runEnv     the run environment (e.g., UAT, DEV)
     * @param reportData the data to be published
     * @param <T>        the type of the report data
     */
    public <T> void publish(RunEnvironment runEnv, T reportData) {
        publish(runEnv, reportData, null);
    }

    /**
     * Publishes the given report data to the Solace message broker with optional topic ID suffix replacement.
     *
     * @param runEnv     the run environment (e.g., UAT, DEV)
     * @param reportData the data to be published
     * @param idSuffix   the suffix to append if topic contains "/eid"
     * @param <T>        the type of the report data
     */
    public <T> void publish(RunEnvironment runEnv, T reportData, String idSuffix) {
        SolaceConfig solaceConfig;
        try {
            switch (runEnv) {
                case UAT -> solaceConfig = configLoader.getDefaultConnection();
                case UAT_ETZ -> solaceConfig = configLoader.getConnection(RunEnvironment.UAT_ETZ);
                case DEV -> solaceConfig = configLoader.getConnection(RunEnvironment.DEV);
                default -> throw new UnsupportedOperationException("Invalid environment: " + runEnv);
            }
        } catch (Exception e) {
            logger.error("Failed to get Solace config for environment: {}", runEnv, e);
            throw e;
        }

        if (idSuffix != null && solaceConfig.getTopic().contains("/eid")) {
            if (idSuffix.isBlank()) {
                logger.warn("idSuffix was blank while topic contained /eid");
                throw new IllegalArgumentException("idSuffix cannot be null or blank when topic contains /eid");
            }
            solaceConfig.setTopic(solaceConfig.getTopic().replace("/eid", "/" + idSuffix));
            logger.debug("Updated topic with idSuffix: {}", solaceConfig.getTopic());
        }

        SolaceTestDataProducer producer = SolaceTestDataProducer.getInstance(loggingEnabled);
        producer.initConnection(solaceConfig.getHost(), solaceConfig.getUsername(),
                solaceConfig.getPassword(), solaceConfig.getVpnname());

        try {
            String payload = JsonUtil.writeValueAsString(reportData);
            logger.info("Publishing message to topic: {}", solaceConfig.getTopic());
            if (loggingEnabled) {
                logger.debug("Payload: {}", payload);
            }
            producer.send(solaceConfig.getTopic(), payload);
        } catch (Exception e) {
            logger.error("Failed to send message to topic: {}", solaceConfig.getTopic(), e);
            throw e;
        }
    }
}

/**
 * A publisher used to send logs data to the Solace message broker.
 * Loads configuration from 'solace-logs-config.yaml'.
 */
public class LogsDataPublisher extends AbstractDataPublisher {

    /**
     * Constructs a LogsDataPublisher with logging control.
     *
     * @param loggingEnabled whether to enable logging of message contents
     */
    public LogsDataPublisher(boolean loggingEnabled) {
        super("solace-logs-config.yaml", loggingEnabled);
    }

    /**
     * Publishes test log data to the Solace message broker using the specified run environment.
     *
     * @param runEnvironment the environment to target (UAT, DEV, etc.)
     * @param data           the log data to send
     * @param loggingEnabled whether to log the message payload
     * @param idSuffix       suffix to replace "/eid" in the topic if required
     * @param <T>            type of the data
     */
    public static <T> void publishTestData(RunEnvironment runEnvironment, T data, boolean loggingEnabled, String idSuffix) {
        new LogsDataPublisher(loggingEnabled).publish(runEnvironment, data, idSuffix);
    }
}

/**
 * A publisher used to send test data to the Solace message broker.
 * Loads configuration from 'solace-config.yaml'.
 */
public class TestDataPublisher extends AbstractDataPublisher {

    /**
     * Constructs a TestDataPublisher with logging control.
     *
     * @param loggingEnabled whether to enable logging of message payloads
     */
    public TestDataPublisher(boolean loggingEnabled) {
        super("solace-config.yaml", loggingEnabled);
    }

    /**
     * Publishes test data to the Solace message broker using the specified run environment.
     *
     * @param runEnvironment the environment to target (UAT, DEV, etc.)
     * @param data           the test data to send
     * @param loggingEnabled whether to log the message payload
     * @param <T>            type of the data
     */
    public static <T> void publishTestData(RunEnvironment runEnvironment, T data, boolean loggingEnabled) {
        new TestDataPublisher(loggingEnabled).publish(runEnvironment, data);
    }
}
```
