```java
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

public class QAPTestBuilder {
    private final JunitLaunchBuilder parent;
    private final QAPTest test;

    public QAPTestBuilder(JunitLaunchBuilder parent, String methodName, String displayName) {
        this.parent = parent;
        this.test = new QAPTest(methodName, displayName);
        this.test.setStartTime(Instant.now().toEpochMilli());
        this.test.setEndTime(Instant.now().toEpochMilli());
    }

    public QAPTestBuilder withStatus(String status) {
        test.setStatus(status);
        return this;
    }

    public QAPTestBuilder withParams(String params) {
        test.setTestParams(params.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public QAPTestBuilder withTags(Set<String> tags) {
        test.setTag(tags);
        return this;
    }

    public QAPTestBuilder withLogs(String logs) {
        test.setLogs(logs);
        return this;
    }

    public QAPTestBuilder withFix(String fix) {
        test.setFix(fix);
        return this;
    }

    public JunitLaunchBuilder done() {
        return parent.addBuiltTest(test);
    }
}
```
