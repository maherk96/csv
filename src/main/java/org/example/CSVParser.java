package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility class for parsing CSV files into Java objects.
 * Supports flexible configurations for delimiter, header mapping, error handling, and type conversion.
 */
public class CSVParser {
    private static final Logger log = LoggerFactory.getLogger(CSVParser.class);

    /**
     * Parses a CSV file into a list of objects of the specified type.
     *
     * @param file   the CSV file to parse.
     * @param config the configuration specifying how to parse the CSV file.
     * @param <T>    the type of objects to map each row to.
     * @return a list of parsed objects.
     * @throws CSVParseException if there is an error during parsing.
     */
    public static <T> List<T> parse(File file, CSVParserConfig<T> config) {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(config, "Config cannot be null");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new CSVParseException("CSV file is empty");
            }
            return parseToObjects(reader, headerLine, config);
        } catch (IOException e) {
            log.error("Failed to read file: {}", file.getPath(), e);
            throw new CSVParseException("Failed to read file: " + file.getPath(), e);
        }
    }

    /**
     * Parses the remaining rows of a CSV file into objects of the specified type.
     *
     * @param reader      the reader for the CSV file.
     * @param headerLine  the header line from the CSV file.
     * @param config      the configuration specifying how to parse the file.
     * @param <T>         the type of objects to map each row to.
     * @return a list of parsed objects.
     */
    private static <T> List<T> parseToObjects(BufferedReader reader, String headerLine, CSVParserConfig<T> config) {
        HeaderMapping headerMapping = parseHeaderMapping(headerLine, config);
        List<T> results = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        String line;
        int lineNumber = 1; // Header is line 1
        while (true) {
            try {
                line = reader.readLine();
                lineNumber++;
                if (line == null) break;

                if (config.isSkipEmptyLines() && line.trim().isEmpty()) {
                    continue;
                }

                String[] values = splitCSV(line, config.getDelimiter());

                if (values.length != headerMapping.headers().length) {
                    CSVParseException ex = new CSVParseException("Column count mismatch at line " + lineNumber +
                            ". Expected: " + headerMapping.headers().length + ", Found: " + values.length);
                    if (!handleError(config.getErrorHandlingStrategy(), ex, errorMessages)) {
                        continue;
                    }
                }

                T instance = config.getTargetClass().getDeclaredConstructor().newInstance();

                for (int i = 0; i < headerMapping.headers().length; i++) {
                    String header = headerMapping.headers()[i];
                    Field field = headerMapping.fieldMap().get(header);
                    if (field != null) {
                        String rawValue = config.isTrimFields() ? values[i].trim() : values[i];
                        Object convertedValue = TypeConverter.convert(rawValue, field.getType());
                        field.set(instance, convertedValue);
                    } else if (!config.isIgnoreUnknownColumns()) {
                        CSVParseException ex = new CSVParseException("Unknown column '" + header + "' at line " + lineNumber);
                        if (!handleError(config.getErrorHandlingStrategy(), ex, errorMessages)) {
                            continue;
                        }
                    }
                }

                results.add(instance);
            } catch (ReflectiveOperationException | CSVParseException e) {
                handleError(config.getErrorHandlingStrategy(),
                        new CSVParseException("Error parsing line " + lineNumber, e), errorMessages);
            } catch (IOException e) {
                handleError(config.getErrorHandlingStrategy(),
                        new CSVParseException("IO error at line " + lineNumber, e), errorMessages);
            }
        }

        if (config.getErrorHandlingStrategy() == CSVParserConfig.ErrorHandlingStrategy.COLLECT_ERRORS && !errorMessages.isEmpty()) {
            errorMessages.forEach(log::error);
        }

        return results;
    }

    /**
     * Parses the headers from the header line of a CSV file.
     *
     * @param headerLine the header line from the CSV file.
     * @param config     the configuration specifying how to parse the headers.
     * @return an array of parsed headers.
     * @throws CSVParseException if the header line is empty.
     */
    private static String[] parseHeaders(String headerLine, CSVParserConfig<?> config) {
        if (headerLine == null || headerLine.isEmpty()) {
            throw new CSVParseException("Header line is empty");
        }

        return Arrays.stream(headerLine.split(Pattern.quote(config.getDelimiter())))
                .map(String::trim) // Trim spaces
                .map(String::toLowerCase) // Normalize for case-insensitivity
                .toArray(String[]::new);
    }

    /**
     * Creates a mapping between headers and fields in the target class.
     *
     * @param headerLine the header line from the CSV file.
     * @param config     the configuration specifying the target class and header mapping.
     * @param <T>        the type of the target class.
     * @return a mapping of headers to fields.
     */
    private static <T> HeaderMapping parseHeaderMapping(String headerLine, CSVParserConfig<T> config) {
        String[] headers = parseHeaders(headerLine, config);
        log.info("Parsed Headers: {}", Arrays.toString(headers));
        log.info("Configured Header Mapping: {}", config.getHeaderMapping());

        String[] mappedHeaders = Arrays.stream(headers)
                .map(header -> config.getHeaderMapping().getOrDefault(header, header))
                .toArray(String[]::new);

        Map<String, Field> fieldMap = getFieldMap(config.getTargetClass());
        return new HeaderMapping(mappedHeaders, fieldMap);
    }

    /**
     * Retrieves a map of field names to fields for the given class.
     *
     * @param clazz the target class.
     * @return a map of field names to fields.
     */
    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    /**
     * Splits a CSV line into its individual values using the specified delimiter.
     *
     * @param line      the CSV line to split.
     * @param delimiter the delimiter to use for splitting.
     * @return an array of values.
     * @throws IllegalArgumentException if the delimiter is null or empty.
     */
    private static String[] splitCSV(String line, String delimiter) {
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be null or empty");
        }
        return line.split(Pattern.quote(delimiter));
    }

    /**
     * Handles errors based on the configured error handling strategy.
     *
     * @param strategy       the error handling strategy.
     * @param exception      the exception to handle.
     * @param errorMessages  a list to collect error messages if the strategy is COLLECT_ERRORS.
     * @return {@code false} if the strategy is CONTINUE_ON_ERROR, otherwise throws the exception.
     */
    private static <T> boolean handleError(CSVParserConfig.ErrorHandlingStrategy strategy, CSVParseException exception, List<String> errorMessages) {
        switch (strategy) {
            case CONTINUE_ON_ERROR:
                log.warn("Parsing error: {}", exception.getMessage());
                return false;
            case HALT_ON_ERROR:
                throw exception;
            case COLLECT_ERRORS:
                if (errorMessages != null) {
                    errorMessages.add(exception.getMessage());
                }
                log.error("Parsing error: {}", exception.getMessage());
                return false;
            default:
                throw new IllegalArgumentException("Unknown ErrorHandlingStrategy: " + strategy);
        }
    }

    /**
     * Represents a mapping between headers and fields in the target class.
     */
    private record HeaderMapping(String[] headers, Map<String, Field> fieldMap) {
    }
}