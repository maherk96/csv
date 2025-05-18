```java
public class ScenarioContext {
    public final QAPScenario scenario;
    public final long launchId;
    public final long testFeatureIdForApp;
    public final long userId;
    public final long envId;
    public final String testLaunch;
    public final String featureName;

    public ScenarioContext(QAPScenario scenario, long launchId, long testFeatureIdForApp,
                           long userId, long envId, String testLaunch, String featureName) {
        this.scenario = scenario;
        this.launchId = launchId;
        this.testFeatureIdForApp = testFeatureIdForApp;
        this.userId = userId;
        this.envId = envId;
        this.testLaunch = testLaunch;
        this.featureName = featureName;
    }
}

public void processScenario(ScenarioContext context) {
    log.info("Processing test case [{}]", context.scenario.getScenarioName());

    try {
        long testId = createScenarioDetails(context);
        long logId = registry.logService.createLogsForTestCase(context.scenario);

        processTestSteps(context.scenario.getSteps(), testId, context.scenario);

        TestDetails testDetails = createTestDetails(context, testId, logId);

        if (TestCaseStatus.FAILED.name().equals(context.scenario.getStatus())) {
            long exceptionId = registry.exceptionService.createExceptionForTestCase(context.scenario);
            testDetails.setExceptionId(exceptionId);
        }

        if (hasTags(context.scenario)) {
            createTags(context.scenario.getTags(), testId, context.launchId);
        }

        registry.testRunService.create(testDetails);
        log.info("Persisted test case [{}] to {}", context.scenario.getScenarioName(), context.testLaunch);

    } catch (DatabaseException e) {
        log.error("Database error while processing test case {}", context.scenario, e);
    } catch (Exception e) {
        log.error("Error processing test case {}", context.scenario, e);
        throw new IllegalStateException(e);
    }
}

private void processTestSteps(List<TestStep> steps, long testId, QAPScenario scenario) {
    if (steps == null || steps.isEmpty()) return;

    steps.stream()
         .map(step -> {
             var testStep = TestStepDTO.createTestStep(step.getStatus(), step.getStepName(), testId, null);

             if (TestCaseStatus.FAILED.name().equals(step.getStatus())) {
                 long exceptionId = registry.exceptionService.createExceptionForTestCase(scenario);
                 testStep.setExceptionId(exceptionId);
             }

             return testStep;
         })
         .forEach(registry.testStepService::create);
}

private long createScenarioDetails(ScenarioContext ctx) {
    return registry.testCachedService.getOrCreateScenarioDetails(
        ctx.scenario, ctx.testFeatureIdForApp, ctx.featureName
    );
}

private void processTestSteps(List<TestStep> steps, long testId, QAPScenario scenario) {
    if (steps == null || steps.isEmpty()) return;

    for (var step : steps) {
        var testStep = TestStepDTO.createTestStep(step.getStatus(), step.getStepName(), testId, null);

        if (TestCaseStatus.FAILED.name().equals(step.getStatus())) {
            long exceptionId = registry.exceptionService.createExceptionForTestCase(scenario);
            testStep.setExceptionId(exceptionId);
        }

        registry.testStepService.create(testStep);
    }
}

private TestDetails createTestDetails(ScenarioContext ctx, long testId, long logId) {
    return registry.testRunService.createTestCaseDetails(
        ctx.scenario,
        ConversionUtil.mapTestCaseStatus(ctx.scenario),
        ctx.envId,
        logId,
        ctx.launchId,
        ctx.userId
    );
}

private boolean hasTags(QAPScenario scenario) {
    return scenario.getTags() != null && !scenario.getTags().isEmpty();
}
```
