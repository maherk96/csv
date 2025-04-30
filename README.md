```java
private <E, ID> E resolveReference(
        ID id,
        Function<ID, Optional<E>> finder,
        Supplier<NotFoundException> notFoundSupplier
) {
    return id == null ? null : finder.apply(id).orElseThrow(notFoundSupplier);
}

private void mapToEntity(final TestDTO testDTO, final Test test, boolean isCucumber) {
    test.setDisplayName(testDTO.getDisplayName());
    test.setMethodName(testDTO.getMethodName());

    if (isCucumber) {
        var testFeature = resolveReference(
            testDTO.getTestClass(),  // likely a misnaming in your code – correct if needed
            testFeatureRepository::findById,
            () -> new NotFoundException(String.format("Test feature %s not found.", testDTO.getTestClass()))
        );
        test.setTestFeature(testFeature);
    } else {
        var testClass = resolveReference(
            testDTO.getTestClass(),
            testClassRepository::findById,
            () -> new NotFoundException(String.format("Test class %s not found", testDTO.getTestClass()))
        );
        test.setTestClass(testClass);

        var testFeature = resolveReference(
            testDTO.getTestFeature(),
            testFeatureRepository::findById,
            () -> new NotFoundException(String.format("Test feature %s not found", testDTO.getTestFeature()))
        );
        test.setTestFeature(testFeature);
    }
}

@Transactional
@CacheEvict(cacheNames = "testCache", allEntries = true)
public Long createForJUnit(final TestDTO testDTO) {
    return createAndSave(testDTO, false);
}

@Transactional
@CacheEvict(cacheNames = "featureCache", allEntries = true)
public Long createForCucumber(final TestDTO testDTO) {
    return createAndSave(testDTO, true);
}

private Long createAndSave(final TestDTO testDTO, boolean isCucumber) {
    final var test = new Test();
    mapToEntity(testDTO, test, isCucumber);
    return testRepository.save(test).getId();
}
```
