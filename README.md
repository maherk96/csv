```java
@Cacheable(cacheNames = "testCache", key = "'scenario_' + #scenarioName + '_' + #testFeatureId")
public Test getTestScenarioForTestFeature(...) { ... }

@Cacheable(cacheNames = "testCache", key = "'method_' + #methodName + '_' + #displayName + '_' + #testClassId")
public Test getTestCaseForTestClass(...) { ... }
```
