```java
public static <T> Long createIfPresent(
    T entity,
    Predicate<T> hasContentPredicate,
    Function<T, Long> creator
) {
    return hasContentPredicate.test(entity) ? creator.apply(entity) : null;
}
```
