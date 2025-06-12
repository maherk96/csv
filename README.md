```java

public static class ResultRow {
    private final Map<String, Object> data;

    public ResultRow(Map<String, Object> data) {
        this.data = data;
    }

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

    public ResultRow expect(String column, Object expectedValue) {
        Object actual = data.get(column);
        Assertions.assertEquals(expectedValue, actual,
            "Expected column [" + column + "] to be [" + expectedValue + "] but was [" + actual + "]");
        return this;
    }

    public ResultRow expect(String column, Object expectedValue, String message) {
        Assertions.assertEquals(expectedValue, data.get(column), message);
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

    public ResultRow expectMatches(String column, java.util.function.Predicate<Object> condition, String failureMessage) {
        Object actual = data.get(column);
        Assertions.assertTrue(condition.test(actual),
            "Expected column [" + column + "] to match condition, but it failed. " + failureMessage + " (was: " + actual + ")");
        return this;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
```
