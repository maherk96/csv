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

    int columnCount = rows.get(0).size();

    // Case 1: key-value table with "key"/"value" headers
    if (columnCount == 2 &&
        rows.size() > 1 &&
        "key".equalsIgnoreCase(rows.get(0).get(0)) &&
        "value".equalsIgnoreCase(rows.get(0).get(1))) {
        
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            map.put(rows.get(i).get(0), rows.get(i).get(1));
        }
        map.put("rowIndex", "0");
        return List.of(map);
    }

    // Case 2: unnamed key-value pairs (2 columns, no headers)
    if (columnCount == 2 && rows.stream().allMatch(row -> row.size() == 2)) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            map.put(rows.get(i).get(0), rows.get(i).get(1));
        }
        map.put("rowIndex", "0");
        return List.of(map);
    }

    // Case 3: header + data rows (ensure at least 2 rows, all same size)
    boolean hasHeader = rows.size() > 1 &&
        rows.get(0).size() > 1 &&
        rows.stream().allMatch(r -> r.size() == columnCount);

    if (hasHeader) {
        List<String> headers = rows.get(0);

        // Quick check: are these likely actual headers or just data? (i.e. NOT numeric/symbolic values)
        boolean headerLooksValid = headers.stream().noneMatch(s -> s.matches(".*\\d.*") || s.contains("/"));

        if (headerLooksValid) {
            List<Map<String, String>> result = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < row.size(); j++) {
                    map.put(headers.get(j), row.get(j));
                }
                map.put("rowIndex", String.valueOf(i - 1));
                result.add(map);
            }
            return result;
        }
    }

    // Case 4: Raw data (no headers)
    List<Map<String, String>> result = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
        List<String> row = rows.get(i);
        Map<String, String> map = new LinkedHashMap<>();
        for (int j = 0; j < row.size(); j++) {
            map.put("col" + j, row.get(j));
        }
        map.put("rowIndex", String.valueOf(i));
        result.add(map);
    }

    return result;
}
```
