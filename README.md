```java

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
@ContextConfiguration(classes = {
    ApplicationResolver.class,
    TestCacheConfig.class
})
class ApplicationResolverTest {

    @Autowired
    private ApplicationResolver resolver;

    @MockBean
    private ApplicationService service;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void resolve_shouldUseCacheAfterFirstCall() {
        // given
        String name = "MyApp";
        long expectedId = 123L;

        // mock service
        when(service.getOrCreateApp(name)).thenReturn(expectedId);

        // when - first call: triggers the service
        long id1 = resolver.resolve(name);
        assertEquals(expectedId, id1);

        // when - second call: should hit cache, not the service
        long id2 = resolver.resolve(name);
        assertEquals(expectedId, id2);

        // then
        verify(service, times(1)).getOrCreateApp(name); // only 1 service call due to caching
    }
}
```
