```java
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationMapperTest {

    private final ApplicationMapper mapper = Mappers.getMapper(ApplicationMapper.class);

    @Test
    void shouldMapEntityToDto() {
        Application app = new Application();
        app.setId(1L);
        app.setName("MyApp");
        app.setCreated(Instant.now());

        ApplicationDTO dto = mapper.toDto(app);

        assertEquals(app.getId(), dto.getId());
        assertEquals(app.getName(), dto.getName());
        assertEquals(app.getCreated(), dto.getCreated());
    }

    @Test
    void shouldMapDtoToEntity() {
        ApplicationDTO dto = new ApplicationDTO("MyApp");
        dto.setId(1L);
        dto.setCreated(Instant.now());

        Application app = mapper.toEntity(dto);

        assertEquals(dto.getId(), app.getId());
        assertEquals(dto.getName(), app.getName());
        assertEquals(dto.getCreated(), app.getCreated());

        // Ensure ignored relationships are null or empty
        assertNull(app.getAppTestClasses());
        assertNull(app.getTestLaunches());
        assertNull(app.getAppTestFeatures());
        assertNull(app.getApplicationComponents());
    }
}
```
