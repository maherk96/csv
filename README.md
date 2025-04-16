import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.EventQueue;

@Service
public class CucumberTestDataProcessor extends BaseTestDataProcessor<QAPCucumberLaunch> {

    private final EventQueue<QAPCucumberLaunch> dataQueue;
    private final TaskExecutor taskExecutor;

    private final ApplicationService appService;
    private final EnvironmentService envService;
    private final UserService userService;
    private final TestRunService testRunService;
    private final TestService testService;
    private final LogService logService;
    private final FixService fixService;
    private final ExceptionService exceptionService;
    private final TestLaunchService testLaunchService;
    private final TestClassService testClassService;
    private final TestFeatureService testFeatureService;
    private final TestTagService testTagService;
    private final TestParamService testParamService;

    @Autowired
    public CucumberTestDataProcessor(
        EventQueue<QAPCucumberLaunch> dataQueue,
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
        TestFeatureService testFeatureService,
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
        this.testFeatureService = testFeatureService;
        this.testTagService = testTagService;
        this.testParamService = testParamService;

        processData(); // Start processing immediately
    }

    @Override
    public void processData() {
        doProcess(dataQueue, taskExecutor, this::processTestReportData);
    }

    @Override
    public void processTestReportData(QAPCucumberLaunch testData) {
        validateTestData(testData);
        logService.logInfo("Processing test report for " + testData);
        // Continue with your real logic...
    }

    @Override
    public void validateTestData(QAPCucumberLaunch testData) {
        // Custom validation logic
    }

    @PreDestroy
    public void shutdown() {
        super.shutdown(taskExecutor);
    }
}
