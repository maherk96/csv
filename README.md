```java
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;

/**
 * Produces market data snapshots and sends them via a configured {@link MessageSender}.
 * Supports both immediate and scheduled snapshots at regular intervals.
 * Uses functional programming with Java Streams for efficient processing.
 * 
 * <p>Example usage:
 * <pre>{@code
 * MessageSender sender = new TibrvMessageSender("FX.MARKETDATA.@CCY");
 * MarketDataProducer producer = new MarketDataProducer(sender, 0.0001);
 * producer.createMarketDataSnapShot("EUR/USD", 1.2345, 5, true, 10);
 * }</pre>
 * </p>
 */
@Slf4j
public class MarketDataProducer {
    private final MessageSender messageSender;
    private final double pipBasis;

    /**
     * Constructs a MarketDataProducer with a specified message sender and pip basis.
     * 
     * @param messageSender The message sender (e.g., TIBRV, Solace, Kafka)
     * @param pipBasis The pip basis for calculating bid/ask prices
     */
    public MarketDataProducer(MessageSender messageSender, double pipBasis) {
        this.messageSender = messageSender;
        this.pipBasis = pipBasis;
    }

    /**
     * Creates a market data snapshot and optionally schedules it for periodic sending.
     * 
     * @param symbol The currency pair (e.g., "EUR/USD")
     * @param price The base price for bid/ask calculation
     * @param numOfRungs The number of price levels to generate
     * @param useTimer If true, the snapshot will be sent periodically
     * @param intervalSeconds The interval (in seconds) between scheduled snapshots
     */
    public void createMarketDataSnapShot(String symbol, double price, int numOfRungs, boolean useTimer, long intervalSeconds) {
        double askPrice = price + pipBasis;
        double bidPrice = price - pipBasis;
        DspMarketDataSnapShot snapshot = createMarketData(symbol, askPrice, bidPrice, numOfRungs);

        if (useTimer) {
            messageSender.startScheduledSending(snapshot, intervalSeconds);
        } else {
            CompletableFuture.runAsync(() -> messageSender.sendMarketData(snapshot));
        }
    }

    /**
     * Creates a market data snapshot containing bid and ask entries.
     * 
     * @param symbol The currency pair symbol
     * @param askPrice The starting ask price
     * @param bidPrice The starting bid price
     * @param numOfRungs The number of price levels to generate
     * @return A {@link DspMarketDataSnapShot} representing the market data
     */
    private DspMarketDataSnapShot createMarketData(String symbol, double askPrice, double bidPrice, int numOfRungs) {
        Instant timestamp = Instant.now();
        List<MdEntries> mdEntriesList = generateMdEntries(askPrice, bidPrice, numOfRungs, timestamp);

        return DspMarketDataSnapShot.builder()
                .sendingTime(timestamp)
                .symbol(symbol)
                .securityType(SecurityType.SPOT)
                .mdEntries(mdEntriesList)
                .build();
    }

    /**
     * Generates a list of market depth entries using Java Streams.
     * 
     * @param askPrice The initial ask price
     * @param bidPrice The initial bid price
     * @param numOfRungs The number of price levels
     * @param timestamp The timestamp for the market data
     * @return A list of {@link MdEntries} containing bid and ask prices
     */
    private List<MdEntries> generateMdEntries(double askPrice, double bidPrice, int numOfRungs, Instant timestamp) {
        return Stream.iterate(1, i -> i + 1)
                .limit(numOfRungs)
                .flatMap(i -> Stream.of(
                        createMdEntry(MdEntryType.ASK, askPrice + (i - 1) * pipBasis, 100_000L * i, "C", timestamp),
                        createMdEntry(MdEntryType.BID, bidPrice - (i - 1) * pipBasis, 100_000L * i, "C", timestamp)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Creates a single market depth entry.
     * 
     * @param type The entry type (ASK or BID)
     * @param price The price level
     * @param quantity The order quantity
     * @param currency The currency type
     * @param timestamp The timestamp of the entry
     * @return An {@link MdEntries} object
     */
    private MdEntries createMdEntry(MdEntryType type, double price, long quantity, String currency, Instant timestamp) {
        return new MdEntries(type, price, quantity, currency, timestamp);
    }
}
```
