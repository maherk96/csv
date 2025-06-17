```java
  import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class OptionalFieldJoinValidationTest {

    record NullableFieldTest(String displayName, String methodName, String className, QAPHeaderBuilderModifier modifier) {}

    interface QAPHeaderBuilderModifier {
        void modify(QAPHeaderBuilder builder);
    }

    static Stream<NullableFieldTest> nullableFieldTests() {
        return Stream.of(
            new NullableFieldTest("Null Application Name", "nullAppName", "NullAppNameTest", builder -> builder.applicationName(null)),
            new NullableFieldTest("Null Test Environment", "nullTestEnv", "NullTestEnvTest", builder -> builder.testEnv(null)),
            new NullableFieldTest("Null OS Version", "nullOsVersion", "NullOsVersionTest", builder -> builder.osVersion(null)),
            new NullableFieldTest("Null Git Branch", "nullGitBranch", "NullGitBranchTest", builder -> builder.gitBranch(null)),
            new NullableFieldTest("Null Runner Info", "nullRunnerInfo", "NullRunnerInfoTest", builder -> {
                builder.testRunnerVersion(null);
                builder.jdkVersion(null);
            })
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nullableFieldTests")
    @DisplayName("Test Processing with Null Optional Fields")
    void testProcessesWithNullableFields(NullableFieldTest test) {
        var launchId = launchIdHelper.setQAPLaunchId();
        var builder = qapDataHelper.qapHeaderBuilder()
            .launchId(launchId)
            .applicationName("DefaultApp")
            .testEnv("qa")
            .user("testUser")
            .gitBranch("main")
            .isRegression(true)
            .timestamp(Instant.now().plusSeconds(30).toEpochMilli())
            .osVersion("Ubuntu")
            .testRunnerVersion("1.0.0")
            .jdkVersion("17");

        test.modifier.modify(builder);
        var header = builder.build();

        var launch = new JunitLaunchBuilder()
            .startLaunch()
            .withHeader(header)
            .withTestClass(test.className, test.displayName, Set.of("Optional"))
            .addTest(test.methodName, test.displayName)
            .withStatus(PASSED.name())
            .done()
            .build();

        publishTestData(launch, launch);

        solaceClient.getWaitFactory().waitForTestCompletion(
            launch.getHeader().getLaunchId(),
            launch.getHeader().getUser()
        );

        databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
            .withParams(launchId)
            .expectSingleRow(DatabaseClient.ResultRow.expecting()
                .expect("methodName", test.methodName)
                .expect("displayName", test.displayName)
                .expect("testClassName", test.className)
                .expect("status", PASSED.name())
                .expect("userName", "testUser")
                .expect("launchId", launchId)
                .expectNotNull("startTime")
                .expectNotNull("endTime")
            );
    }

    @Test
    @DisplayName("Regression Flag Defaults to False When Null")
    void testProcessesWithNullRegressionDefaultsToFalse() {
        var launchId = launchIdHelper.setQAPLaunchId();
        var header = qapDataHelper.qapHeaderBuilder()
            .launchId(launchId)
            .applicationName("RegressionTestApp")
            .testEnv("test")
            .user("regUser")
            .gitBranch("feature-branch")
            .timestamp(Instant.now().plusSeconds(40).toEpochMilli())
            .osVersion("Linux")
            .testRunnerVersion("2.0.0")
            .jdkVersion("21")
            .build();

        var launch = new JunitLaunchBuilder()
            .startLaunch()
            .withHeader(header)
            .withTestClass("NullRegressionTest", "Regression Null Test", Set.of("Reg"))
            .addTest("regressionDefaultFalse", "Regression defaults to false")
            .withStatus(PASSED.name())
            .done()
            .build();

        publishTestData(launch, launch);

        solaceClient.getWaitFactory().waitForTestCompletion(
            launch.getHeader().getLaunchId(),
            launch.getHeader().getUser()
        );

        databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
            .withParams(launchId)
            .expectSingleRow(DatabaseClient.ResultRow.expecting()
                .expect("methodName", "regressionDefaultFalse")
                .expect("displayName", "Regression defaults to false")
                .expect("testClassName", "NullRegressionTest")
                .expect("status", PASSED.name())
                .expect("userName", "regUser")
                .expect("appName", "RegressionTestApp")
                .expect("envName", "test")
                .expect("launchId", launchId)
                .expectNotNull("startTime")
                .expectNotNull("endTime")
            );
    }

@Test
@DisplayName("Test Processes With Null Test Params")
void testProcessesWithNullTestParams() {
    var launchId = launchIdHelper.setQAPLaunchId();
    var header = qapDataHelper.buildQAPHeader(
        launchId,
        "QAPAuto",
        "DEV",
        "testUser",
        "main",
        false,
        Instant.now().plusSeconds(30).toEpochMilli(),
        "Linux",
        "1.1.0",
        "17"
    );

    var launch = new JunitLaunchBuilder()
        .startLaunch()
        .withHeader(header)
        .withTestClass("NullParamTest", "Test Param Nulls", Set.of("Param"))
        .addTest("paramTest", "Test with null test param")
        .withTestParams(null)
        .withStatus(PASSED.name())
        .done()
        .build();

    publishTestData(launch, launch);

    solaceClient.getWaitFactory().waitForTestCompletion(
        launch.getHeader().getLaunchId(),
        launch.getHeader().getUser()
    );

    databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
        .withParams(launchId)
        .expectSingleRow(DatabaseClient.ResultRow.expecting()
            .expect("methodName", "paramTest")
            .expect("displayName", "Test with null test param")
            .expect("testClassName", "NullParamTest")
            .expect("status", PASSED.name())
            .expect("userName", "testUser")
            .expect("appName", "QAPAuto")
            .expect("envName", "DEV")
            .expect("launchId", launchId)
            .expectNull("testParams")
            .expectNotNull("startTime")
            .expectNotNull("endTime")
        );

@Test
@DisplayName("Test Processes With Null Log")
void testProcessesWithNullLog() {
    var launchId = launchIdHelper.setQAPLaunchId();
    var header = qapDataHelper.buildQAPHeader(
        launchId,
        "QAPAuto",
        "qa",
        "user1",
        "develop",
        true,
        Instant.now().plusSeconds(20).toEpochMilli(),
        "Ubuntu",
        "2.0",
        "17"
    );

    var launch = new JunitLaunchBuilder()
        .startLaunch()
        .withHeader(header)
        .withTestClass("NullLogTest", "Log null test", Set.of("Run"))
        .addTest("nullLogTest", "TestRun with log null")
        .withStatus("FAILED")
        .done()
        .build();

    publishTestData(launch, launch);

    solaceClient.getWaitFactory().waitForTestCompletion(
        launch.getHeader().getLaunchId(),
        launch.getHeader().getUser()
    );

    databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
        .withParams(launchId)
        .expectSingleRow(DatabaseClient.ResultRow.expecting()
            .expect("methodName", "nullLogTest")
            .expect("displayName", "TestRun with log null")
            .expect("testClassName", "NullLogTest")
            .expect("status", "FAILED")
            .expect("userName", "user1")
            .expect("appName", "QAPAuto")
            .expect("envName", "qa")
            .expect("launchId", launchId)
            .expectNull("log")
            .expectNotNull("startTime")
            .expectNotNull("endTime")
        );


}

@Test
@DisplayName("Test Processes With Null Fix")
void testProcessesWithNullFix() {
    var launchId = launchIdHelper.setQAPLaunchId();
    var header = qapDataHelper.buildQAPHeader(
        launchId,
        "FixApp",
        "qa",
        "userFix",
        "fix-branch",
        true,
        Instant.now().plusSeconds(15).toEpochMilli(),
        "Linux",
        "1.2.0",
        "19"
    );

    var launch = new JunitLaunchBuilder()
        .startLaunch()
        .withHeader(header)
        .withTestClass("NullFixTest", "Fix null test", Set.of("Fix"))
        .addTest("nullFixTest", "TestRun with fix null")
        .withStatus("FAILED")
        .done()
        .build();

    publishTestData(launch, launch);

    solaceClient.getWaitFactory().waitForTestCompletion(
        launch.getHeader().getLaunchId(),
        launch.getHeader().getUser()
    );

    databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
        .withParams(launchId)
        .expectSingleRow(DatabaseClient.ResultRow.expecting()
            .expect("methodName", "nullFixTest")
            .expect("displayName", "TestRun with fix null")
            .expect("testClassName", "NullFixTest")
            .expect("status", "FAILED")
            .expect("userName", "userFix")
            .expect("appName", "FixApp")
            .expect("envName", "qa")
            .expect("launchId", launchId)
            .expectNull("fix")
            .expectNotNull("startTime")
            .expectNotNull("endTime")
        );
}

@Test
@DisplayName("Test Processes With Null Exception")
void testProcessesWithNullException() {
    var launchId = launchIdHelper.setQAPLaunchId();
    var header = qapDataHelper.buildQAPHeader(
        launchId,
        "ExceptionApp",
        "qa",
        "userEx",
        "exception-branch",
        false,
        Instant.now().plusSeconds(25).toEpochMilli(),
        "Linux",
        "1.2.3",
        "20"
    );

    var launch = new JunitLaunchBuilder()
        .startLaunch()
        .withHeader(header)
        .withTestClass("NullExceptionTest", "Exception null test", Set.of("Ex"))
        .addTest("nullExceptionTest", "TestRun with exception null")
        .withStatus("ABORTED")
        .done()
        .build();

    publishTestData(launch, launch);

    solaceClient.getWaitFactory().waitForTestCompletion(
        launch.getHeader().getLaunchId(),
        launch.getHeader().getUser()
    );

    databaseClient.runQuery(qapQueries.get("TestLaunchDataQuery"))
        .withParams(launchId)
        .expectSingleRow(DatabaseClient.ResultRow.expecting()
            .expect("methodName", "nullExceptionTest")
            .expect("displayName", "TestRun with exception null")
            .expect("testClassName", "NullExceptionTest")
            .expect("status", "ABORTED")
            .expect("userName", "userEx")
            .expect("appName", "ExceptionApp")
            .expect("envName", "qa")
            .expect("launchId", launchId)
            .expectNull("exception")
            .expectNotNull("startTime")
            .expectNotNull("endTime")
        );
}



}

```
