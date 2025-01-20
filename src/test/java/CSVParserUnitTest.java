import org.example.CSVParseException;
import org.example.CSVParser;
import org.example.CSVParserConfig;
import org.example.TypeConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CSVParserUnitTest {

    @TempDir
    Path tempDir;

    private File csvFile;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = tempDir.resolve("test.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("currency pair,bid low price,bid upper price,offer low price,offer upper price,num. of rungs bid,num. of rungs offer\n");
            writer.write("EUR/USD,1.1,1.2,1.3,1.4,5,6\n");
            writer.write("GBP/USD,1.5,1.6,1.7,1.8,7,8\n");
            writer.write("INVALID_ROW,abc,xyz,1.9,2.0,5,not_a_number\n");
        }
    }

    @Test
    void testParseWithDefaultConfig() {
        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter(",")
                .withTrimFields(true)
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

        assertNotNull(currencyPairs);
        assertEquals(2, currencyPairs.size()); // Only valid rows should be parsed
    }

    @Test
    void testParseWithErrorStrategyContinueOnError() {
        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter(",")
                .withTrimFields(true)
                .withHeaderMapping(Map.of(
                        "currency pair", "currencyPair",
                        "bid low price", "bidLowPrice",
                        "bid upper price", "bidUpperPrice",
                        "offer low price", "offerLowPrice",
                        "offer upper price", "offerUpperPrice",
                        "num. of rungs bid", "numOfRungsBid",
                        "num. of rungs offer", "numOfRungsOffer"
                ))
                .withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.CONTINUE_ON_ERROR)
                .build();

        List<CurrencyPair> currencyPairs = CSVParser.parse(csvFile, config);

        assertNotNull(currencyPairs);
        assertEquals(2, currencyPairs.size()); // Invalid rows are ignored
    }

    @Test
    void testParseWithErrorStrategyHaltOnError() {
        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter(",")
                .withTrimFields(true)
                .withHeaderMapping(Map.of(
                        "currency pair", "currencyPair",
                        "bid low price", "bidLowPrice",
                        "bid upper price", "bidUpperPrice",
                        "offer low price", "offerLowPrice",
                        "offer upper price", "offerUpperPrice",
                        "num. of rungs bid", "numOfRungsBid",
                        "num. of rungs offer", "numOfRungsOffer"
                ))
                .withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.HALT_ON_ERROR)
                .build();

        Exception exception = assertThrows(CSVParseException.class, () -> {
            CSVParser.parse(csvFile, config);
        });

        assertTrue(exception.getMessage().contains("Error parsing line")); // Assert it fails on the invalid row
    }

    @Test
    void testParseWithErrorStrategyCollectErrors() {
        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter(",")
                .withTrimFields(true)
                .withHeaderMapping(Map.of(
                        "currency pair", "currencyPair",
                        "bid low price", "bidLowPrice",
                        "bid upper price", "bidUpperPrice",
                        "offer low price", "offerLowPrice",
                        "offer upper price", "offerUpperPrice",
                        "num. of rungs bid", "numOfRungsBid",
                        "num. of rungs offer", "numOfRungsOffer"
                ))
                .withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.COLLECT_ERRORS)
                .build();

        List<CurrencyPair> currencyPairs = CSVParser.parse(csvFile, config);

        assertNotNull(currencyPairs);
        assertEquals(2, currencyPairs.size()); // Invalid rows are ignored
    }
    @Test
    void testParseWithCustomDelimiter() throws IOException {
        File customDelimiterFile = tempDir.resolve("custom.csv").toFile();
        try (FileWriter writer = new FileWriter(customDelimiterFile)) {
            // Ensure headers and values are separated by the custom delimiter
            writer.write("currency pair|bid low price|bid upper price|offer low price|offer upper price|num. of rungs bid|num. of rungs offer\n");
            writer.write("EUR/USD|1.1|1.2|1.3|1.4|5|6\n");
        }

        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter("|") // Specify the custom delimiter
                .withTrimFields(true)
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

        List<CurrencyPair> currencyPairs = CSVParser.parse(customDelimiterFile, config);

        assertNotNull(currencyPairs);
        assertEquals(1, currencyPairs.size()); // Only one valid row
        assertEquals("EUR/USD", currencyPairs.get(0).getCurrencyPair());
        assertEquals(1.1, currencyPairs.get(0).getBidLowPrice());
        assertEquals(1.2, currencyPairs.get(0).getBidUpperPrice());
        assertEquals(1.3, currencyPairs.get(0).getOfferLowPrice());
        assertEquals(1.4, currencyPairs.get(0).getOfferUpperPrice());
        assertEquals(5, currencyPairs.get(0).getNumOfRungsBid());
        assertEquals(6, currencyPairs.get(0).getNumOfRungsOffer());
    }

    @Test
    void testParseWithEmptyLines() throws IOException {
        File fileWithEmptyLines = tempDir.resolve("emptylines.csv").toFile();
        try (FileWriter writer = new FileWriter(fileWithEmptyLines)) {
            writer.write("currency pair,bid low price,bid upper price,offer low price,offer upper price,num. of rungs bid,num. of rungs offer\n");
            writer.write("\n"); // Empty line
            writer.write("EUR/USD,1.1,1.2,1.3,1.4,5,6\n");
            writer.write("\n"); // Empty line
        }

        CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
                .withDelimiter(",")
                .withTrimFields(true)
                .withSkipEmptyLines(true) // Enable skipping empty lines
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

        List<CurrencyPair> currencyPairs = CSVParser.parse(fileWithEmptyLines, config);

        assertNotNull(currencyPairs);
        assertEquals(1, currencyPairs.size());
        assertEquals("EUR/USD", currencyPairs.get(0).getCurrencyPair());
    }



        @Test
        void testParseWithTypeConverter() throws IOException {
            csvFile = tempDir.resolve("test.csv").toFile();
            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.write("currency pair,bid low price,bid upper price,offer low price,offer upper price,num. of rungs bid,num. of rungs offer,date,custom type\n");
                writer.write("EUR/USD,1.1,1.2,1.3,1.4,5,6,2025-01-01,custom_value\n");
                writer.write("GBP/USD,1.5,1.6,1.7,1.8,7,8,2025-02-01,another_value\n");
            }

            // Register a custom converter for a specific type
            TypeConverter.registerConverter(CustomType.class, CustomType::new);

            CSVParserConfig<CurrencyPairWithCustomType> config = new CSVParserConfig.Builder<>(CurrencyPairWithCustomType.class)
                    .withDelimiter(",")
                    .withTrimFields(true)
                    .withHeaderMapping(Map.of(
                            "currency pair", "currencyPair",
                            "bid low price", "bidLowPrice",
                            "bid upper price", "bidUpperPrice",
                            "offer low price", "offerLowPrice",
                            "offer upper price", "offerUpperPrice",
                            "num. of rungs bid", "numOfRungsBid",
                            "num. of rungs offer", "numOfRungsOffer",
                            "date", "date",
                            "custom type", "customType"
                    ))
                    .build();

            List<CurrencyPairWithCustomType> currencyPairs = CSVParser.parse(csvFile, config);

            assertNotNull(currencyPairs);
            assertEquals(2, currencyPairs.size());

            // First row
            CurrencyPairWithCustomType firstPair = currencyPairs.get(0);
            assertEquals("EUR/USD", firstPair.getCurrencyPair());
            assertEquals(1.1, firstPair.getBidLowPrice());
            assertEquals(1.2, firstPair.getBidUpperPrice());
            assertEquals(1.3, firstPair.getOfferLowPrice());
            assertEquals(1.4, firstPair.getOfferUpperPrice());
            assertEquals(5, firstPair.getNumOfRungsBid());
            assertEquals(6, firstPair.getNumOfRungsOffer());
            assertEquals(LocalDate.of(2025, 1, 1), firstPair.getDate());
            assertEquals(new CustomType("custom_value"), firstPair.getCustomType());

            // Second row
            CurrencyPairWithCustomType secondPair = currencyPairs.get(1);
            assertEquals("GBP/USD", secondPair.getCurrencyPair());
            assertEquals(1.5, secondPair.getBidLowPrice());
            assertEquals(1.6, secondPair.getBidUpperPrice());
            assertEquals(1.7, secondPair.getOfferLowPrice());
            assertEquals(1.8, secondPair.getOfferUpperPrice());
            assertEquals(7, secondPair.getNumOfRungsBid());
            assertEquals(8, secondPair.getNumOfRungsOffer());
            assertEquals(LocalDate.of(2025, 2, 1), secondPair.getDate());
            assertEquals(new CustomType("another_value"), secondPair.getCustomType());
        }


    public static class CurrencyPair {
        private String currencyPair;
        private double bidLowPrice;
        private double bidUpperPrice;
        private double offerLowPrice;
        private double offerUpperPrice;
        private int numOfRungsBid;
        private int numOfRungsOffer;

        // Getters and setters

        public String getCurrencyPair() {
            return currencyPair;
        }

        public void setCurrencyPair(String currencyPair) {
            this.currencyPair = currencyPair;
        }

        public double getBidLowPrice() {
            return bidLowPrice;
        }

        public void setBidLowPrice(double bidLowPrice) {
            this.bidLowPrice = bidLowPrice;
        }

        public double getBidUpperPrice() {
            return bidUpperPrice;
        }

        public void setBidUpperPrice(double bidUpperPrice) {
            this.bidUpperPrice = bidUpperPrice;
        }

        public double getOfferLowPrice() {
            return offerLowPrice;
        }

        public void setOfferLowPrice(double offerLowPrice) {
            this.offerLowPrice = offerLowPrice;
        }

        public double getOfferUpperPrice() {
            return offerUpperPrice;
        }

        public void setOfferUpperPrice(double offerUpperPrice) {
            this.offerUpperPrice = offerUpperPrice;
        }

        public int getNumOfRungsBid() {
            return numOfRungsBid;
        }

        public void setNumOfRungsBid(int numOfRungsBid) {
            this.numOfRungsBid = numOfRungsBid;
        }

        public int getNumOfRungsOffer() {
            return numOfRungsOffer;
        }

        public void setNumOfRungsOffer(int numOfRungsOffer) {
            this.numOfRungsOffer = numOfRungsOffer;
        }

        @Override
        public String toString() {
            return "CurrencyPair{" +
                    "currencyPair='" + currencyPair + '\'' +
                    ", bidLowPrice=" + bidLowPrice +
                    ", bidUpperPrice=" + bidUpperPrice +
                    ", offerLowPrice=" + offerLowPrice +
                    ", offerUpperPrice=" + offerUpperPrice +
                    ", numOfRungsBid=" + numOfRungsBid +
                    ", numOfRungsOffer=" + numOfRungsOffer +
                    '}';
        }
    }

        // Additional POJO with CustomType
        public static class CurrencyPairWithCustomType {
            private String currencyPair;
            private double bidLowPrice;
            private double bidUpperPrice;
            private double offerLowPrice;
            private double offerUpperPrice;
            private int numOfRungsBid;
            private int numOfRungsOffer;
            private LocalDate date;
            private CustomType customType;

            // Getters and Setters
            public String getCurrencyPair() {
                return currencyPair;
            }

            public void setCurrencyPair(String currencyPair) {
                this.currencyPair = currencyPair;
            }

            public double getBidLowPrice() {
                return bidLowPrice;
            }

            public void setBidLowPrice(double bidLowPrice) {
                this.bidLowPrice = bidLowPrice;
            }

            public double getBidUpperPrice() {
                return bidUpperPrice;
            }

            public void setBidUpperPrice(double bidUpperPrice) {
                this.bidUpperPrice = bidUpperPrice;
            }

            public double getOfferLowPrice() {
                return offerLowPrice;
            }

            public void setOfferLowPrice(double offerLowPrice) {
                this.offerLowPrice = offerLowPrice;
            }

            public double getOfferUpperPrice() {
                return offerUpperPrice;
            }

            public void setOfferUpperPrice(double offerUpperPrice) {
                this.offerUpperPrice = offerUpperPrice;
            }

            public int getNumOfRungsBid() {
                return numOfRungsBid;
            }

            public void setNumOfRungsBid(int numOfRungsBid) {
                this.numOfRungsBid = numOfRungsBid;
            }

            public int getNumOfRungsOffer() {
                return numOfRungsOffer;
            }

            public void setNumOfRungsOffer(int numOfRungsOffer) {
                this.numOfRungsOffer = numOfRungsOffer;
            }

            public LocalDate getDate() {
                return date;
            }

            public void setDate(LocalDate date) {
                this.date = date;
            }

            public CustomType getCustomType() {
                return customType;
            }

            public void setCustomType(CustomType customType) {
                this.customType = customType;
            }
        }

        // Custom Type
        public static class CustomType {
            private final String value;

            public CustomType(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return "CustomType{" + "value='" + value + '\'' + '}';
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                CustomType that = (CustomType) obj;
                return value.equals(that.value);
            }
        }
    }
