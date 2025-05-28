```java
@Plugin(
    name = "QAPLog4jAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE,
    printObject = true
)
public class QAPLog4jAppender extends AbstractAppender {

    private final TestDataPublisher testDataPublisher = new TestDataPublisher(false);
    private final QAPPPropertiesLoader propertiesLoader = new QAPPPropertiesLoader();

    protected QAPLog4jAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static QAPLog4jAppender createAppender(
        @PluginAttribute("name") String name,
        @PluginElement("filter") Filter filter,
        @PluginElement("layout") Layout<? extends Serializable> layout) {
        return new QAPLog4jAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        if (isInternal(event.getLoggerName())) {
            return; // Skip logging to prevent recursion
        }

        if (getLayout() != null) {
            String formatted = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
            testDataPublisher.publish(
                RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), formatted);
            return;
        }

        // Build fallback log
        StringBuilder fallback = new StringBuilder()
            .append(Instant.ofEpochMilli(event.getTimeMillis()))
            .append(" [").append(Thread.currentThread().getName()).append("] ")
            .append(event.getLoggerName()).append(" ")
            .append(event.getLevel()).append(" ")
            .append(event.getMessage().getFormattedMessage());

        Throwable thrown = event.getThrown();
        if (thrown != null) {
            StringWriter sw = new StringWriter();
            thrown.printStackTrace(new PrintWriter(sw));
            testDataPublisher.publish(
                RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), fallback.toString());
            testDataPublisher.publish(
                RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), sw.toString());
        } else {
            testDataPublisher.publish(
                RunEnvironment.valueOf(propertiesLoader.getRunEnvironment()), fallback.toString());
        }
    }

    private boolean isInternal(String loggerName) {
        return loggerName != null && (
            loggerName.startsWith("com.citi.fx.qa.qap.data.publisher") ||
            loggerName.startsWith("QAPLog4jAppender") ||
            loggerName.contains("TestDataPublisher")
        );
    }
}

```
