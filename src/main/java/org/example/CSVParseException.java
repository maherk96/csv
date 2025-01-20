package org.example;

/**
 * Custom exception class for handling CSV parsing errors.
 * This exception is thrown when an error occurs during the parsing of a CSV file.
 */
public class CSVParseException extends RuntimeException {

    /**
     * Constructs a new {@code CSVParseException} with the specified detail message.
     *
     * @param message the detail message describing the error.
     */
    public CSVParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CSVParseException} with the specified detail message and cause.
     *
     * @param message the detail message describing the error.
     * @param cause   the cause of the error (can be {@code null}).
     */
    public CSVParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code CSVParseException} with the specified cause.
     *
     * @param cause the cause of the error (can be {@code null}).
     */
    public CSVParseException(Throwable cause) {
        super(cause);
    }
}