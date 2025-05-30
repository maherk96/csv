```java
@Plugin(
    name = "QAPLog4jAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE,
    printObject = true
)
public class QAPLog4jAppender extends AbstractAppender {

    private final LogsDataPublisher logsDataPublisher = new LogsDataPublisher(false);
    private final QAPropertiesLoader propertiesLoader = new QAPropertiesLoader();

    protected QAPLog4jAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static QAPLog4jAppender createAppender(
        @PluginAttribute("name") String name,
        @PluginElement("Filter") Filter filter,
        @PluginElement("Layout") Layout<? extends Serializable> layout
    ) {
        return new QAPLog4jAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        if (LogUtils.isInternalLog(event.getLoggerName())) {
            return;
        }

        try {
            if (getLayout() != null) {
                handleFormattedLog(event);
            } else {
                handleFallbackLog(event);
            }
        } catch (Exception e) {
            // Optional: Log to internal logger or fallback system if needed
            System.err.println("Error appending log event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleFormattedLog(LogEvent event) {
        String formatted = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        publish(formatted);
    }

    private void handleFallbackLog(LogEvent event) {
        StringBuilder fallback = new StringBuilder()
            .append(Instant.ofEpochMilli(event.getTimeMillis()))
            .append(" [")
            .append(Thread.currentThread().getName())
            .append("] ")
            .append(event.getLoggerName())
            .append(" - ")
            .append(event.getMessage().getFormattedMessage());

        Throwable thrown = event.getThrown();
        if (thrown != null) {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                thrown.printStackTrace(pw);
                fallback.append(System.lineSeparator()).append(sw.toString());
            } catch (IOException ioException) {
                fallback.append(" [Unable to print stack trace]");
            }
        }

        publish(fallback.toString());
    }

    private void publish(String message) {
        logsDataPublisher.publish(
            RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()),
            message,
            System.getProperty(ExtensionUtil.SYSTEM_PROPERTY_LAUNCH_ID)
        );
    }
}
```
