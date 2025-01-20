# CSV Parser

## Overview
This project provides a flexible CSV parsing library for Java. It allows for converting CSV files into Java objects with various configurable options like custom delimiters, header mappings, error handling strategies, and support for built-in or custom type converters.

- Parse CSV files into Java objects.
- Support for custom delimiters (e.g., `,`, `|`, etc.).
- Flexible header-to-field mapping for case-insensitivity or custom column mapping.
- Options to skip empty lines and trim whitespace from fields.
- Built-in error handling strategies:
    - Continue on error
    - Halt on error
    - Collect errors
- Built-in type converters for common data types like `String`, `Integer`, `LocalDate`, etc.
- Support for registering custom type converters for complex or user-defined types.

## Usage

### Example CSV File
```csv
currency pair,bid low price,bid upper price,offer low price,offer upper price,num. of rungs bid,num. of rungs offer
EUR/USD,1.1,1.2,1.3,1.4,5,6
GBP/USD,1.5,1.6,1.7,1.8,7,8
```

Basic Usage

```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withDelimiter(",") // Specify the delimiter
    .withTrimFields(true) // Remove leading/trailing whitespace
    .withHeaderMapping(Map.of(
        "currency pair", "currencyPair",
        "bid low price", "bidLowPrice",
        "bid upper price", "bidUpperPrice",
        "offer low price", "offerLowPrice",
        "offer upper price", "offerUpperPrice",
        "num. of rungs bid", "numOfRungsBid",
        "num. of rungs offer", "numOfRungsOffer"
    ))
    .build();

List<CurrencyPair> currencyPairs = CSVParser.parse(csvFile, config);
```

Configuration Options

Delimiters
	•	Default: ,
	•	Use a custom delimiter when your CSV file uses a non-standard character like | or ;.

```java
config.withDelimiter("|");
```

Trimming Fields
	•	Default: true
	•	Enable trimming to remove extra spaces around field values:

```java
config.withTrimFields(true);
```


Skip Empty Lines
	•	Default: true
	•	Skip blank rows in the CSV file:

```java
config.withSkipEmptyLines(true);
```


Ignore Unknown Columns
	•	Default: false
	•	Use when your CSV has extra columns not mapped to fields in the target class:

```java
config.withIgnoreUnknownColumns(true);
```


Error Handling Strategies

	1.	CONTINUE_ON_ERROR: Logs errors and skips problematic rows.
    2.	HALT_ON_ERROR: Stops parsing immediately upon encountering an error.
    3.	COLLECT_ERRORS: Collects all errors and logs them at the end.

```java
config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.CONTINUE_ON_ERROR);
config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.HALT_ON_ERROR);
config.withErrorHandlingStrategy(CSVParserConfig.ErrorHandlingStrategy.COLLECT_ERRORS);
```

Type Conversion

Built-in Converters

The library supports the following data types by default:
	•	Primitive types: int, double, boolean, etc.
	•	Wrapper types: Integer, Double, Boolean, etc.
	•	Common classes: String, BigDecimal, LocalDate, LocalDateTime

Custom Converters

Use a custom converter for:
	•	Custom or user-defined types.
	•	Complex transformations like parsing custom date formats or converting text into objects.

Example:

```java
TypeConverter.registerConverter(MyCustomType.class, MyCustomType::fromString);

CSVParserConfig<MyCustomType> config = new CSVParserConfig.Builder<>(MyCustomType.class).build();
```

Parsing a CSV with Custom Delimiter

```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withDelimiter("|") // Use '|' as the delimiter
    .build();


List<CurrencyPair> currencyPairs = CSVParser.parse(customDelimiterFile, config);
```

Handling Empty Lines

```java
CSVParserConfig<CurrencyPair> config = new CSVParserConfig.Builder<>(CurrencyPair.class)
    .withSkipEmptyLines(true) // Ignore empty rows
    .build();
```

Custom Header Mapping

Map CSV headers to Java fields:

```java
config.withHeaderMapping(Map.of(
    "currency pair", "currencyPair",
    "bid low price", "bidLowPrice"
));
```
