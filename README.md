```java
package com.example.launch;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h2>LaunchBuilder – pure, framework‑agnostic helper</h2>
 *
 * <p>This utility builds a fully‑populated {@link TestLaunchDTO} from:</p>
 * <ul>
 *   <li>CI‑supplied launch metadata (<em>UUID, branch, timestamps, …</em>)</li>
 *   <li>a collection of executed test cases</li>
 *   <li>application/user/environment identifiers</li>
 *   <li>a runner‑specific callback (e.g. <code>b ‑&gt; b.testClass(id)</code>)</li>
 * </ul>
 *
 * <p>It performs <strong>no persistence</strong> and has <strong>no Spring annotations</strong>.  That
 * keeps the class:</p>
 * <ul>
 *   <li><em>Deterministic</em> – easy to unit‑test in isolation</li>
 *   <li><em>Framework‑agnostic</em> – reusable outside Spring (CLI tools, batch jobs, …)</li>
 *   <li><em>Proxy‑free</em> – the class is {@code final} because Spring never needs to subclass it</li>
 * </ul>
 *
 * <h3>Usage from a Spring service</h3>
 *
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class LaunchService {
 *     private final TestLaunchRepository repo;
 *
 *     public long createJUnitLaunch(QAPHeader header,
 *                                   List<QAPAbstractTestCase> cases,
 *                                   long app, long user, long env,
 *                                   long testClassId) {
 *
 *         TestLaunchDTO dto = LaunchBuilder.build(header, cases, app, user, env,
 *                                                 b -> b.testClass(testClassId));
 *         return repo.save(dto).getId();
 *     }
 * }
 * }</pre>
 */
public final class LaunchBuilder {

    private LaunchBuilder() {
        /* utility class – do not instantiate */
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Builds a {@link TestLaunchDTO} without persisting it.
     *
     * @param launch      launch header captured by the CI runner
     * @param cases       executed test cases belonging to the launch
     * @param app         application identifier
     * @param user        user identifier
     * @param env         environment identifier
     * @param runnerExtra callback that injects the runner‑specific attribute into the builder
     *                    (e.g. <code>b -&gt; b.testClass(id)</code> or <code>b -&gt; b.testFeature(id)</code>)
     * @return immutable DTO ready to be persisted by the caller
     */
    public static TestLaunchDTO build(QAPHeader launch,
                                      List<QAPAbstractTestCase> cases,
                                      long app,
                                      long user,
                                      long env,
                                      Consumer<TestLaunchDTO.Builder> runnerExtra) {

        StatusSummary s      = summariseStatuses(cases);
        var builder          = createCommonFields(launch, s, app, user, env);
        runnerExtra.accept(builder);
        return builder.build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Aggregates per‑status counters in a <em>single pass</em> over the list.
     */
    private static StatusSummary summariseStatuses(List<QAPAbstractTestCase> cases) {
        EnumMap<TestCaseStatus, Long> counts = cases.stream()
            .map(tc -> TestCaseStatus.fromString(tc.getStatus()))
            .collect(Collectors.groupingBy(
                    Function.identity(),
                    () -> new EnumMap<>(TestCaseStatus.class),
                    Collectors.counting()));

        return new StatusSummary(
            counts.getOrDefault(TestCaseStatus.PASSED,   0L).intValue(),
            counts.getOrDefault(TestCaseStatus.FAILED,   0L).intValue(),
            counts.getOrDefault(TestCaseStatus.ABORTED,  0L).intValue(),
            counts.getOrDefault(TestCaseStatus.DISABLED, 0L).intValue(),
            counts.getOrDefault(TestCaseStatus.IGNORED,  0L).intValue()
        );
    }

    /**
     * Populates every DTO field that is common to all runners.
     */
    private static TestLaunchDTO.Builder createCommonFields(QAPHeader launch,
                                                            StatusSummary s,
                                                            long app,
                                                            long user,
                                                            long env) {

        return TestLaunchDTO.builder()
            .total(s.total())
            .passed(s.passed)
            .failed(s.failed)
            .aborted(s.aborted)
            .disabled(s.disabled)
            .ignored(s.ignored)
            .launchId(launch.getLaunchId())
            .gitBranch(launch.getGitBranch())
            .regression(launch.isRegression())
            .app(app)
            .user(user)
            .env(env)
            .startTime(convertInstantToOffsetDateTime(launch.getLaunchStartTime()))
            .endTime(convertInstantToOffsetDateTime(launch.getLaunchEndTime()))
            .jdkVersion(launch.getJdkVersion())
            .osVersion(launch.getOsVersion())
            .testRunnerVersion(launch.getTestRunnerVersion());
    }

    /**
     * Tiny immutable container using Java 17 <code>record</code> syntax.
     */
    private record StatusSummary(int passed,
                                 int failed,
                                 int aborted,
                                 int disabled,
                                 int ignored) {
        int total() {
            return passed + failed + aborted + disabled + ignored;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  PLACEHOLDER UTILITY – real implementation lives elsewhere in the project
    // ─────────────────────────────────────────────────────────────────────────────

    private static OffsetDateTime convertInstantToOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}

```
