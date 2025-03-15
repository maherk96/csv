<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Properties>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n</Property>
        <Property name="LOG_DIR">logs</Property>
    </Properties>

    <Appenders>
        <!-- Console Appender -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout>
                <Pattern>${LOG_PATTERN}</Pattern>
            </PatternLayout>
        </Console>

        <!-- File Appender for fix-gateway -->
        <RollingFile name="FixGatewayLog" fileName="${LOG_DIR}/fix-gateway.log"
                     filePattern="${LOG_DIR}/fix-gateway-%d{yyyy-MM-dd}.log.gz">
            <PatternLayout>
                <Pattern>${LOG_PATTERN}</Pattern>
            </PatternLayout>
            <Policies>
                <TimeBasedTriggeringPolicy />
            </Policies>
        </RollingFile>

        <!-- File Appender for exchange-manager -->
        <RollingFile name="ExchangeManagerLog" fileName="${LOG_DIR}/exchange-manager.log"
                     filePattern="${LOG_DIR}/exchange-manager-%d{yyyy-MM-dd}.log.gz">
            <PatternLayout>
                <Pattern>${LOG_PATTERN}</Pattern>
            </PatternLayout>
            <Policies>
                <TimeBasedTriggeringPolicy />
            </Policies>
        </RollingFile>

    </Appenders>

    <Loggers>
        <!-- Logger for fix-gateway -->
        <Logger name="service.fix-gateway" level="info" additivity="false">
            <AppenderRef ref="FixGatewayLog"/>
        </Logger>

        <!-- Logger for exchange-manager -->
        <Logger name="service.exchange-manager" level="info" additivity="false">
            <AppenderRef ref="ExchangeManagerLog"/>
        </Logger>

        <!-- Root Logger (for Console) -->
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
