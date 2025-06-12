```java
package com.citi.fx.qa.qap;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility that connects to the Oracle database using JDBC,
 * reads all table and column names from the QAPORTAL schema,
 * and generates a static Java enum container class (`QAPTables`)
 * to be used for type-safe column references in test assertions.
 */
public class QAPEnumGenerator {

    public static void main(String[] args) throws Exception {
        DataSource dataSource = DataSourceFactory.create();
        String schema = "QAPORTAL";
        String outputFile = "src/main/java/com/citi/fx/qa/qap/QAPTables.java";

        List<String> enumBlocks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, schema, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                ResultSet columns = metaData.getColumns(null, schema, tableName, "%");

                List<String> columnNames = new ArrayList<>();
                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }

                if (!columnNames.isEmpty()) {
                    String enumBlock = buildEnumBlock(tableName, columnNames);
                    enumBlocks.add(enumBlock);
                }
            }
        }

        StringBuilder fileContent = new StringBuilder();
        fileContent.append("package com.citi.fx.qa.qap;\n\n");
        fileContent.append("/**\n")
                   .append(" * Auto-generated class listing all QAPORTAL DB tables and columns as enums.\n")
                   .append(" */\n");
        fileContent.append("public class QAPTables {\n\n");
        enumBlocks.forEach(fileContent::append);
        fileContent.append("}\n");

        // Ensure the parent directory exists
        Files.createDirectories(Paths.get(outputFile).getParent());
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.write(fileContent.toString());
        }

        System.out.println("✅ QAPTables.java successfully generated.");
    }

    /**
     * Builds a nested enum block for a single table.
     *
     * @param tableName    the name of the table
     * @param columnNames  the list of column names
     * @return a formatted Java enum string
     */
    private static String buildEnumBlock(String tableName, List<String> columnNames) {
        String enumName = tableName.toUpperCase();
        StringBuilder sb = new StringBuilder();
        sb.append("    public enum ").append(enumName).append(" {\n");
        sb.append("        ").append(String.join(", ", columnNames)).append(";\n");
        sb.append("    }\n\n");
        return sb.toString();
    }
}
```
