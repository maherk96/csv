package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for converting strings into various data types.
 * Provides built-in converters for standard types and allows for custom converters to be registered.
 */
public class TypeConverter {
    private static final Map<Class<?>, Function<String, ?>> CONVERTERS = new HashMap<>();

    static {
        // Register default converters for common types
        CONVERTERS.put(String.class, str -> str);
        CONVERTERS.put(Integer.class, Integer::valueOf);
        CONVERTERS.put(int.class, Integer::valueOf);
        CONVERTERS.put(Long.class, Long::valueOf);
        CONVERTERS.put(long.class, Long::valueOf);
        CONVERTERS.put(Double.class, Double::valueOf);
        CONVERTERS.put(double.class, Double::valueOf);
        CONVERTERS.put(Float.class, Float::valueOf);
        CONVERTERS.put(float.class, Float::valueOf);
        CONVERTERS.put(Boolean.class, Boolean::valueOf);
        CONVERTERS.put(boolean.class, Boolean::valueOf);
        CONVERTERS.put(BigDecimal.class, BigDecimal::new);
        CONVERTERS.put(LocalDate.class, LocalDate::parse);
        CONVERTERS.put(LocalDateTime.class, LocalDateTime::parse);
    }

    /**
     * Registers a custom converter for the specified type.
     *
     * @param type      the class of the target type.
     * @param converter a function that converts a {@link String} to the target type.
     * @param <T>       the target type.
     */
    public static <T> void registerConverter(Class<T> type, Function<String, T> converter) {
        CONVERTERS.put(type, converter);
    }

    /**
     * Converts a string value to the specified target type using a registered converter.
     *
     * @param value      the string value to convert.
     * @param targetType the class of the target type.
     * @param <T>        the target type.
     * @return the converted value, or the default value for primitive types if the input is null or empty.
     * @throws CSVParseException if no converter is registered for the target type or if the conversion fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> targetType) {
        if (value == null || value.isEmpty()) {
            if (targetType.isPrimitive()) {
                return (T) getDefaultPrimitiveValue(targetType);
            }
            return null;
        }

        Function<String, ?> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            throw new CSVParseException("Unsupported type conversion: " + targetType.getName());
        }

        try {
            return (T) converter.apply(value);
        } catch (Exception e) {
            throw new CSVParseException("Failed to convert value '" + value + "' to type " + targetType.getName(), e);
        }
    }

    /**
     * Returns the default value for the specified primitive type.
     *
     * @param type the class of the primitive type.
     * @return the default value of the primitive type.
     * @throws IllegalArgumentException if the type is not a primitive type.
     */
    private static Object getDefaultPrimitiveValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == char.class) return '\u0000';
        throw new IllegalArgumentException("Not a primitive type: " + type.getName());
    }
}