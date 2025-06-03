```java
/**
 * Processes a list of test steps for a given scenario and test run.
 *
 * @param steps      The list of steps associated with the scenario
 * @param testId     The ID of the parent test (TEST.ID)
 * @param scenario   The scenario being executed
 * @param testRunId  The ID of the associated test run (TEST_RUN.ID)
 */
private void processTestSteps(List<QAPStep> steps, long testId, QAPScenario scenario, long testRunId) {
    if (steps == null || steps.isEmpty()) return;

    steps.forEach(step -> {
        log.info("Processing test step [{}] for scenario [{}]",
                step.getStepName(), scenario.getScenarioName());

        long testStepId = registry.testStepCachedService.getOrCreateTestStepDetails(step.getStepName(), testId);

        createStepRun(step, scenario, testStepId, testRunId);
        createStepData(step, testStepId);
    });
}

/**
 * Persists a single test step execution (TEST_STEP_RUN).
 *
 * @param step        The step that was executed
 * @param scenario    The parent scenario
 * @param testStepId  The ID of the related step definition (TEST_STEP.ID)
 * @param testRunId   The ID of the scenario run (TEST_RUN.ID)
 */
private void createStepRun(QAPStep step, QAPScenario scenario, long testStepId, long testRunId) {
    var stepRunDTO = new TestStepRunDTO();

    // These can be updated later with actual timing
    stepRunDTO.setStartTime(null); // TODO: Capture actual step start time
    stepRunDTO.setEndTime(null);   // TODO: Capture actual step end time

    stepRunDTO.setStatus(ConversionUtil.mapTestCaseStatus(scenario.name()));
    stepRunDTO.setTestStep(testStepId);
    stepRunDTO.setTestRun(testRunId);

    if (TestCaseStatus.FAILED.name().equals(step.getStatus())) {
        stepRunDTO.setException(registry.exceptionService.createExceptionForTestCase(scenario));
    }

    registry.testStepRunService.create(stepRunDTO);
}

/**
 * Persists key-value data associated with a step (TEST_STEP_DATA).
 *
 * @param step        The step that may contain a data table
 * @param testStepId  The ID of the related step definition (TEST_STEP.ID)
 */
private void createStepData(QAPStep step, long testStepId) {
    if (step.getDataTable() == null || step.getDataTable().isEmpty()) return;

    step.getDataTable().forEach(dataRow ->
        dataRow.forEach((key, value) -> {
            var testStepDTO = new TestStepDataDTO();
            testStepDTO.setTestStep(testStepId);
            testStepDTO.setKeyName(key);
            testStepDTO.setValue(value);

            registry.testStepDataService.create(testStepDTO);
        })
    );
}
```
