```java
public static class ResultRow {
    private final Map<String, Object> data;

    public ResultRow(Map<String, Object> data) {
        this.data = data;
    }

    // ======== Typed Accessors ========
    public String getString(String column) {
        return (String) data.get(column);
    }

    public Long getLong(String column) {
        Object val = data.get(column);
        return val instanceof Number ? ((Number) val).longValue() : null;
    }

    public Integer getInt(String column) {
        Object val = data.get(column);
        return val instanceof Number ? ((Number) val).intValue() : null;
    }

    public Object get(String column) {
        return data.get(column);
    }

    public Map<String, Object> asMap() {
        return data;
    }

    // ======== Assertion Helpers ========
    public ResultRow expect(String column, Object expectedValue) {
        return expect(column, expectedValue, "Expected column [" + column + "] to be [" + expectedValue + "]");
    }

    public ResultRow expect(String column, Object expectedValue, String message) {
        Object actual = data.get(column);

        if (actual instanceof Number && expectedValue instanceof Number) {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expectedValue.toString());
            Assertions.assertEquals(expectedNum, actualNum,
                message + " but was [" + actual + "]");
        } else {
            Assertions.assertEquals(expectedValue, actual,
                message + " but was [" + actual + "]");
        }

        return this;
    }

    public ResultRow expectNotNull(String column) {
        Object actual = data.get(column);
        Assertions.assertNotNull(actual, "Expected column [" + column + "] to be non-null");
        return this;
    }

    public ResultRow expectNull(String column) {
        Object actual = data.get(column);
        Assertions.assertNull(actual, "Expected column [" + column + "] to be null but was [" + actual + "]");
        return this;
    }

    public ResultRow expectMatches(String column, Predicate<Object> condition, String failureMessage) {
        Object actual = data.get(column);
        Assertions.assertTrue(condition.test(actual),
            "Expected column [" + column + "] to match condition, but it failed. " + failureMessage + " (was: " + actual + ")");
        return this;
    }

    public ResultRow expectGreaterThan(String column, Number minValue) {
        Object actual = data.get(column);
        Assertions.assertTrue(actual instanceof Number, "Expected column [" + column + "] to be a number");

        BigDecimal actualNum = new BigDecimal(actual.toString());
        BigDecimal minNum = new BigDecimal(minValue.toString());

        Assertions.assertTrue(actualNum.compareTo(minNum) > 0,
            "Expected column [" + column + "] to be > " + minNum + " but was " + actualNum);
        return this;
    }

    public ResultRow expectLessThan(String column, Number maxValue) {
        Object actual = data.get(column);
        Assertions.assertTrue(actual instanceof Number, "Expected column [" + column + "] to be a number");

        BigDecimal actualNum = new BigDecimal(actual.toString());
        BigDecimal maxNum = new BigDecimal(maxValue.toString());

        Assertions.assertTrue(actualNum.compareTo(maxNum) < 0,
            "Expected column [" + column + "] to be < " + maxNum + " but was " + actualNum);
        return this;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    // ======== Fluent Builder for Lambdas ========
    public static ChainedExpectations expecting() {
        return new ChainedExpectations();
    }

    public static class ChainedExpectations implements Consumer<ResultRow> {
        private final List<Consumer<ResultRow>> assertions = new ArrayList<>();

        public ChainedExpectations expect(String column, Object expectedValue) {
            assertions.add(row -> row.expect(column, expectedValue));
            return this;
        }

        public ChainedExpectations expect(String column, Object expectedValue, String message) {
            assertions.add(row -> row.expect(column, expectedValue, message));
            return this;
        }

        public ChainedExpectations expectNotNull(String column) {
            assertions.add(row -> row.expectNotNull(column));
            return this;
        }

        public ChainedExpectations expectNull(String column) {
            assertions.add(row -> row.expectNull(column));
            return this;
        }

        public ChainedExpectations expectMatches(String column, Predicate<Object> matcher, String failureMessage) {
            assertions.add(row -> row.expectMatches(column, matcher, failureMessage));
            return this;
        }

        public ChainedExpectations expectGreaterThan(String column, Number minValue) {
            assertions.add(row -> row.expectGreaterThan(column, minValue));
            return this;
        }

        public ChainedExpectations expectLessThan(String column, Number maxValue) {
            assertions.add(row -> row.expectLessThan(column, maxValue));
            return this;
        }

        @Override
        public void accept(ResultRow row) {
            assertions.forEach(assertion -> assertion.accept(row));
        }
    }
}
```
