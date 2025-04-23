```java
@Transactional
@Cacheable(cacheNames = "appsCache", key = "'applicationEntityByName_' + #applicationName")
public Application getOrCreateApplicationEntity(String applicationName) {
    if (applicationRepository.existsByNameIgnoreCase(applicationName)) {
        log.debug("Application entity exists {}", applicationName);

        return applicationRepository.findByNameIgnoreCase(applicationName)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Application entity %s was not found", applicationName))
                );
    }

    log.info("Application {} does not exist, creating new one", applicationName);

    Application app = new Application();
    app.setName(applicationName);

    return applicationRepository.save(app);
}
```
