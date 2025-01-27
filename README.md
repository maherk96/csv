# CSV Parser

## Overview
This project provides a robust and flexible CSV parsing library for Java. It allows converting CSV files into Java objects with various configurable options like custom delimiters, header mappings, error handling strategies, and support for built-in or custom type converters.

---

## Features
- Parse CSV files into Java objects.
- Support for custom delimiters (e.g., `,`, `|`, etc.).
- Flexible header-to-field mapping for case-insensitivity or custom column mapping.
- Options to:
    - Skip empty lines.
    - Trim whitespace from fields.
    - Handle unknown columns.
- Error handling strategies:
    - Continue on error.
    - Halt on error.
    - Collect errors.
- Built-in type converters for common data types.
- Support for custom type converters for complex or user-defined types.

---

## Usage

### Example CSV File
```csv
currency pair,bid low price,bid upper price,offer low price,offer upper price,num. of rungs bid,num. of rungs offer
EUR/USD,1.1,1.2,1.3,1.4,5,6
GBP/USD,1.5,1.6,1.7,1.8,7,8
```

### Parsing CSV to Java Objects
```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withDelimiter(",") // Specify delimiter
    .withTrimFields(true) // Trim fields
    .withHeaderMapping(Map.of(
        "currency pair", "currencyPair",
        "bid low price", "bidLowPrice",
        "bid upper price", "bidUpperPrice",
        "offer low price", "offerLowPrice",
        "offer upper price", "offerUpperPrice",
        "num. of rungs bid", "numOfRungsBid",
        "num. of rungs offer", "numOfRungsOffer"
    ))
    .build();

List<CurrencyPair> currencyPairs = CSVParser.parse(csvFile, config);
```

---

## Configuration Options

### Delimiters
- **Default**: `,`
- Use a custom delimiter when your CSV file uses a non-standard character like `|` or `;`.
  ```java
  config.withDelimiter("|");
  ```

### Trimming Fields
- **Default**: `true`
- Enable trimming to remove extra spaces around field values:
  ```java
  config.withTrimFields(true);
  ```

### Skip Empty Lines
- **Default**: `true`
- Skip blank rows in the CSV file:
  ```java
  config.withSkipEmptyLines(true);
  ```

### Ignore Unknown Columns
- **Default**: `false`
- Use when your CSV has extra columns not mapped to fields in the target class:
  ```java
  config.withIgnoreUnknownColumns(true);
  ```

### Error Handling Strategies
1. **CONTINUE_ON_ERROR**: Logs errors and skips problematic rows.
   ```java
   config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.CONTINUE_ON_ERROR);
   ```
2. **HALT_ON_ERROR**: Stops parsing immediately upon encountering an error.
   ```java
   config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.HALT_ON_ERROR);
   ```
3. **COLLECT_ERRORS**: Collects all errors and logs them at the end.
   ```java
   config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.COLLECT_ERRORS);
   ```

---

## Type Conversion

### Built-in Converters
The library supports the following data types by default:
- **Primitive types**: `int`, `double`, `boolean`, etc.
- **Wrapper types**: `Integer`, `Double`, `Boolean`, etc.
- **Common classes**: `String`, `BigDecimal`, `LocalDate`, `LocalDateTime`

### Custom Converters
Use a custom converter for:
- Custom or user-defined types.
- Complex transformations like parsing custom date formats or converting text into objects.

Example:
```java
TypeConverter.registerConverter(MyCustomType.class, MyCustomType::fromString);

CSVParserConfig<MyCustomType> config = new CSVParserConfig.Builder<>(MyCustomType.class).build();
```

---

## Advanced Usage

### Parsing a CSV with Custom Delimiter
```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withDelimiter("|") // Use '|' as the delimiter
    .build();

List<CurrencyPair> currencyPairs = CSVParser.parse(customDelimiterFile, config);
```

### Handling Empty Lines
```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withSkipEmptyLines(true) // Ignore empty rows
    .build();
```

### Custom Header Mapping
Map CSV headers to Java fields:
```java
config.withHeaderMapping(Map.of(
    "currency pair", "currencyPair",
    "bid low price", "bidLowPrice"
));
```








```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@DisplayName("StompClient Tests")
class StompClientTest {

    private static final String WEBSOCKET_URI = "ws://localhost:8080/websocket";
    private static final String TEST_TOPIC = "/topic/test";

    @Mock
    private StompSession mockStompSession;
    
    @Mock
    private StompClient.MessageHandler mockMessageHandler;
    
    @Mock
    private StompClient.StompConnectionStateListener mockStateListener;
    
    @Mock
    private WebSocketStompClient mockWebSocketStompClient;

    @Captor
    private ArgumentCaptor<StompFrameHandler> frameHandlerCaptor;
    
    @Captor
    private ArgumentCaptor<String> topicCaptor;

    private StompClient stompClient;
    private CompletableFuture<StompSession> sessionFuture;

    @BeforeEach
    void setUp() {
        sessionFuture = new CompletableFuture<>();
        given(mockStompSession.isConnected()).willReturn(true);
        
        stompClient = new StompClient.Builder()
            .websocketUri(WEBSOCKET_URI)
            .addTopic(TEST_TOPIC)
            .messageHandler(mockMessageHandler)
            .connectionStateListener(mockStateListener)
            .build();
    }

    @Nested
    @DisplayName("Connection Tests")
    class ConnectionTests {
        
        @Test
        @DisplayName("Should connect successfully")
        void shouldConnectSuccessfully() throws ExecutionException, InterruptedException, TimeoutException {
            // Arrange
            sessionFuture.complete(mockStompSession);
            given(mockWebSocketStompClient.connectAsync(eq(WEBSOCKET_URI), any(StompSessionHandler.class)))
                .willReturn(sessionFuture);

            // Act
            CompletableFuture<Void> connectFuture = stompClient.connectAsync();
            connectFuture.get(5, TimeUnit.SECONDS);

            // Assert
            then(mockStateListener).should().onConnected();
            assertTrue(stompClient.isConnected());
        }

        @Test
        @DisplayName("Should handle connection failure")
        void shouldHandleConnectionFailure() {
            // Arrange
            RuntimeException testException = new RuntimeException("Connection failed");
            sessionFuture.completeExceptionally(testException);
            given(mockWebSocketStompClient.connectAsync(eq(WEBSOCKET_URI), any(StompSessionHandler.class)))
                .willReturn(sessionFuture);

            // Act
            CompletableFuture<Void> connectFuture = stompClient.connectAsync();

            // Assert
            then(mockStateListener).should(timeout(5000)).onReconnecting(1);
        }

        @Test
        @DisplayName("Should timeout on slow connection")
        void shouldTimeoutOnSlowConnection() {
            // Arrange
            given(mockWebSocketStompClient.connectAsync(any(), any()))
                .willReturn(new CompletableFuture<>()); // Never completes

            // Act & Assert
            assertTimeout(Duration.ofSeconds(2), () -> {
                CompletableFuture<Void> future = stompClient.connectAsync();
                // Don't wait for the future to complete
            });
        }
    }

    @Nested
    @DisplayName("Message Handling Tests")
    class MessageHandlingTests {
        
        @Test
        @DisplayName("Should handle message correctly")
        void shouldHandleMessageCorrectly() {
            // Arrange
            String payload = "Test message";
            StompHeaders headers = new StompHeaders();
            headers.setDestination(TEST_TOPIC);

            // Act
            stompClient.handleFrame(headers, payload);

            // Assert
            then(mockMessageHandler).should().handleMessage(TEST_TOPIC, payload);
            then(mockMessageHandler).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should handle message handler exception")
        void shouldHandleMessageHandlerException() {
            // Arrange
            String payload = "Test message";
            willThrow(new RuntimeException("Handler error"))
                .given(mockMessageHandler)
                .handleMessage(any(), any());

            // Act
            stompClient.handleFrame(new StompHeaders(), payload);

            // Assert
            then(mockMessageHandler).should().handleMessage(any(), eq(payload));
            then(mockStateListener).should(never()).onError(any());
        }
    }

    @Nested
    @DisplayName("Topic Management Tests")
    class TopicManagementTests {
        
        @Test
        @DisplayName("Should handle topic subscription")
        void shouldHandleTopicSubscription() {
            // Arrange
            String newTopic = "/topic/new";
            
            // Act
            stompClient.addTopic(newTopic);

            // Assert
            then(mockStompSession).should(times(2))
                .subscribe(topicCaptor.capture(), frameHandlerCaptor.capture());
            
            List<String> capturedTopics = topicCaptor.getAllValues();
            assertTrue(capturedTopics.contains(newTopic));
        }

        @Test
        @DisplayName("Should handle topic unsubscription")
        void shouldHandleTopicUnsubscription() {
            // Act
            stompClient.removeTopic(TEST_TOPIC);

            // Assert
            then(mockStateListener).should(never()).onError(any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should attempt reconnection")
        void shouldAttemptReconnection() {
            // Arrange
            RuntimeException testException = new RuntimeException("Transport error");

            // Act
            stompClient.handleTransportError(mockStompSession, testException);

            // Assert
            then(mockStateListener).should().onReconnecting(1);
            then(mockStateListener).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should handle max reconnection attempts")
        void shouldHandleMaxReconnectionAttempts() {
            // Arrange
            RuntimeException testException = new RuntimeException("Transport error");

            // Act
            for (int i = 0; i < 6; i++) {
                stompClient.handleTransportError(mockStompSession, testException);
            }

            // Assert
            then(mockStateListener).should().onConnectionFailed(any(RuntimeException.class));
        }

        @Test
        @DisplayName("Should handle exceptions")
        void shouldHandleExceptions() {
            // Arrange
            StompCommand command = StompCommand.SEND;
            StompHeaders headers = new StompHeaders();
            byte[] payload = "Test".getBytes();
            RuntimeException testException = new RuntimeException("Test exception");

            // Act
            stompClient.handleException(mockStompSession, command, headers, payload, testException);

            // Assert
            then(mockStateListener).should().onError(testException);
            then(mockStateListener).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("Builder Validation Tests")
    class BuilderValidationTests {
        
        @Test
        @DisplayName("Should validate builder parameters")
        void shouldValidateBuilderParameters() {
            // Assert
            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> 
                    new StompClient.Builder().build(),
                    "Should throw exception for empty builder"
                ),
                () -> assertThrows(IllegalArgumentException.class, () -> 
                    new StompClient.Builder()
                        .websocketUri(WEBSOCKET_URI)
                        .build(),
                    "Should throw exception for missing topic"
                ),
                () -> assertThrows(IllegalArgumentException.class, () -> 
                    new StompClient.Builder()
                        .websocketUri(WEBSOCKET_URI)
                        .addTopic(TEST_TOPIC)
                        .build(),
                    "Should throw exception for missing message handler"
                )
            );
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {
        
        @Test
        @DisplayName("Should handle concurrent topic modification")
        void shouldHandleConcurrentTopicModification() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                threads[i] = new Thread(() -> {
                    stompClient.addTopic("/topic/concurrent" + threadNum);
                    stompClient.removeTopic("/topic/concurrent" + threadNum);
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Assert
            then(mockStateListener).should(never()).onError(any());
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {
        
        @Test
        @DisplayName("Should handle disconnection")
        void shouldHandleDisconnection() {
            // Act
            stompClient.disconnect();

            // Assert
            then(mockStateListener).should().onDisconnected();
            assertFalse(stompClient.isConnected());
        }
    }
}
```
