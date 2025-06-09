```java
/**
 * Extracts a structured data table from a Cucumber TestStep if it contains a valid DataTableArgument.
 * Supports key-value pairs, header-based rows, and fallback generic formats.
 *
 * @param testStep the test step to extract the data table from
 * @return a list of maps, each representing a row in the table
 */
public List<Map<String, String>> extractDataTable(TestStep testStep) {
    if (!(testStep instanceof PickleStepTestStep pickleStep)) return Collections.emptyList();
    if (!(pickleStep.getStep().getArgument() instanceof DataTableArgument dataTableArgument)) return Collections.emptyList();

    List<List<String>> rows = dataTableArgument.cells();
    if (rows == null || rows.isEmpty()) return Collections.emptyList();

    int columnCount = rows.get(0).size();

    if (isKeyValueTable(rows, columnCount)) {
        return List.of(toKeyValueMap(rows.stream().skip(1).toList(), 0));
    }

    if (isUniformKeyValueRows(rows, columnCount)) {
        return List.of(toKeyValueMap(rows, 0));
    }

    if (hasHeaderRow(rows, columnCount)) {
        List<String> headers = rows.get(0);
        return IntStream.range(1, rows.size())
            .mapToObj(i -> toHeaderMappedRow(headers, rows.get(i), i - 1))
            .collect(Collectors.toList());
    }

    return IntStream.range(0, rows.size())
        .mapToObj(i -> toIndexedGenericRow(rows.get(i), i))
        .collect(Collectors.toList());
}

/**
 * Determines if the first row contains 'key' and 'value' headers.
 */
private boolean isKeyValueTable(List<List<String>> rows, int columnCount) {
    return columnCount == 2 &&
           rows.size() > 1 &&
           "key".equalsIgnoreCase(rows.get(0).get(0)) &&
           "value".equalsIgnoreCase(rows.get(0).get(1));
}

/**
 * Checks if all rows contain exactly two columns, indicating simple key-value pairs.
 */
private boolean isUniformKeyValueRows(List<List<String>> rows, int columnCount) {
    return columnCount == 2 && rows.stream().allMatch(row -> row.size() == 2);
}

/**
 * Checks if the first row is likely a header row by ensuring it's not numeric or symbolic.
 */
private boolean hasHeaderRow(List<List<String>> rows, int columnCount) {
    return rows.size() > 1 &&
           rows.get(0).size() == columnCount &&
           rows.stream().allMatch(r -> r.size() == columnCount) &&
           rows.get(0).stream().noneMatch(s -> s.matches(".*\\d.*") || s.contains("/"));
}

/**
 * Converts a 2-column row list into a single key-value map.
 *
 * @param rows the list of rows
 * @param rowIndex the row index to start from
 * @return a key-value map with "rowIndex" included
 */
private Map<String, String> toKeyValueMap(List<List<String>> rows, int rowIndex) {
    Map<String, String> map = IntStream.range(rowIndex, rows.size())
        .boxed()
        .collect(Collectors.toMap(
            i -> rows.get(i).get(0),
            i -> rows.get(i).get(1),
            (a, b) -> b,
            LinkedHashMap::new
        ));
    map.put("rowIndex", "0");
    return map;
}

/**
 * Maps a data row to its corresponding header columns.
 *
 * @param headers the header row
 * @param row the current row
 * @param index the row index
 * @return a map of header-value pairs
 */
private Map<String, String> toHeaderMappedRow(List<String> headers, List<String> row, int index) {
    Map<String, String> map = IntStream.range(0, Math.min(headers.size(), row.size()))
        .boxed()
        .collect(Collectors.toMap(
            headers::get,
            row::get,
            (a, b) -> b,
            LinkedHashMap::new
        ));
    map.put("rowIndex", String.valueOf(index));
    return map;
}

/**
 * Converts a row into a generic column-labeled map ("col0", "col1", etc.).
 *
 * @param row the list of values in the row
 * @param index the row index
 * @return a map with column-indexed keys and values
 */
private Map<String, String> toIndexedGenericRow(List<String> row, int index) {
    Map<String, String> map = IntStream.range(0, row.size())
        .boxed()
        .collect(Collectors.toMap(
            i -> "col" + i,
            row::get,
            (a, b) -> b,
            LinkedHashMap::new
        ));
    map.put("rowIndex", String.valueOf(index));
    return map;
}
```
