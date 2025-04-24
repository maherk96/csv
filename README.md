```java
@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableCaching
@ContextConfiguration(classes = {
    ComponentServiceTest.TestCacheConfig.class,
    ComponentService.class
})
public class ComponentServiceTest {

    @MockBean
    private ComponentRepository componentRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testGetOrCreateComponent_isCached() {
        String componentName = "ComponentX";
        long appId = 200L;
        long expectedId = 5000L;

        ComponentDTO dto = new ComponentDTO();
        dto.setComponentName(componentName);
        dto.setId(expectedId);

        Mockito.when(componentRepository.findByComponentName(componentName)).thenReturn(dto);
        Mockito.when(componentRepository.findAll()).thenReturn(List.of(dto));

        // First call — hits the service
        long id1 = componentService.getOrCreateComponent(componentName, appId);
        // Second call — should use cache
        long id2 = componentService.getOrCreateComponent(componentName, appId);

        Assertions.assertEquals(expectedId, id1);
        Assertions.assertEquals(expectedId, id2);

        Mockito.verify(componentRepository, Mockito.times(1)).findByComponentName(componentName);
    }

    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        public CacheManager cacheManager() {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager("componentsCache");
            cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES));
            return cacheManager;
        }

        @Bean
        public ComponentService componentService(ComponentRepository repo) {
            return new ComponentService(repo);
        }
    }
}
```
