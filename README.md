```java
public class QAPHeader {
    private final long launchStartTime;
    private final String launchId;
    private final String applicationName;
    private final String testEnvironment;
    private final String user;
    private final String gitBranch;
    private final boolean isRegression;
    private final long launchEndTime;
    private final String osVersion;
    private final String testRunnerVersion;
    private final String jdkVersion;

    private QAPHeader(QAPHeaderBuilder builder) {
        this.launchStartTime = builder.launchStartTime;
        this.launchId = builder.launchId;
        this.applicationName = builder.applicationName;
        this.testEnvironment = builder.testEnvironment;
        this.user = builder.user;
        this.gitBranch = builder.gitBranch;
        this.isRegression = builder.isRegression;
        this.launchEndTime = builder.launchEndTime;
        this.osVersion = builder.osVersion;
        this.testRunnerVersion = builder.testRunnerVersion;
        this.jdkVersion = builder.jdkVersion;
    }

    public static class QAPHeaderBuilder {
        private final long launchStartTime = System.currentTimeMillis();
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

        public QAPHeaderBuilder withApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public QAPHeaderBuilder withTestEnvironment(String testEnvironment) {
            this.testEnvironment = testEnvironment;
            return this;
        }

        public QAPHeaderBuilder withUser(String user) {
            this.user = user;
            return this;
        }

        public QAPHeaderBuilder withGitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
            return this;
        }

        public QAPHeaderBuilder withIsRegression(boolean isRegression) {
            this.isRegression = isRegression;
            return this;
        }

        public QAPHeaderBuilder withLaunchEndTime(long launchEndTime) {
            this.launchEndTime = launchEndTime;
            return this;
        }

        public QAPHeaderBuilder withOsVersion(String osVersion) {
            this.osVersion = osVersion;
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
            return new QAPHeader(this);
        }
    }

    // getters only (no setters)
}
```
