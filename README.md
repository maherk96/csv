```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        eventQueue = mock(EventQueue.class);
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
    void shouldProcessPayloadIfApiKeyIsValid() {
        // Given
        Map<String, String> props = new HashMap<>();
        props.put("app-name", "test-app");
        props.put("api-key", "valid-key");

        SamplePayload mockPayload = new SamplePayload("test");

        when(inboundMessage.getProperties()).thenReturn(props);
        when(featureConfig.isEnableApiKeyValidation()).thenReturn(true);
        when(apiKeyService.validateApiKey("test-app", "valid-key")).thenReturn(true);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            jsonUtilMock.when(() -> JsonUtil.isValid(anyString(), eq(SamplePayload.class))).thenReturn(true);
            jsonUtilMock.when(() -> JsonUtil.mapStringToDTO(anyString(), eq(SamplePayload.class)))
                        .thenReturn(mockPayload);

            // When
            processor.process("{\"value\":\"test\"}", inboundMessage);

            // Then
            verify(eventQueue).put(mockPayload);
        }
    }

    @Test
    void shouldProcessPayloadIfValidationIsDisabled() {
        // Given
        when(inboundMessage.getProperties()).thenReturn(new HashMap<>());
        when(featureConfig.isEnableApiKeyValidation()).thenReturn(false);

        SamplePayload mockPayload = new SamplePayload("test");

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            jsonUtilMock.when(() -> JsonUtil.isValid(anyString(), eq(SamplePayload.class))).thenReturn(true);
            jsonUtilMock.when(() -> JsonUtil.mapStringToDTO(anyString(), eq(SamplePayload.class)))
                        .thenReturn(mockPayload);

            // When
            processor.process("{\"value\":\"test\"}", inboundMessage);

            // Then
            verify(eventQueue).put(mockPayload);
        }
    }
}
```
