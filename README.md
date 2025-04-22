```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExceptionMapper {

    ExceptionMapper INSTANCE = Mappers.getMapper(ExceptionMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "exception", source = "exception")
    ExceptionDTO toDto(Exception exception);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "exception", source = "exception")
    Exception toEntity(ExceptionDTO dto);
}
```
