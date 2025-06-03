```java
/**
 * Handles the completion of each test step within a scenario by recording step details and any data tables.
 *
 * @param event the event indicating the completion of a test step.
 */
private void handleTestStepFinished(TestStepFinished event) {
    Throwable error = event.getResult().getError();

    // Skip hooks like @Before, @After, etc.
    if (!(event.getTestStep() instanceof HookTestStep)) {

        // Convert raw table into typed QAPStepRow objects
        List<QAPStepRow> stepRows = exHelper.extractDataTable(event.getTestStep()).stream()
            .map(row -> {
                int rowIndex = Integer.parseInt(row.get("rowIndex"));
                row.remove("rowIndex"); // Remove from values map
                return new QAPStepRow(rowIndex, row);
            })
            .collect(Collectors.toList());

        // Construct the step entry
        var qapStep = new QAPSteps(
            exHelper.extractStepName(event.getTestStep()),
            event.getResult().getStatus().name(),
            error != null ? ExceptionUtils.getStackTrace(error) : null,
            stepRows
        );

        // Add it to the current scenario
        currentScenario.get().addStep(qapStep);
    }
}

```
