```java
private <T> T resolveReference(
    Supplier<String> idSupplier,
    Function<String, T> cacheGetter,
    Function<String, Optional<T>> dbFetcher,
    String label
) {
    String id = idSupplier.get();
    if (id == null) return null;
    
    T cached = cacheGetter.apply(id);
    if (cached != null) return cached;

    return dbFetcher.apply(id)
        .orElseThrow(() -> new NotFoundException(String.format("%s %s was not found", label, id)));
}

private TestStepRun mapToEntity(final TestStepRunDTO testStepRunDTO, final TestStepRun testStepRun) {
    var exceptionCache = ExceptionCache.getInstance();
    var fixCache = FixCache.getInstance();
    var logCache = LogCache.getInstance();

    testStepRun.setCreated(testStepRunDTO.getCreated());
    testStepRun.setEndTime(testStepRunDTO.getEndTime());
    testStepRun.setStartTime(testStepRunDTO.getStartTime());
    testStepRun.setStatus(testStepRunDTO.getStatus());

    testStepRun.setException(resolveReference(
        testStepRunDTO::getException,
        exceptionCache::get,
        id -> exceptionRepository.findById(id),
        "Exception"
    ));

    testStepRun.setFix(resolveReference(
        testStepRunDTO::getFix,
        fixCache::get,
        id -> fixRepository.findById(id),
        "Fix"
    ));

    testStepRun.setLog(resolveReference(
        testStepRunDTO::getLog,
        logCache::get,
        id -> logRepository.findById(id),
        "Logs"
    ));

    return testStepRun;
}
```
