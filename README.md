```java
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

    boolean hasHeader = rows.size() > 1 && rows.get(0).stream().allMatch(cell -> !cell.isBlank());
    List<Map<String, String>> result = new ArrayList<>();

    if (hasHeader) {
        List<String> headers = rows.get(0);
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                String key = headers.get(j);
                String value = row.get(j);
                rowMap.put(key, value);
            }
            rowMap.put("rowIndex", String.valueOf(i - 1));
            result.add(rowMap);
        }
    } else {
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                rowMap.put("col" + j, row.get(j));
            }
            rowMap.put("rowIndex", String.valueOf(i));
            result.add(rowMap);
        }
    }

    return result;
}
```
