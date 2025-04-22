```java
private static StatusSummary summariseStatuses(List<QAPAbstractTestCase> cases) {
    EnumMap<TestcaseStatus, Long> counts = cases.stream()
        .collect(Collectors.groupingBy(
                QAPAbstractTestCase::getStatus,
                () -> new EnumMap<>(TestcaseStatus.class),
                Collectors.counting()));

    return new StatusSummary(
            counts.getOrDefault(TestcaseStatus.PASSED,   0L).intValue(),
            counts.getOrDefault(TestcaseStatus.FAILED,   0L).intValue(),
            counts.getOrDefault(TestcaseStatus.ABORTED,  0L).intValue(),
            counts.getOrDefault(TestcaseStatus.DISABLED, 0L).intValue(),
            counts.getOrDefault(TestcaseStatus.IGNORED,  0L).intValue());
}

/** Tiny helper record – Java 16+ */
private record StatusSummary(int passed, int failed, int aborted,
                             int disabled, int ignored) {
    int total() { return passed + failed + aborted + disabled + ignored; }
}
private TestLaunchDtoBuilder populateCommonFields(QAPHeader launch,
                                                  StatusSummary s,
                                                  long app, long user, long env) {
    return TestLaunchDto.builder()
            .total(s.total())
            .passed(s.passed).failed(s.failed).aborted(s.aborted)
            .disabled(s.disabled).ignored(s.ignored)
            .launchUuid(launch.getLaunchId())
            .ciBranch(launch.getBranch())
            .regression(launch.isRegression())
            .app(app).user(user).env(env)
            .startTime(convertInstantToOffsetDateTime(launch.getLaunchStartTime()))
            .endTime(convertInstantToOffsetDateTime(launch.getLaunchEndTime()))
            .jdkVersion(launch.getJdkVersion())
            .osVersion(launch.getOsVersion())
            .testRunnerVersion(launch.getTestRunnerVersion());
}

private Long createLaunch(QAPHeader launch,
                          List<QAPAbstractTestCase> cases,
                          long app, long user, long env,
                          Consumer<TestLaunchDtoBuilder> extra) {

    StatusSummary s = summariseStatuses(cases);
    TestLaunchDtoBuilder builder = populateCommonFields(launch, s, app, user, env);
    extra.accept(builder);               // e.g. b -> b.testClass(id)  OR  b -> b.testFeature(id)
    return create(builder.build());
}
```
