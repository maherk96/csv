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

    // Case 1: key-value table with headers "key" and "value"
    if (columnCount == 2 && rows.size() > 1 && "key".equalsIgnoreCase(rows.get(0).get(0)) && "value".equalsIgnoreCase(rows.get(0).get(1))) {
        Map<String, String> kvMap = new LinkedHashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            kvMap.put(rows.get(i).get(0), rows.get(i).get(1));
        }
        kvMap.put("rowIndex", "0");
        return List.of(kvMap);
    }

    // Case 2: unnamed key-value rows (e.g., quantity | 200)
    if (columnCount == 2 && rows.stream().allMatch(row -> row.size() == 2)) {
        Map<String, String> unnamedMap = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            unnamedMap.put(rows.get(i).get(0), rows.get(i).get(1));
        }
        unnamedMap.put("rowIndex", "0");
        return List.of(unnamedMap);
    }

    // Case 3: proper header + data rows
    if (rows.size() > 1 && columnCount > 1) {
        List<String> headers = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                rowMap.put(headers.get(j), row.get(j));
            }
            rowMap.put("rowIndex", String.valueOf(i - 1));
            result.add(rowMap);
        }
        return result;
    }

    // Case 4: raw data (multi-column, no headers)
    if (rows.size() >= 1 && columnCount > 1) {
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < row.size(); j++) {
                rowMap.put("col" + j, row.get(j));
            }
            rowMap.put("rowIndex", String.valueOf(i));
            result.add(rowMap);
        }
        return result;
    }

    // Case 5: single-column raw list
    if (columnCount == 1) {
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> rowMap = new LinkedHashMap<>();
            rowMap.put("col0", rows.get(i).get(0));
            rowMap.put("rowIndex", String.valueOf(i));
            result.add(rowMap);
        }
        return result;
    }

    return Collections.emptyList();
}
```
