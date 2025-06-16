```xml
    <!-- Show the SQL queries -->
    <Logger name="org.hibernate.SQL" level="debug" additivity="false">
        <AppenderRef ref="Console"/>
    </Logger>

    <!-- Show the parameters bound to queries -->
    <Logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="trace" additivity="false">
        <AppenderRef ref="Console"/>
    </Logger>

```
