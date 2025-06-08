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

    List<Map<String, String>> result = new ArrayList<>();

    int columnCount = rows.get(0).size();

    if (columnCount == 2 && rows.stream().allMatch(r -> r.size() == 2)) {
        // Treat as key-value pairs (no header)
        Map<String, String> merged = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            merged.put(row.get(0), row.get(1));
        }
        merged.put("rowIndex", "0");
        result.add(merged);

    } else if (rows.size() > 1 && columnCount > 1) {
        // Treat first row as header
        List<String> headers = rows.get(0);
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> map = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                map.put(headers.get(j), row.get(j));
            }
            map.put("rowIndex", String.valueOf(i - 1));
            result.add(map);
        }

    } else {
        // Handle raw or single-column rows
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> map = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                map.put("col" + j, row.get(j));
            }
            map.put("rowIndex", String.valueOf(i));
            result.add(map);
        }
    }

    return result;
}
```
