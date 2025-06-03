```java
/**
 * Extracts the data table arguments from a test step, if present, and adds rowIndex to each row.
 *
 * @param testStep the Cucumber test step object which may contain a data table.
 * @return a list of maps representing the rows of the data table, including rowIndex for each row.
 */
public List<Map<String, String>> extractDataTable(TestStep testStep) {
    if (!(testStep instanceof PickleStepTestStep pickleStep)) {
        return Collections.emptyList();
    }

    if (!(pickleStep.getStep().getArgument() instanceof DataTableArgument dataTableArgument)) {
        return Collections.emptyList();
    }

    List<List<String>> rows = dataTableArgument.cells();
    if (rows == null || rows.isEmpty()) {
        return Collections.emptyList();
    }

    List<String> headers = rows.get(0);

    return IntStream.range(1, rows.size()) // Skip header
        .mapToObj(i -> {
            List<String> row = rows.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                rowMap.put(headers.get(j), row.get(j));
            }
            rowMap.put("rowIndex", String.valueOf(i - 1)); // 0-based row index
            return rowMap;
        })
        .collect(Collectors.toList());
}
```
