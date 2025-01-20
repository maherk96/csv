package org.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration class for the {@link CSVParser}.
 * Allows customization of parsing behavior, such as delimiter, header mapping, error handling strategies, and more.
 *
 * @param <T> the type of objects to map each CSV row to.
 */
public class CSVParserConfig<T> {
    private final Class<T> targetClass;
    private final String delimiter;
    private final boolean skipEmptyLines;
    private final boolean trimFields;
    private final Map<String, String> headerMapping;
    private final boolean ignoreUnknownColumns;
    private final ErrorHandlingStrategy errorHandlingStrategy;

    private CSVParserConfig(Builder<T> builder) {
        this.targetClass = builder.targetClass;
        this.delimiter = builder.delimiter;
        this.skipEmptyLines = builder.skipEmptyLines;
        this.trimFields = builder.trimFields;
        this.headerMapping = Collections.unmodifiableMap(new HashMap<>(builder.headerMapping));
        this.ignoreUnknownColumns = builder.ignoreUnknownColumns;
        this.errorHandlingStrategy = builder.errorHandlingStrategy;
    }

    /**
     * @return the target class to which CSV rows are mapped.
     */
    public Class<T> getTargetClass() {
        return targetClass;
    }

    /**
     * @return the delimiter used to separate values in the CSV.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * @return {@code true} if empty lines should be skipped, {@code false} otherwise.
     */
    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    /**
     * @return {@code true} if fields should be trimmed of whitespace, {@code false} otherwise.
     */
    public boolean isTrimFields() {
        return trimFields;
    }

    /**
     * @return a map of CSV headers to target object field names.
     */
    public Map<String, String> getHeaderMapping() {
        return headerMapping;
    }

    /**
     * @return {@code true} if unknown columns should be ignored, {@code false} otherwise.
     */
    public boolean isIgnoreUnknownColumns() {
        return ignoreUnknownColumns;
    }

    /**
     * @return the strategy for handling parsing errors.
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return errorHandlingStrategy;
    }

    /**
     * Builder class for constructing instances of {@link CSVParserConfig}.
     *
     * @param <T> the type of objects to map each CSV row to.
     */
    public static class Builder<T> {
        private final Class<T> targetClass;
        private String delimiter = ",";
        private boolean skipEmptyLines = true;
        private boolean trimFields = true;
        private Map<String, String> headerMapping = new HashMap<>();
        private boolean ignoreUnknownColumns = false;
        private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.CONTINUE_ON_ERROR;

        /**
         * Creates a new builder for {@link CSVParserConfig}.
         *
         * @param targetClass the target class to which CSV rows are mapped.
         * @throws NullPointerException if {@code targetClass} is {@code null}.
         */
        public Builder(Class<T> targetClass) {
            this.targetClass = Objects.requireNonNull(targetClass, "Target class cannot be null");
        }

        /**
         * Sets the delimiter to use for splitting CSV rows.
         *
         * @param delimiter the delimiter to use (e.g., "," or "|").
         * @return this builder instance.
         * @throws IllegalArgumentException if {@code delimiter} is null or empty.
         */
        public Builder<T> withDelimiter(String delimiter) {
            if (delimiter == null || delimiter.isEmpty()) {
                throw new IllegalArgumentException("Delimiter cannot be null or empty");
            }
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Sets whether empty lines should be skipped during parsing.
         *
         * @param skipEmptyLines {@code true} to skip empty lines, {@code false} otherwise.
         * @return this builder instance.
         */
        public Builder<T> withSkipEmptyLines(boolean skipEmptyLines) {
            this.skipEmptyLines = skipEmptyLines;
            return this;
        }

        /**
         * Sets whether fields should be trimmed of leading and trailing whitespace.
         *
         * @param trimFields {@code true} to trim fields, {@code false} otherwise.
         * @return this builder instance.
         */
        public Builder<T> withTrimFields(boolean trimFields) {
            this.trimFields = trimFields;
            return this;
        }

        /**
         * Sets a mapping of CSV headers to target object field names.
         *
         * @param headerMapping the mapping of headers to field names.
         * @return this builder instance.
         */
        public Builder<T> withHeaderMapping(Map<String, String> headerMapping) {
            this.headerMapping = new HashMap<>(headerMapping);
            return this;
        }

        /**
         * Sets whether unknown columns in the CSV file should be ignored.
         *
         * @param ignoreUnknownColumns {@code true} to ignore unknown columns, {@code false} otherwise.
         * @return this builder instance.
         */
        public Builder<T> withIgnoreUnknownColumns(boolean ignoreUnknownColumns) {
            this.ignoreUnknownColumns = ignoreUnknownColumns;
            return this;
        }

        /**
         * Sets the strategy for handling parsing errors.
         *
         * @param strategy the error handling strategy to use.
         * @return this builder instance.
         * @throws NullPointerException if {@code strategy} is {@code null}.
         */
        public Builder<T> withErrorHandlingStrategy(ErrorHandlingStrategy strategy) {
            this.errorHandlingStrategy = Objects.requireNonNull(strategy, "ErrorHandlingStrategy cannot be null");
            return this;
        }

        /**
         * Builds and returns a new {@link CSVParserConfig} instance.
         *
         * @return the constructed {@link CSVParserConfig}.
         */
        public CSVParserConfig<T> build() {
            return new CSVParserConfig<>(this);
        }
    }

    /**
     * Enum representing strategies for handling parsing errors.
     */
    public enum ErrorHandlingStrategy {
        /**
         * Continue parsing even if errors occur. Errors are logged as warnings.
         */
        CONTINUE_ON_ERROR,

        /**
         * Halt parsing immediately when an error occurs.
         */
        HALT_ON_ERROR,

        /**
         * Collect all errors and log them after parsing is complete.
         */
        COLLECT_ERRORS
    }
}