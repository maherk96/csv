```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

class AbstractEventProcessorTest {

    private ApiKeyService apiKeyService;
    private FeatureConfig featureConfig;
    private EventQueue<SamplePayload> eventQueue;
    private InboundMessage inboundMessage;

    private AbstractEventProcessor<SamplePayload> processor;

    @BeforeEach
    void setUp() {
        apiKeyService = mock(ApiKeyService.class);
        featureConfig = mock(FeatureConfig.class);
        eventQueue = spy(new EventQueue<>());
        inboundMessage = mock(InboundMessage.class);

        processor = new AbstractEventProcessor<>(apiKeyService, featureConfig) {
            @Override
            protected Class<SamplePayload> getPayloadClass() {
                return SamplePayload.class;
            }

            @Override
            protected EventQueue<SamplePayload> getQueue() {
                return eventQueue;
            }
        };
    }

    @Test
    void shouldSkipProcessingIfApiKeyIsInvalid() {
        Map<String, String> props = new HashMap<>();
        props.put("app-name", "test-app");
        props.put("api-key", "invalid-key");

        when(inboundMessage.getProperties()).thenReturn(props);
        when(featureConfig.isEnableApiKeyValidation()).thenReturn(true);
        when(apiKeyService.validateApiKey("test-app", "invalid-key")).thenReturn(false);

        processor.process("{\"value\":\"test\"}", inboundMessage);

        verify(eventQueue, never()).put(any());
    }

    @Test
    void shouldProcessPayloadIfApiKeyIsValid() {
        Map<String, String> props = new HashMap<>();
        props.put("app-name", "test-app");
        props.put("api-key", "valid-key");

        when(inboundMessage.getProperties()).thenReturn(props);
        when(featureConfig.isEnableApiKeyValidation()).thenReturn(true);
        when(apiKeyService.validateApiKey("test-app", "valid-key")).thenReturn(true);

        processor.process("{\"value\":\"test\"}", inboundMessage);

        verify(eventQueue).put(any(SamplePayload.class));
    }

    @Test
    void shouldProcessPayloadIfValidationIsDisabled() {
        Map<String, String> props = new HashMap<>();
        when(inboundMessage.getProperties()).thenReturn(props);
        when(featureConfig.isEnableApiKeyValidation()).thenReturn(false);

        processor.process("{\"value\":\"test\"}", inboundMessage);

        verify(eventQueue).put(any(SamplePayload.class));
    }

    // You can also assert logging or invalid payload behavior if needed
}
```
