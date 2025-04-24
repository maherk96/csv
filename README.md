```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CacheTestConfig.class }) // define beans here
@EnableCaching
public class ApplicationCachedServiceTest {

    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private ApplicationCachedService applicationCachedService;

    @Test
    void testGetOrCreateApplication_isCached() {
        String appName = "TestApp";
        long expectedId = 123L;

        // Mock the application service to return the ID
        Mockito.when(applicationService.getOrCreateApp(appName)).thenReturn(expectedId);

        // First call - should invoke the real service
        long firstCall = applicationCachedService.getOrCreateApplication(appName);

        // Second call - should use cache
        long secondCall = applicationCachedService.getOrCreateApplication(appName);

        // Assert both are equal
        Assertions.assertEquals(expectedId, firstCall);
        Assertions.assertEquals(expectedId, secondCall);

        // Verify the method was only called once
        Mockito.verify(applicationService, Mockito.times(1)).getOrCreateApp(appName);
    }

    @Test
    void testFindById_isCached() {
        Long id = 999L;
        Application app = new Application();
        // Add necessary properties if needed

        Mockito.when(applicationService.findByID(id)).thenReturn(app);

        Application result1 = applicationCachedService.findById(id);
        Application result2 = applicationCachedService.findById(id);

        Assertions.assertEquals(result1, result2);
        Mockito.verify(applicationService, Mockito.times(1)).findByID(id);
    }
}
```
