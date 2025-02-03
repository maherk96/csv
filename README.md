
```java
private StringBuilder failureMessage(OrderTestData testData) {
    StringBuilder builder = new StringBuilder();
    List<ExecutionReport> list = getReports(testData.getClOrdID());
    ExecutionReport lastEr = list.stream().reduce((first, second) -> second).orElse(null);

    builder.append("\n--- Failure Details ---\n");

    if (lastEr != null) {
        builder.append("Last Order Status: ").append(lastEr.getOrdStatus()).append("\n");
        builder.append("Last Execution Type: ").append(lastEr.getExecType()).append("\n");
        builder.append("Last Execution Report: ").append(lastEr).append("\n");

        // Check for pending replace
        if (lastEr.getExecType().equals(ExecutionType.PENDING_REPLACE)) {
            builder.append("Order Status: Pending Replace\n");

            // Check for a corresponding reject in the order cancel reject list
            OrderCancelRejectReport rejectReport = orderCancelRejectList.stream()
                .filter(reject -> reject.getClOrdID().equals(testData.getClOrdID()))
                .findFirst()
                .orElse(null);

            if (rejectReport != null) {
                builder.append("Order Replace Rejected: ").append(rejectReport.getText()).append("\n");
            }
        }

        // Handle rejected case
        if (lastEr.getExecType().equals(ExecutionType.REJECTED)) {
            builder.append("Reason for Rejection: ").append(lastEr.getText()).append("\n");
        }
    } else {
        List<ExecutionReport> origList = getReports(testData.getOrigClOrdID());
        ExecutionReport origLastEr = origList.stream().reduce((first, second) -> second).orElse(null);

        if (origLastEr != null) {
            builder.append("Last Execution Report (Original Order): ").append(origLastEr).append("\n");
            builder.append("Original Order Status: ").append(origLastEr.getOrdStatus()).append("\n");
            builder.append("Original Execution Type: ").append(origLastEr.getExecType()).append("\n");
        } else {
            builder.append("No execution reports found for the order.\n");
        }
    }

    builder.append("\n--- End of Details ---");
    return builder;
}
```
