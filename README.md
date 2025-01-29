```java
DECLARE
    CURSOR launch_cursor IS
        SELECT ID
        FROM QAPORTAL.TEST_LAUNCH
        WHERE REGRESSION = 0; -- Only non-regression test launches

BEGIN
    FOR record IN launch_cursor LOOP
        -- Delete related STEP_RUN entries
        DELETE FROM QAPORTAL.STEP_RUN
        WHERE TEST_RUN_ID IN (
            SELECT ID
            FROM QAPORTAL.TEST_RUN
            WHERE TEST_LAUNCH_ID = record.ID
        );

        -- Delete related TEST_RUN entries
        DELETE FROM QAPORTAL.TEST_RUN
        WHERE TEST_LAUNCH_ID = record.ID;

        -- Delete related TEST_PARAM entries
        DELETE FROM QAPORTAL.TEST_PARAM
        WHERE TEST_LAUNCH_ID = record.ID;

        -- Delete related TEST_TAG entries
        DELETE FROM QAPORTAL.TEST_TAG
        WHERE TEST_LAUNCH_ID = record.ID;

        -- Delete the TEST_LAUNCH entry itself
        DELETE FROM QAPORTAL.TEST_LAUNCH
        WHERE ID = record.ID;
    END LOOP;

    -- Cleanup FIX entries not associated with any TEST_RUN
    DELETE FROM QAPORTAL.FIX
    WHERE ID NOT IN (
        SELECT DISTINCT FIX_ID
        FROM QAPORTAL.TEST_RUN
        WHERE FIX_ID IS NOT NULL
    );

    -- Cleanup LOG entries not associated with any TEST_RUN
    DELETE FROM QAPORTAL.LOG
    WHERE ID NOT IN (
        SELECT DISTINCT LOG_ID
        FROM QAPORTAL.TEST_RUN
        WHERE LOG_ID IS NOT NULL
    );
END;
t date of the report period.
     * @param reportEnd   End date of the report period.
     * @param topN        Number of top common failures to include.
     * @return FailureReport object containing aggregated data.
     */
    @Cacheable(value = "failureReports", key = "#applicationName + '-' + #environmentName + '-' + #reportStart + '-' + #reportEnd + '-' + #topN")
    public FailureReport generateFailureReport(List<RegressionTestFailure> failures, LocalDate reportStart, LocalDate reportEnd, int topN) {
        // Filter failures within the specified time frame
        List<RegressionTestFailure> filteredFailures = failures.stream()
                .filter(failure -> {
                    LocalDate failureDate = failure.getExceptionCreated().toLocalDate();
                    return (failureDate.isEqual(reportStart) || failureDate.isAfter(reportStart)) &&
                           (failureDate.isEqual(reportEnd) || failureDate.isBefore(reportEnd));
                })
                .collect(Collectors.toList());

        // Group failures by exception message
        Map<String, List<RegressionTestFailure>> groupedFailures = filteredFailures.stream()
                .collect(Collectors.groupingBy(RegressionTestFailure::getExceptionMessage));

        // Create list of CommonFailure objects
        List<CommonFailure> commonFailures = groupedFailures.entrySet().stream()
                .map(entry -> {
                    String exceptionMessage = entry.getKey();
                    List<RegressionTestFailure> exceptionFailures = entry.getValue();

                    long count = exceptionFailures.size();
                    LocalDate firstOccurred = exceptionFailures.stream()
                            .map(failure -> failure.getExceptionCreated().toLocalDate())
                            .min(LocalDate::compareTo)
                            .orElse(null);
                    LocalDate lastOccurred = exceptionFailures.stream()
                            .map(failure -> failure.getExceptionCreated().toLocalDate())
                            .max(LocalDate::compareTo)
                            .orElse(null);
                    List<LocalDate> occurrenceDates = exceptionFailures.stream()
                            .map(failure -> failure.getExceptionCreated().toLocalDate())
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

                    // Further group by test details
                    Map<String, List<RegressionTestFailure>> groupedByTest = exceptionFailures.stream()
                            .collect(Collectors.groupingBy(failure -> 
                                failure.getTestClassName() + "|" +
                                failure.getTestMethodName() + "|" +
                                failure.getTestDisplayName()
                            ));

                    // Create list of AssociatedTest objects
                    List<AssociatedTest> associatedTests = groupedByTest.entrySet().stream()
                            .map(testEntry -> {
                                String[] testDetails = testEntry.getKey().split("\\|");
                                String testClassName = testDetails[0];
                                String testMethodName = testDetails[1];
                                String testDisplayName = testDetails[2];
                                List<RegressionTestFailure> testFailures = testEntry.getValue();

                                long testFailureCount = testFailures.size();
                                LocalDate testFirstOccurred = testFailures.stream()
                                        .map(failure -> failure.getExceptionCreated().toLocalDate())
                                        .min(LocalDate::compareTo)
                                        .orElse(null);
                                LocalDate testLastOccurred = testFailures.stream()
                                        .map(failure -> failure.getExceptionCreated().toLocalDate())
                                        .max(LocalDate::compareTo)
                                        .orElse(null);
                                List<LocalDate> testOccurrenceDates = testFailures.stream()
                                        .map(failure -> failure.getExceptionCreated().toLocalDate())
                                        .distinct()
                                        .sorted()
                                        .collect(Collectors.toList());

                                return new AssociatedTest(
                                        testClassName,
                                        testMethodName,
                                        testDisplayName,
                                        testFailureCount,
                                        testFirstOccurred,
                                        testLastOccurred,
                                        testOccurrenceDates
                                );
                            })
                            .collect(Collectors.toList());

                    return new CommonFailure(
                            exceptionMessage,
                            count,
                            firstOccurred,
                            lastOccurred,
                            occurrenceDates,
                            associatedTests
                    );
                })
                // Sort by failureCount descending to get the most common failures first
                .sorted(Comparator.comparingLong(CommonFailure::getFailureCount).reversed())
                .limit(topN) // Limit to top N
                .collect(Collectors.toList());

        // Assemble the FailureReport
        FailureReport report = new FailureReport();
        report.setReportGeneratedAt(LocalDateTime.now());
        report.setTimeFrame(new TimeFrame(reportStart, reportEnd));
        report.setMostCommonFailures(commonFailures);

        return report;
    }
}


```
