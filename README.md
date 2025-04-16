```java

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class TestDataPublisherTest {

    private SolaceConfigLoader mockLoader;
    private SolaceConfig mockConfig;

    @BeforeEach
    void setup() {
        mockLoader = mock(SolaceConfigLoader.class);
        mockConfig = mock(SolaceConfig.class);

        when(mockConfig.getHost()).thenReturn("localhost");
        when(mockConfig.getUsername()).thenReturn("user");
        when(mockConfig.getPassword()).thenReturn("pass");
        when(mockConfig.getVpnname()).thenReturn("vpn");
        when(mockConfig.getTopic()).thenReturn("topic");
    }

    @Test
    void shouldSendDataToUatEnvironment() {
        // Arrange
        when(mockLoader.getDefaultConnection()).thenReturn(mockConfig);
        TestDataPublisher publisher = new TestDataPublisher(mockLoader);

        try (
            MockedStatic<SolaceTestDataProducer> producerMock = mockStatic(SolaceTestDataProducer.class);
            MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)
        ) {
            SolaceTestDataProducer producer = mock(SolaceTestDataProducer.class);
            producerMock.when(SolaceTestDataProducer::getInstance).thenReturn(producer);

            jsonUtilMock.when(() -> JsonUtil.writeValueAsString("payload")).thenReturn("{\"mocked\":\"json\"}");

            // Act
            publisher.publish(RunEnvironment.UAT, "payload");

            // Assert
            verify(producer).send(
                eq("localhost"),
                eq("user"),
                eq("pass"),
                eq("vpn"),
                eq("topic"),
                eq("{\"mocked\":\"json\"}")
            );
        }
    }
}
```
