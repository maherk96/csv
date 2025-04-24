```java
    @Test
    void testCacheEvictedAfterCreate() {
        String appName = "EvictApp";
        Long appId = 1234L;

        Application application = new Application();
        application.setId(appId);
        application.setName(appName);

        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(appId);
        dto.setName(appName);

        // Step 1: Seed the cache
        Mockito.when(applicationService.getOrCreateApp(appName)).thenReturn(appId);
        applicationCachedService.getOrCreateApplication(appName);
        cacheManager.getCache("appsCache").put("id_" + appId, application);
        cacheManager.getCache("appsCache").put("dto_" + appId, dto);

        Assertions.assertNotNull(getFromCache("name_" + appName));
        Assertions.assertNotNull(getFromCache("id_" + appId));
        Assertions.assertNotNull(getFromCache("dto_" + appId));

        // Step 2: Mock the save
        Mockito.when(applicationRepository.save(Mockito.any(Application.class))).thenReturn(application);

        // Step 3: Call create, which is annotated with @CacheEvict
        ApplicationService realService = new ApplicationService(applicationRepository);
        realService.create(dto);

        // Step 4: Assert cache is evicted
        Assertions.assertNull(getFromCache("name_" + appName));
        Assertions.assertNull(getFromCache("id_" + appId));
        Assertions.assertNull(getFromCache("dto_" + appId));
    }

    // Helper to fetch from cache
    private Object getFromCache(String key) {
        return cacheManager.getCache("appsCache").get(key, Object.class);
    }
```
