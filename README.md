```java
@Cacheable(cacheNames = "applicationCache", key = "'getApplicationById_' + #id")
public Application getApplicationById(long id) {
    return applicationRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(
                    String.format("Application id %s was not found", id)));
}
```
