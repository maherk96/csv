```java
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionMapperTest {

    private final ExceptionMapper mapper = Mappers.getMapper(ExceptionMapper.class);

    @Test
    void shouldMapEntityToDto() {
        Exception entity = new Exception();
        entity.setId(1L);
        entity.setException("oops".getBytes());

        ExceptionDTO dto = mapper.toDto(entity);

        assertEquals(1L, dto.getId());
        assertArrayEquals("oops".getBytes(), dto.getException());
    }

    @Test
    void shouldMapDtoToEntity() {
        ExceptionDTO dto = new ExceptionDTO();
        dto.setId(2L);
        dto.setException("whoops".getBytes());

        Exception entity = mapper.toEntity(dto);

        assertEquals(2L, entity.getId());
        assertArrayEquals("whoops".getBytes(), entity.getException());
    }
}
```
