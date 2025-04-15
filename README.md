```sql
public static <T> boolean isValid(String value, Class<T> clazz) {
    try {
        getObjectMapper().readValue(value, clazz);
        return true;
    } catch (JsonProcessingException e) {
        log.error("Deserialization failed for class {}. Payload: {}\nError: {}\nException type: {}",
                clazz.getSimpleName(), value, e.getOriginalMessage(), e.getClass().getSimpleName());

        // For specific types, show more details
        if (e instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException unrecognized) {
            log.error("Unrecognized field: {}", unrecognized.getPropertyName());
        } else if (e instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mismatch) {
            log.error("Mismatched input at path: {}", mismatch.getPathReference());
        }

        return false;
    }
}

```
