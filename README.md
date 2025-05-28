```java

public class SolaceTestDataProducer {
    private static final Logger log = LoggerFactory.getLogger(SolaceTestDataProducer.class);
    private static final String SYSTEM_PROPERTY_LAUNCH_ID = "launchID";

    private static SolaceTestDataProducer instance;
    private final boolean enableLogging;
    private SolaceMessageProducer producer;

    private SolaceTestDataProducer(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public static synchronized SolaceTestDataProducer getInstance(boolean enableLogging) {
        if (instance == null) {
            instance = new SolaceTestDataProducer(enableLogging);
        }
        return instance;
    }

    public synchronized void initConnection(String host, String username, String password, String vpnname) {
        if (producer == null) {
            this.producer = new SolaceMessageProducer(host, username, password, vpnname);
        }
    }

    public void send(String topicName, String messageContent) {
        if (producer == null) {
            throw new IllegalStateException("SolaceMessageProducer not initialized.");
        }

        try {
            producer.send(topicName, new Message(messageContent));
        } catch (MessageProducerException e) {
            throw new RuntimeException(e);
        }

        if (enableLogging) {
            log.info("Sent to QAP reporting service: {} -> {}", 
                System.getProperty(SYSTEM_PROPERTY_LAUNCH_ID), topicName);
        }
    }
}




public class TestDataPublisher {

    private final SolaceConfigLoader configLoader;
    private final boolean loggingEnabled;

    public TestDataPublisher(boolean loggingEnabled) {
        this.configLoader = new SolaceConfigLoader("solace-config.yaml");
        this.loggingEnabled = loggingEnabled;
    }

    public static <T> void publishTestData(
        RunEnvironment runEnvironment, T data, boolean loggingEnabled) {
        new TestDataPublisher(loggingEnabled).publish(runEnvironment, data);
    }

    public <T> void publish(RunEnvironment runEnv, T reportData) {
        SolaceConfig solaceConfig;
        switch (runEnv) {
            case UAT -> solaceConfig = configLoader.getDefaultConnection();
            case UAT_ETZ -> solaceConfig = configLoader.getConnection(RunEnvironment.UAT_ETZ);
            case DEV -> solaceConfig = configLoader.getConnection(RunEnvironment.DEV);
            default -> throw new UnsupportedOperationException("Invalid environment: " + runEnv);
        }

        SolaceTestDataProducer producer = SolaceTestDataProducer.getInstance(loggingEnabled);
        producer.initConnection(
            solaceConfig.getHost(),
            solaceConfig.getUsername(),
            solaceConfig.getPassword(),
            solaceConfig.getVpnname()
        );

        producer.send(solaceConfig.getTopic(), JsonUtil.writeValueAsString(reportData));
    }
}
```
