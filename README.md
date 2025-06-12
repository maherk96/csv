```java
package com.example.codegen;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataSourceFactory {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe"; // update as needed
    private static final String USER = "QAPORTAL";
    private static final String PASSWORD = "your_password";

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC Driver not found", e);
        }
    }

    public static DataSource create() {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(URL, USER, PASSWORD);
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return DriverManager.getConnection(URL, username, password);
            }

            // The remaining methods can throw UnsupportedOperationException for this use case
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
            @Override public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }
            @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
            @Override public int getLoginTimeout() { throw new UnsupportedOperationException(); }
            @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };
    }
}
```
