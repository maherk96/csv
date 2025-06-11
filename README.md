```java
import java.time.Instant;

public class QAPHeaderBuilder {
    private final long launchStartTime = Instant.now().toEpochMilli(); // defaulted to "now"
    private final String launchId;

    private String applicationName;
    private String testEnvironment;
    private String user;
    private String gitBranch;
    private boolean isRegression;
    private long launchEndTime;
    private String osVersion;
    private String testRunnerVersion;
    private String jdkVersion;

    public QAPHeaderBuilder(String launchId) {
        this.launchId = launchId;
    }

    public QAPHeaderBuilder withApplicationName(String name) {
        this.applicationName = name;
        return this;
    }

    public QAPHeaderBuilder withTestEnvironment(String env) {
        this.testEnvironment = env;
        return this;
    }

    public QAPHeaderBuilder withUser(String user) {
        this.user = user;
        return this;
    }

    public QAPHeaderBuilder withGitBranch(String branch) {
        this.gitBranch = branch;
        return this;
    }

    public QAPHeaderBuilder withIsRegression(boolean isRegression) {
        this.isRegression = isRegression;
        return this;
    }

    public QAPHeaderBuilder withLaunchEndTime(long endTime) {
        this.launchEndTime = endTime;
        return this;
    }

    public QAPHeaderBuilder withOsVersion(String version) {
        this.osVersion = version;
        return this;
    }

    public QAPHeaderBuilder withTestRunnerVersion(String version) {
        this.testRunnerVersion = version;
        return this;
    }

    public QAPHeaderBuilder withJdkVersion(String version) {
        this.jdkVersion = version;
        return this;
    }

    public QAPHeader build() {
        QAPHeader header = new QAPHeader(launchStartTime, launchId);
        header.setApplicationName(applicationName);
        header.setTestEnvironment(testEnvironment);
        header.setUser(user);
        header.setGitBranch(gitBranch);
        header.setIsRegression(isRegression);
        header.setLaunchEndTime(launchEndTime);
        header.setOsVersion(osVersion);
        header.setTestRunnerVersion(testRunnerVersion);
        header.setJdkVersion(jdkVersion);
        return header;
    }
}
```
