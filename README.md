```java
@Slf4j
@Component
public class EventPublisher {

    private MessagingService messagingService;
    private DirectMessagePublisher messagePublisher;

    @Autowired
    private SolaceConfigProperties configProperties;

    @PostConstruct
    public void initialize() {
        final var properties = setupPropertiesForConnection();

        messagingService = MessagingService.builder(ConfigurationProfile.V1)
            .fromProperties(properties)
            .build();
        messagingService.connect();

        messagePublisher = messagingService.createDirectMessagePublisherBuilder().build();
        messagePublisher.start();

        log.info("Solace publisher initialized.");
    }

    public void publish(String topic, String payload) {
        Topic publishTopic = Topic.of(topic);

        OutboundMessage message = messagingService.messageBuilder()
            .withApplicationMessageId(UUID.randomUUID().toString())
            .withPayload(payload)
            .build(publishTopic);

        messagePublisher.publish(message);
        log.info("Published message to topic '{}': {}", topic, payload);
    }

    private Properties setupPropertiesForConnection() {
        final var properties = new Properties();
        properties.setProperty(SolaceProperties.TransportLayerProperties.HOST, configProperties.getHostUrl());
        properties.setProperty(SolaceProperties.ServiceProperties.VPN_NAME, configProperties.getVpnName());
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME, configProperties.getUserName());
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD, configProperties.getPassword());
        properties.setProperty(SolaceProperties.TransportLayerProperties.RECONNECTION_ATTEMPTS, configProperties.getReconnectionAttempts());
        properties.setProperty(SolaceProperties.TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, configProperties.getConnectionRetriesPerHost());
        return properties;
    }

    @PreDestroy
    public void shutdown() {
        if (messagePublisher != null) {
            messagePublisher.terminate(1000);
        }
        if (messagingService != null) {
            messagingService.disconnect();
        }
        log.info("Solace publisher shutdown completed.");
    }
}
```
