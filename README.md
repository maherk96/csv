```xml
<Configuration status="WARN">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d [%t] %-5p %c{1} %m%n"/>
    </Console>
    <RollingFile name="LogFile"
                 fileName="../logs/app_current.log"
                 filePattern="../logs/app-%d{yyyy-MM-dd-HH-mm-ss}-%i.log">
      <PatternLayout pattern="%d [%t] %-5p %c{1} %m%n"/>
      <Policies>
        <OnStartupTriggeringPolicy/>
        <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
        <SizeBasedTriggeringPolicy size="10MB"/>
      </Policies>
    </RollingFile>
  </Appenders>

  <Loggers>
    <!-- Hibernate SQL -->
    <Logger name="org.hibernate.SQL" level="debug" additivity="false">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="LogFile"/>
    </Logger>

    <!-- JdbcTemplate + Param Bindings -->
    <Logger name="org.springframework.jdbc.core.JdbcTemplate" level="debug" additivity="false">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="LogFile"/>
    </Logger>

    <Logger name="org.springframework.jdbc.core.StatementCreatorUtils" level="trace" additivity="false">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="LogFile"/>
    </Logger>

    <!-- Root Logger -->
    <Root level="info">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="LogFile"/>
    </Root>
  </Loggers>
</Configuration>
```
