```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ComponentMapper {

    ComponentMapper INSTANCE = Mappers.getMapper(ComponentMapper.class);

    @Mapping(target = "application", source = "application.id")
    ComponentDTO toDto(Component component);

    @Mapping(target = "application.id", source = "application")
    @Mapping(target = "componentComponentVersions", ignore = true)
    @Mapping(target = "created", ignore = true)
    Component toEntity(ComponentDTO dto);
}

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ComponentMapperTest {

    private final ComponentMapper mapper = Mappers.getMapper(ComponentMapper.class);

    @Test
    void shouldMapEntityToDto() {
        Application app = new Application();
        app.setId(42L);

        Component component = new Component();
        component.setId(1L);
        component.setComponentName("API Service");
        component.setApplication(app);
        component.setCreated(Instant.now());

        ComponentDTO dto = mapper.toDto(component);

        assertEquals(component.getId(), dto.getId());
        assertEquals(component.getComponentName(), dto.getComponentName());
        assertEquals(42L, dto.getApplication());
    }

    @Test
    void shouldMapDtoToEntity() {
        ComponentDTO dto = new ComponentDTO("Database", 88L);
        dto.setId(5L);

        Component component = mapper.toEntity(dto);

        assertEquals(dto.getId(), component.getId());
        assertEquals("Database", component.getComponentName());
        assertNotNull(component.getApplication());
        assertEquals(88L, component.getApplication().getId());
    }
}
```
