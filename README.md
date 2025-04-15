```java
/**
 * Generic abstract processor for handling event payloads from Solace messages.
 * 
 * @param <T> the type of event payload to process
 */
@Slf4j
public abstract class AbstractEventProcessor<T> {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private FeatureConfig featureConfig;

    /**
     * Returns the class type of the payload for JSON deserialization.
     * 
     * @return the payload class type
     */
    protected abstract Class<T> getPayloadClass();

    /**
     * Returns the queue to which valid deserialized payloads will be added.
     * 
     * @return the event queue
     */
    protected abstract EventQueue<T> getQueue();

    /**
     * Processes an inbound Solace message: validates API key if required,
     * deserializes the payload to the specified type, and adds it to the queue.
     * 
     * @param payload         the message payload as a JSON string
     * @param inboundMessage  the original inbound message object with metadata
     */
    public void process(String payload, InboundMessage inboundMessage) {
        final var appName = inboundMessage.getProperties().get("app-name");
        final var apiKey = inboundMessage.getProperties().get("api-key");

        if (featureConfig.isEnableApiKeyValidation()
                && (appName == null || apiKey == null
                || !apiKeyService.validateApiKey(appName, apiKey))) {
            log.warn("Invalid or missing API key for message from app: {}", appName);
            return;
        }

        log.info("Processing incoming payload: {}", inboundMessage.getDestinationName());

        if (JsonUtil.isValid(payload, getPayloadClass())) {
            T parsed = JsonUtil.mapStringToDTO(payload, getPayloadClass());
            getQueue().put(parsed);
        }
    }
}

/**
 * Processor for JUnit-based events received from Solace messaging service.
 * Extends {@link AbstractEventProcessor} to provide queue and type information.
 */
@Component
public class JunitEventProcessor extends AbstractEventProcessor<QAPJunitLaunch> {

    @Autowired
    private EventQueue<QAPJunitLaunch> junitQueue;

    /**
     * Returns the class of {@link QAPJunitLaunch} for JSON mapping.
     */
    @Override
    protected Class<QAPJunitLaunch> getPayloadClass() {
        return QAPJunitLaunch.class;
    }

    /**
     * Returns the queue instance used to hold parsed {@link QAPJunitLaunch} objects.
     */
    @Override
    protected EventQueue<QAPJunitLaunch> getQueue() {
        return junitQueue;
    }
}

/**
 * Processor for Cucumber-based events received from Solace messaging service.
 * Extends {@link AbstractEventProcessor} to provide queue and type information.
 */
@Component
public class CucumberEventProcessor extends AbstractEventProcessor<QAPCucumberLaunch> {

    @Autowired
    private EventQueue<QAPCucumberLaunch> cucumberQueue;

    /**
     * Returns the class of {@link QAPCucumberLaunch} for JSON mapping.
     */
    @Override
    protected Class<QAPCucumberLaunch> getPayloadClass() {
        return QAPCucumberLaunch.class;
    }

    /**
     * Returns the queue instance used to hold parsed {@link QAPCucumberLaunch} objects.
     */
    @Override
    protected EventQueue<QAPCucumberLaunch> getQueue() {
        return cucumberQueue;
    }
}

```
