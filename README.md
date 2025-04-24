```java
@Transactional
@Caching(evict = {
    @CacheEvict(cacheNames = "appsCache", key = "'name_' + #applicationDTO.name"),
    @CacheEvict(cacheNames = "appsCache", key = "'id_' + #result"),
    @CacheEvict(cacheNames = "appsCache", key = "'dto_' + #result")
})
public Long create(final ApplicationDTO applicationDTO) {
    final Application application = new Application();
    mapToEntity(applicationDTO, application);
    Long result = applicationRepository.save(application).getId();
    return result;
}
```
