```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Context;

@Mapper(componentModel = "spring")
public interface ComponentMapper {

    ComponentMapper INSTANCE = Mappers.getMapper(ComponentMapper.class);

    // DTO ➜ Entity (uses ApplicationResolver to fetch Application)
    @Mapping(target = "application", expression = "java(applicationResolver.resolve(dto.getApplication()))")
    @Mapping(target = "componentComponentVersions", ignore = true)
    @Mapping(target = "created", ignore = true)
    Component toEntity(ComponentDTO dto, @Context ApplicationResolver applicationResolver);

    // Entity ➜ DTO
    @Mapping(target = "application", source = "application.id")
    ComponentDTO toDto(Component component);
}
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationResolver {

    private final ApplicationRepository repo;

    /** Returns the Application entity or throws if not found. */
    @Cacheable(value = "applicationCache", key = "#id")
    public Application resolve(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));
    }
}
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
@ContextConfiguration(classes = {
    ApplicationResolver.class, 
    TestCacheConfig.class  // see below
})
class ApplicationResolverTest {

    @Autowired
    private ApplicationResolver resolver;

    @Autowired
    private ApplicationRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void resolve_shouldUseCacheAfterFirstCall() {
        Application app = new Application();
        app.setId(1L);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(app));

        // First call - should hit repo
        Application result1 = resolver.resolve(1L);
        assertEquals(app, result1);

        // Second call - should come from cache
        Application result2 = resolver.resolve(1L);
        assertEquals(app, result2);

        // Verify only one call to repo
        verify(repository, times(1)).findById(1L);
    }
}
```
