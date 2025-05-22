```java
    @Override
    public void append(LogEvent event) {
        if (getLayout() != null) {
            String formatted = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
            logEvents.add(formatted);
        } else {
            String fallback = Instant.ofEpochMilli(event.getTimeMillis()) + " [" +
                              event.getThreadName() + "] " +
                              event.getLoggerName() + " " +
                              event.getLevel() + " - " +
                              event.getMessage().getFormattedMessage();
            logEvents.add(fallback);
        }

        // Optional: include stack trace if present
        if (event.getThrown() != null) {
            StringWriter sw = new StringWriter();
            event.getThrown().printStackTrace(new PrintWriter(sw));
            logEvents.add(sw.toString());
        }
    }
```
