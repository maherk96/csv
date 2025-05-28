```java
@Override
public void append(LogEvent event) {
    if (layout != null) {
        String formatted = new String(layout.toByteArray(event), StandardCharsets.UTF_8);
        testDataPublisher.publish(RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), formatted);
        return;
    }

    // Build fallback log
    StringBuilder fallback = new StringBuilder()
        .append(Instant.ofEpochMilli(event.getTimeMillis())).append(" | ")
        .append(Thread.currentThread().getName()).append(" | ")
        .append(event.getLoggerName()).append(" | ")
        .append(event.getLevel()).append(" | ")
        .append(event.getMessage().getFormattedMessage());

    Throwable thrown = event.getThrown();
    if (thrown != null) {
        StringWriter sw = new StringWriter();
        thrown.printStackTrace(new PrintWriter(sw));
        testDataPublisher.publish(RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), fallback.toString());
        testDataPublisher.publish(RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), sw.toString());
    } else {
        testDataPublisher.publish(RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), fallback.toString());
    }
}

```
