```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataTableWrapper {

    public enum TableType {
        KEY_VALUE,
        RECORDS,
        SINGLE_COLUMN,
        RAW_ROWS,
        EMPTY
    }

    private TableType type;

    private Map<String, String> keyValue;
    private List<Map<String, String>> records;
    private List<String> singleColumn;
    private List<List<String>> rawRows;
}

public DataTableWrapper extractDataTable(TestStep testStep) {
    if (!(testStep instanceof PickleStepTestStep pickleStep)) return new DataTableWrapper(DataTableWrapper.TableType.EMPTY, null, null, null, null);
    if (!(pickleStep.getStep().getArgument() instanceof DataTableArgument dataTableArgument)) return new DataTableWrapper(DataTableWrapper.TableType.EMPTY, null, null, null, null);

    List<List<String>> rows = dataTableArgument.cells();
    if (rows == null || rows.isEmpty()) return new DataTableWrapper(DataTableWrapper.TableType.EMPTY, null, null, null, null);

    int columnCount = rows.get(0).size();

    // SINGLE COLUMN
    if (columnCount == 1) {
        List<String> list = rows.stream()
            .map(row -> row.get(0))
            .collect(Collectors.toList());
        return new DataTableWrapper(DataTableWrapper.TableType.SINGLE_COLUMN, null, null, list, null);
    }

    // TWO COLUMNS, NO HEADER → KEY_VALUE
    if (columnCount == 2 && !rows.get(0).get(0).equalsIgnoreCase("key")) {
        Map<String, String> map = rows.stream()
            .filter(row -> row.size() == 2)
            .collect(Collectors.toMap(
                row -> row.get(0),
                row -> row.get(1),
                (a, b) -> b,
                LinkedHashMap::new
            ));
        return new DataTableWrapper(DataTableWrapper.TableType.KEY_VALUE, map, null, null, null);
    }

    // COLUMN COUNT >= 2, FIRST ROW LOOKS LIKE HEADERS → RECORDS
    if (rows.size() >= 2) {
        List<String> headers = rows.get(0);
        List<Map<String, String>> list = rows.subList(1, rows.size()).stream()
            .map(row -> {
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    map.put(headers.get(i), i < row.size() ? row.get(i) : "");
                }
                return map;
            })
            .collect(Collectors.toList());

        // Heuristic: if headers are non-numeric and look like identifiers
        boolean allHeadersText = headers.stream().allMatch(h -> h.matches("^[a-zA-Z_][a-zA-Z0-9_]*$"));
        if (allHeadersText) {
            return new DataTableWrapper(DataTableWrapper.TableType.RECORDS, null, list, null, null);
        }
    }

    // Fallback: treat as raw rows
    return new DataTableWrapper(DataTableWrapper.TableType.RAW_ROWS, null, null, null, rows);
}

private void handleTestStepFinished(TestStepFinished event) {
    Throwable error = event.getResult().getError();

    if (!(event.getTestStep() instanceof HookTestStep)) {

        DataTableWrapper dataTable = exHelper.extractDataTable(event.getTestStep());

        var qapStep = new QAPSteps(
            exHelper.extractStepName(event.getTestStep()),
            event.getResult().getStatus().name(),
            error != null ? ExceptionUtils.getStackTrace(error) : null,
            (currentStepStartTime.get() == null)
                ? Instant.now().toEpochMilli()
                : currentStepStartTime.get().toEpochMilli(),
            Instant.now().toEpochMilli(),
            dataTable // <--- This is now a DataTableWrapper
        );

        currentScenario.get().addStep(qapStep);
        currentStepStartTime.remove();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QAPSteps {
    private String stepName;
    private String status;
    private String error;
    private long startTime;
    private long endTime;
    private DataTableWrapper dataTable;
}
```
