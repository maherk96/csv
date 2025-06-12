```java

import org.junit.jupiter.api.Assertions;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A utility class for fluent, expressive database assertions in tests using JdbcTemplate.
 */
public class DatabaseClient {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new DatabaseClient with the given DataSource.
     *
     * @param dataSource the DataSource to use
     */
    public DatabaseClient(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Starts building a query runner for the given SQL.
     *
     * @param sql the SQL query to execute
     * @return a fluent QueryRunner for chaining
     */
    public QueryRunner runQuery(String sql) {
        return new QueryRunner(sql, jdbcTemplate);
    }

    /**
     * Fluent helper class for executing queries and asserting on results.
     */
    public static class QueryRunner {
        private final String sql;
        private final JdbcTemplate jdbcTemplate;
        private Object[] params = new Object[0];
        private List<Map<String, Object>> results = null;

        public QueryRunner(String sql, JdbcTemplate jdbcTemplate) {
            this.sql = sql;
            this.jdbcTemplate = jdbcTemplate;
        }

        /**
         * Sets query parameters.
         *
         * @param params positional parameters for the SQL
         * @return this QueryRunner
         */
        public QueryRunner withParams(Object... params) {
            this.params = params;
            return this;
        }

        private void executeQueryIfNeeded() {
            if (results == null) {
                results = jdbcTemplate.queryForList(sql, params);
            }
        }

        /**
         * Asserts that the query returns no results.
         *
         * @return this QueryRunner
         */
        public QueryRunner expectNoResults() {
            executeQueryIfNeeded();
            Assertions.assertTrue(results.isEmpty(), "Expected no results, but got: " + results);
            return this;
        }

        /**
         * Asserts that the query returns exactly the given number of rows.
         *
         * @param expected expected number of rows
         * @return this QueryRunner
         */
        public QueryRunner expectRowCount(int expected) {
            executeQueryIfNeeded();
            Assertions.assertEquals(expected, results.size(), "Expected " + expected + " rows, but got: " + results.size());
            return this;
        }

        /**
         * Asserts that at least one row contains the expected value in the specified column.
         *
         * @param column the column name
         * @param expectedValue the expected value
         * @return this QueryRunner
         */
        public QueryRunner expectResultsToContain(String column, Object expectedValue) {
            executeQueryIfNeeded();
            boolean match = results.stream().anyMatch(row -> expectedValue.equals(row.get(column)));
            Assertions.assertTrue(match, "Expected a row where " + column + " = " + expectedValue + ", but got: " + results);
            return this;
        }

        /**
         * Asserts that the result set contains exactly one row, and runs assertions on it.
         *
         * @param rowAssertion assertions to apply to the single row
         * @return this QueryRunner
         */
        public QueryRunner expectSingleRow(Consumer<ResultRow> rowAssertion) {
            executeQueryIfNeeded();
            Assertions.assertEquals(1, results.size(), "Expected one row but got: " + results.size());
            rowAssertion.accept(new ResultRow(results.get(0)));
            return this;
        }

        /**
         * Applies the same assertion logic to each row in the result set.
         *
         * @param rowAssertions logic to apply to each row
         * @return this QueryRunner
         */
        public QueryRunner assertEachRow(Consumer<ResultRow> rowAssertions) {
            executeQueryIfNeeded();
            for (Map<String, Object> row : results) {
                rowAssertions.accept(new ResultRow(row));
            }
            return this;
        }

        /**
         * Asserts each row individually using the given list of row assertions.
         *
         * @param rowAssertions assertions to apply in order to each row
         * @return this QueryRunner
         */
        public QueryRunner expectRows(List<Consumer<ResultRow>> rowAssertions) {
            executeQueryIfNeeded();
            Assertions.assertEquals(rowAssertions.size(), results.size(), "Row count mismatch");
            for (int i = 0; i < rowAssertions.size(); i++) {
                rowAssertions.get(i).accept(new ResultRow(results.get(i)));
            }
            return this;
        }

        /**
         * Asserts that the query result exactly matches the expected rows.
         *
         * @param expectedRows expected result rows
         * @return this QueryRunner
         */
        public QueryRunner expectExactlyMatchingRows(List<Map<String, Object>> expectedRows) {
            executeQueryIfNeeded();
            Assertions.assertEquals(expectedRows.size(), results.size(), "Expected " + expectedRows.size() + " rows but got " + results.size());
            for (int i = 0; i < expectedRows.size(); i++) {
                Assertions.assertEquals(expectedRows.get(i), results.get(i), "Row " + i + " mismatch");
            }
            return this;
        }

        /**
         * Prints the results to the console (for debugging).
         *
         * @return this QueryRunner
         */
        public QueryRunner printResults() {
            executeQueryIfNeeded();
            results.forEach(System.out::println);
            return this;
        }

        /**
         * Returns all result rows wrapped in ResultRow.
         *
         * @return list of ResultRow
         */
        public List<ResultRow> getResults() {
            executeQueryIfNeeded();
            return results.stream().map(ResultRow::new).collect(Collectors.toList());
        }

        /**
         * Convenience alias to improve readability when chaining.
         *
         * @return this QueryRunner
         */
        public QueryRunner and() {
            return this;
        }
    }

    /**
     * A wrapper for a row of results that provides type-safe accessors.
     */
    public static class ResultRow {
        private final Map<String, Object> data;

        public ResultRow(Map<String, Object> data) {
            this.data = data;
        }

        /**
         * Gets the value as a String from the specified column.
         *
         * @param column the column name
         * @return the String value
         */
        public String getString(String column) {
            return (String) data.get(column);
        }

        /**
         * Gets the value as a Long from the specified column.
         *
         * @param column the column name
         * @return the Long value
         */
        public Long getLong(String column) {
            Object val = data.get(column);
            return val instanceof Number ? ((Number) val).longValue() : null;
        }

        /**
         * Gets the value as an Integer from the specified column.
         *
         * @param column the column name
         * @return the Integer value
         */
        public Integer getInt(String column) {
            Object val = data.get(column);
            return val instanceof Number ? ((Number) val).intValue() : null;
        }

        /**
         * Gets the raw value from the specified column.
         *
         * @param column the column name
         * @return the Object value
         */
        public Object get(String column) {
            return data.get(column);
        }

        /**
         * Returns the underlying row as a map.
         *
         * @return map of column to value
         */
        public Map<String, Object> asMap() {
            return data;
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }
} 
```
