```java
    private static class EntityHelper {
        static <T> long getOrCreate(
            Supplier<Boolean> existsCheck,
            Supplier<Long> getExistingId,
            Supplier<T> dtoCreator,
            Function<T, Long> persistFunction,
            String logIfExists,
            String logIfNew
        ) {
            if (existsCheck.get()) {
                log.debug(logIfExists);
                return getExistingId.get();
            }

            log.info(logIfNew);
            T dto = dtoCreator.get();
            return persistFunction.apply(dto);
        }
    }
    @Transactional
    public long getOrCreateTestDetails(QAPTest testDescription, long testClassId, String clazzName) {
        var methodName = testDescription.getMethodName();
        var displayName = testDescription.getDisplayName();

        return EntityHelper.getOrCreate(
            () -> checkIfTestCaseExistsInTestClass(methodName, displayName, testClassId),
            () -> testRepository.getTestCaseForTestClass(methodName, displayName, testClassId).getId(),
            () -> TestDTO.createTestDetails(displayName, methodName, testClassId),
            this::createForJUnit,
            String.format("Existing test case found for %s %s %s", methodName, displayName, clazzName),
            String.format("Adding test case %s to test class %s %s", methodName, displayName, clazzName)
        );
    }

    @Transactional
    public long getOrCreateScenarioDetails(QAPScenario testDescription, long testFeatureId, String clazzName) {
        var scenarioName = testDescription.getScenarioName();

        return EntityHelper.getOrCreate(
            () -> checkIfScenarioExistsInFeature(scenarioName, testFeatureId),
            () -> testRepository.getTestScenarioForTestFeature(scenarioName, testFeatureId).getId(),
            () -> TestDTO.createTestDetails(scenarioName, testFeatureId),
            this::createForCucumber,
            String.format("Existing test scenario found for %s %s", scenarioName, clazzName),
            String.format("Adding test scenario %s to feature %s", scenarioName, clazzName)
        );
    }
```
