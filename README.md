```java
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class QAPCucumberEventPublisherTest {

    @Test
    void shouldPublishEventToTestDataPublisher() {
        // Arrange
        QAPCucumberLaunch mockLaunch = mock(QAPCucumberLaunch.class);
        QAPPropertiesLoader mockProps = mock(QAPPropertiesLoader.class);

        when(mockProps.getRunEnvironment()).thenReturn("UAT");

        QAPCucumberEventPublisher publisher = new QAPCucumberEventPublisher();

        try (MockedStatic<TestDataPublisher> mockStatic = mockStatic(TestDataPublisher.class)) {

            // Act
            publisher.publishEvent(mockLaunch, mockProps);

            // Assert
            mockStatic.verify(() -> TestDataPublisher.publishTestData(RunEnvironment.UAT, mockLaunch));
        }
    }
}
```
