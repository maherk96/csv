```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Pattern;

public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private boolean isLoggedIn = false;
    private String userType = null;

    private static final Set<String> supportedSymbols = Set.of(
        "EUR/USD", "GBP/USD", "USD/JPY", "AUD/USD", "NZD/USD", "USD/CAD"
    );

    private static final Pattern directionPattern = Pattern.compile("BUY|SELL", Pattern.CASE_INSENSITIVE);

    private static final int DEFAULT_TRADE_LIMIT = 5000;
    private static final int PREMIUM_TRADE_LIMIT = 10000;

    private boolean marketOpen = true; // This could be controlled in tests via setter or constructor

    public boolean login(String userType) {
        log.info("User {} logging in", userType);
        this.userType = userType;
        this.isLoggedIn = true;
        return true;
    }

    public boolean orderPlacement(String instrument, int qty) throws Exception {
        return orderPlacement(instrument, qty, "BUY"); // Default direction
    }

    public boolean orderPlacement(String instrument, int qty, String direction) throws Exception {
        if (!isLoggedIn) {
            log.info("User is not logged in");
            throw new Exception("User is not logged in");
        }

        if ("Guest".equalsIgnoreCase(userType) || "Unauthorised".equalsIgnoreCase(userType)) {
            log.info("Unauthorised user");
            throw new Exception("Unauthorised user");
        }

        if (!marketOpen) {
            log.info("Market is closed");
            throw new Exception("Market is closed");
        }

        if (!supportedSymbols.contains(instrument)) {
            log.info("Unsupported symbol: {}", instrument);
            throw new Exception("Unsupported symbol");
        }

        if (!directionPattern.matcher(direction).matches()) {
            log.info("Invalid trade direction: {}", direction);
            throw new Exception("Invalid trade direction");
        }

        int tradeLimit = "Premium".equalsIgnoreCase(userType) ? PREMIUM_TRADE_LIMIT : DEFAULT_TRADE_LIMIT;
        if (qty > tradeLimit) {
            log.info("Trade limit exceeded for {}: limit={}, requested={}", userType, tradeLimit, qty);
            throw new Exception("Trade limit exceeded");
        }

        log.info("Placed {} trade for {} units of {}", direction, qty, instrument);
        return true;
    }

    // Optional for testing
    public void setMarketOpen(boolean marketOpen) {
        this.marketOpen = marketOpen;
    }

    public void logout() {
        this.isLoggedIn = false;
        this.userType = null;
    }
}

Feature: FX Trade Validation

  Background:
    Given the user is logged into the trade service

  # ✅ Scenario 1: Valid single BUY trade
  Scenario: Place a valid BUY trade
    When the user places a trade for 100 shares of "EUR/USD"
    Then the trade is successfully placed

  # ✅ Scenario 2: Valid SELL trade using key-value table
  Scenario: Place a valid SELL trade using key-value table
    When the user places the following trade values
      | key       | value     |
      | quantity  | 150       |
      | symbol    | GBP/USD   |
      | direction | SELL      |
    Then the trade is successfully placed

  # ✅ Scenario 3: Use key-value table with no header
  Scenario: Place trade from unnamed values
    When the user places the following unnamed values
      | quantity  | 200       |
      | symbol    | USD/JPY   |
      | direction | BUY       |
    Then the trade is successfully placed

  # ✅ Scenario 4: Trade with unsupported symbol
  Scenario: Attempt trade with unsupported symbol
    When the user places a trade for 100 shares of "XXX/YYY"
    Then the trade placement should fail with "Unsupported symbol" error

  # ✅ Scenario 5: Exceed trade limit for regular user
  Scenario: Trade exceeding standard limit
    When the user places a trade for 6000 shares of "EUR/USD"
    Then the trade placement should fail with "Trade limit exceeded" error

  # ✅ Scenario 6: Invalid trade direction
  Scenario: Trade with invalid direction
    When the user places the following trade values
      | key       | value     |
      | quantity  | 100       |
      | symbol    | USD/CAD   |
      | direction | HOLD      |
    Then the trade placement should fail with "Invalid trade direction" error

  # ✅ Scenario Outline: Block unauthorised user types
  Scenario Outline: Block trade by unauthorised roles
    Given "<userType>" is logged into the trade service
    When the user places a trade for 100 shares of "EUR/USD"
    Then the trade placement should fail with "Unauthorised user" error

    Examples:
      | userType     |
      | Guest        |
      | Unauthorised |

  # ✅ Scenario: Single-column table with valid symbols
  Scenario: Place multiple trades from a single-column list
    When the user attempts to trade with the following symbols
      | USD/JPY  |
      | AUD/USD  |
      | NZD/USD  |
    Then all trades are successfully placed

  # ✅ Scenario: Place multiple trades using row-record table
  Scenario: Place multiple trades
    When the user places the following trades
      | quantity | symbol   | direction |
      | 200      | EUR/USD  | BUY       |
      | 150      | USD/CAD  | SELL      |
      | 100      | GBP/USD  | BUY       |
    Then all trades are successfully placed

  # ✅ Scenario: Raw row table with no headers
  Scenario: Place raw trade rows without headers
    When the user places raw trade rows
      | ALG01 | order-1 | EUR/USD | 1.2565 |
      | ALG02 | order-2 | AUD/USD | 1.1111 |
    Then all trades are successfully placed

  # ✅ Scenario: Block trading when market is closed
  Scenario: Prevent trading when market is closed
    Given the market is closed
    When the user places a trade for 100 shares of "GBP/USD"
    Then the trade placement should fail with "Market is closed" error

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class TradeServiceSteps {

    private final TradeService tradeService = new TradeService();
    private boolean lastTradeSuccess;
    private Exception lastException;

    @Given("the user is logged into the trade service")
    public void theUserIsLoggedIn() {
        lastTradeSuccess = tradeService.login("user");
        Assertions.assertTrue(lastTradeSuccess, "User should be logged in");
    }

    @Given("{string} is logged into the trade service")
    public void specificUserIsLoggedIn(String userType) {
        lastTradeSuccess = tradeService.login(userType);
    }

    @Given("the market is closed")
    public void theMarketIsClosed() {
        tradeService.setMarketOpen(false);
    }

    @When("the user places a trade for {int} shares of {string}")
    public void placeTradeWithDefaults(int qty, String symbol) {
        try {
            lastTradeSuccess = tradeService.orderPlacement(symbol, qty);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastTradeSuccess = false;
        }
    }

    @When("the user places the following trade values")
    public void placeTradeWithKeyValue(DataTable dataTable) {
        Map<String, String> row = dataTable.asMap(String.class, String.class);
        int qty = Integer.parseInt(row.get("quantity"));
        String symbol = row.get("symbol");
        String direction = row.get("direction");

        try {
            lastTradeSuccess = tradeService.orderPlacement(symbol, qty, direction);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastTradeSuccess = false;
        }
    }

    @When("the user places the following unnamed values")
    public void placeTradeWithUnnamedKeyValue(DataTable dataTable) {
        Map<String, String> map = new LinkedHashMap<>();
        for (List<String> row : dataTable.cells()) {
            if (row.size() == 2) {
                map.put(row.get(0), row.get(1));
            }
        }

        int qty = Integer.parseInt(map.get("quantity"));
        String symbol = map.get("symbol");
        String direction = map.getOrDefault("direction", "BUY");

        try {
            lastTradeSuccess = tradeService.orderPlacement(symbol, qty, direction);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastTradeSuccess = false;
        }
    }

    @When("the user attempts to trade with the following symbols")
    public void tradeWithSymbols(DataTable dataTable) {
        List<String> symbols = dataTable.asList();
        for (String symbol : symbols) {
            try {
                lastTradeSuccess = tradeService.orderPlacement(symbol, 100);
                lastException = null;
            } catch (Exception e) {
                lastException = e;
                lastTradeSuccess = false;
                break;
            }
        }
    }

    @When("the user places the following trades")
    public void placeMultipleTrades(DataTable dataTable) {
        List<Map<String, String>> trades = dataTable.asMaps();
        for (Map<String, String> row : trades) {
            int qty = Integer.parseInt(row.get("quantity"));
            String symbol = row.get("symbol");
            String direction = row.getOrDefault("direction", "BUY");

            try {
                lastTradeSuccess = tradeService.orderPlacement(symbol, qty, direction);
                lastException = null;
            } catch (Exception e) {
                lastException = e;
                lastTradeSuccess = false;
                break;
            }
        }
    }

    @When("the user places raw trade rows")
    public void placeRawTradeRows(DataTable dataTable) {
        List<List<String>> rows = dataTable.cells();
        for (List<String> row : rows) {
            if (row.size() >= 4) {
                String symbol = row.get(2);
                try {
                    lastTradeSuccess = tradeService.orderPlacement(symbol, 100, "BUY");
                    lastException = null;
                } catch (Exception e) {
                    lastException = e;
                    lastTradeSuccess = false;
                    break;
                }
            }
        }
    }

    @Then("the trade is successfully placed")
    public void tradeIsSuccessful() {
        Assertions.assertTrue(lastTradeSuccess, "Expected trade to be successful");
    }

    @Then("all trades are successfully placed")
    public void allTradesSuccessful() {
        Assertions.assertNull(lastException, "Expected no trade failures");
    }

    @Then("the trade placement should fail with {string} error")
    public void tradeFailsWithMessage(String expected) {
        Assertions.assertNotNull(lastException, "Expected an exception");
        Assertions.assertTrue(
            lastException.getMessage().contains(expected),
            "Expected error to contain: " + expected + " but was: " + lastException.getMessage()
        );
    }
}
```
