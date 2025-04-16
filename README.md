```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d [%t] %-5p %c{1} - %m%n"/>
        </Console>

        <RollingFile name="LogFile"
                     fileName="../logs/app_current.log"
                     filePattern="../logs/app-%d{yyyy-MM-dd-HH-mm-ss}-%i.log">
            <PatternLayout pattern="%d [%t] %-5p %c{1} - %m%n"/>
            <Policies>
                <OnStartupTriggeringPolicy/>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="10MB"/>
            </Policies>
        </RollingFile>
    </Appenders>

    <Loggers>
        <!-- SQL queries -->
        <Logger name="org.hibernate.SQL" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="LogFile"/>
        </Logger>

        <!-- SQL query parameter values -->
        <Logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="trace" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="LogFile"/>
        </Logger>

        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="LogFile"/>
        </Root>
    </Loggers>
</Configuration>
```
