```java
private static String buildEnumBlock(String tableName, List<String> columnNames) {
    String enumName = tableName.toUpperCase();

    String enumConstants = columnNames.stream()
        .map(col -> String.format("        %s_%s(\"%s\")", enumName, col.toUpperCase(), col))
        .collect(Collectors.joining(",\n"));

    return """
            public enum %s implements DatabaseColumn {
            %s;

                private final String columnName;

                %s(String columnName) {
                    this.columnName = columnName;
                }

                @Override
                public String columnName() {
                    return columnName;
                }
            }
            """.formatted(enumName, enumConstants, enumName);
}
```
