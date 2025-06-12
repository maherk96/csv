```java
private static String buildEnumBlock(String tableName, List<String> columnNames) {
    String enumName = tableName.toUpperCase();
    String tablePrefix = enumName + "_";

    StringBuilder sb = new StringBuilder();
    sb.append("    public enum ").append(enumName).append(" {\n");

    String joinedColumns = columnNames.stream()
        .map(col -> tablePrefix + col.toUpperCase())
        .collect(Collectors.joining(", "));

    sb.append("        ").append(joinedColumns).append(";\n");
    sb.append("    }\n");
    return sb.toString();
}
```
