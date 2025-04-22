```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    // Map entity to DTO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "created", source = "created")
    @Mapping(target = "name", source = "name")
    ApplicationDTO toDto(Application app);

    // Map DTO to entity
    @Mapping(target = "id", source = "id")
    @Mapping(target = "created", source = "created")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "appTestClasses", ignore = true)
    @Mapping(target = "testLaunches", ignore = true)
    @Mapping(target = "appTestFeatures", ignore = true)
    @Mapping(target = "applicationComponents", ignore = true)
    Application toEntity(ApplicationDTO dto);
}
```
