```java
/**
 * Persists key-value data associated with a step (TEST_STEP_DATA).
 *
 * @param step        The step that may contain a data table.
 * @param testStepId  The ID of the related step definition (TEST_STEP.ID).
 */
private void createStepData(QAPSteps step, long testStepId) {
    if (step.getDataTable() == null || step.getDataTable().isEmpty()) return;

    step.getDataTable().forEach(dataRow -> {
        int rowIndex = dataRow.getRowIndex();
        dataRow.getValues().forEach((key, value) -> {
            var testStepDTO = new TestStepDataDTO();
            testStepDTO.setTestStep(testStepId);
            testStepDTO.setKeyName(key);
            testStepDTO.setValue(value);
            testStepDTO.setRowIndex(rowIndex); 
            registry.testStepDataService.create(testStepDTO);
        });
    });
}

```
