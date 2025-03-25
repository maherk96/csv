```java
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;

@Service
public abstract class AbstractTestPortalService<T> {

    private final QAPLaunchQueue dataQueue;
    private final TaskExecutor taskExecutor;
    private volatile boolean shutdown = false;

    protected final ApplicationService appService;
    protected final EnvironmentService envService;
    protected final UserService userService;
    protected final TestRunService testRunService;
    protected final TestService testService;
    protected final LogService logService;
    protected final FixService fixService;
    protected final ExceptionService exceptionService;
    protected final TestLaunchService testLaunchService;
    protected final TestClassService testClassService;
    protected final ThreadPoolTaskExecutor executorService;
    protected final TestTagService testTagService;
    protected final TestParamService testParamService;

    @Autowired
    public AbstractTestPortalService(
        QAPLaunchQueue dataQueue,
        TaskExecutor taskExecutor,
        ApplicationService appService,
        EnvironmentService envService,
        UserService userService,
        TestRunService testRunService,
        TestService testService,
        LogService logService,
        FixService fixService,
        ExceptionService exceptionService,
        TestLaunchService testLaunchService,
        TestClassService testClassService,
        ThreadPoolTaskExecutor executorService,
        TestTagService testTagService,
        TestParamService testParamService
    ) {
        this.dataQueue = dataQueue;
        this.taskExecutor = taskExecutor;
        this.appService = appService;
        this.envService = envService;
        this.userService = userService;
        this.testRunService = testRunService;
        this.testService = testService;
        this.logService = logService;
        this.fixService = fixService;
        this.exceptionService = exceptionService;
        this.testLaunchService = testLaunchService;
        this.testClassService = testClassService;
        this.executorService = executorService;
        this.testTagService = testTagService;
        this.testParamService = testParamService;
        processData();
    }

    private void processData() {
        taskExecutor.execute(() -> {
            while (!shutdown) {
                try {
                    T launchData = (T) dataQueue.take();
                    if (launchData != null) {
                        processTestReportData(launchData);
                    }
                } catch (Exception e) {
                    if (!shutdown) {
                        logService.error("Error processing data from queue", e);
                    }
                }
            }
        });
    }

    protected abstract void processTestReportData(T launchData);

    @PreDestroy
    public void shutdown() {
        shutdown = true;
        if (taskExecutor instanceof ThreadPoolTaskExecutor) {
            ((ThreadPoolTaskExecutor) taskExecutor).shutdown();
        }
    }
}
```
