```java
package com.example.codegen;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

public class QAPEnumGenerator {

    public static void main(String[] args) throws Exception {
        DataSource dataSource = MyDataSourceFactory.create();
        String outputFile = "src/main/java/com/example/qa/enums/QAPTables.java";
        String schema = "QAPORTAL";

        try (Connection conn = dataSource.getConnection();
             PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, schema, "%", new String[]{"TABLE"});

            out.println("package com.example.qa.enums;\n");
            out.println("public class QAPTables {");

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                out.println("    public enum " + tableName + " {");

                ResultSet columns = metaData.getColumns(null, schema, tableName, "%");
                boolean first = true;
                while (columns.next()) {
                    if (!first) out.print(", ");
                    String columnName = columns.getString("COLUMN_NAME");
                    out.print(columnName);
                    first = false;
                }
                out.println("; }\n");
            }

            out.println("}");
        }

        System.out.println("QAPTables.java successfully generated.");
    }
}

plugins {
    id 'java'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.oracle.database.jdbc:ojdbc8:21.9.0.0'
    implementation 'com.zaxxer:HikariCP:5.1.0'
}

// Source set for generator code
sourceSets {
    gen {
        java {
            srcDir 'src/gen/java'
        }
    }
}

task generateQAPEnums(type: JavaExec) {
    group = "build setup"
    description = "Generates QAPTables enum from Oracle DB schema"

    classpath = sourceSets.gen.runtimeClasspath
    mainClass = 'com.example.codegen.QAPEnumGenerator'

    doFirst {
        println "Generating QAPTables.java from Oracle DB..."
    }
}

compileJava.dependsOn generateQAPEnums
```
