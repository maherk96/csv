```java
private void mapToEntity(final TestRunDTO testRunDTO, final TestRun testRun) {
    var exceptionCache = ExceptionCache.getInstance();
    var fixCache = FixCache.getInstance();
    var logCache = LogCache.getInstance();

    testRun.setStartTime(testRunDTO.getStartTime());
    testRun.setEndTime(testRunDTO.getEndTime());
    testRun.setStatus(testRunDTO.getStatus());
    testRun.setCreated(testRunDTO.getCreated());

    testRun.setException(resolveReference(
        testRunDTO::getException,
        exceptionCache::get,
        exceptionRepository::findById,
        "Exception"
    ));

    testRun.setFix(resolveReference(
        testRunDTO::getFix,
        fixCache::get,
        fixRepository::findById,
        "Fix"
    ));

    testRun.setLog(resolveReference(
        testRunDTO::getLog,
        logCache::get,
        logRepository::findById,
        "Log"
    ));

    final var test = testRunDTO.getTest() == null
        ? null
        : testCachedService.getTestById(testRunDTO.getTest());
    testRun.setTest(test);

    final var testLaunch = testRunDTO.getTestLaunch() == null
        ? null
        : testLaunchCachedService.getLaunchById(testRunDTO.getTestLaunch());
    testRun.setTestLaunch(testLaunch);
}
```
