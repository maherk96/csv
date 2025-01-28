```java
package com.example.qaportal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedTest {
    private String testClassName;
    private String testMethodName;
    private String testDisplayName;
    private long failureCount;
    private LocalDate firstOccurred;
    private LocalDate lastOccurred;
    private List<LocalDate> occurrenceDates;
}

package com.example.qaportal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonFailure {
    private String exceptionMessage;
    private long failureCount;
    private LocalDate firstOccurred;
    private LocalDate lastOccurred;
    private List<LocalDate> occurrenceDates;
    private List<AssociatedTest> associatedTests; // New Field
}

package com.example.qaportal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailureReport {
    private LocalDateTime reportGeneratedAt;
    private TimeFrame timeFrame;
    private List<CommonFailure> mostCommonFailures;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TimeFrame {
    private LocalDate startDate;
    private LocalDate endDate;
}

package com.example.qaportal.service;

import com.example.qaportal.model.AssociatedTest;
import com.example.qaportal.model.CommonFailure;
import com.example.qaportal.model.FailureReport;
import com.example.qaportal.model.RegressionTestFailure;
import com.example.qaportal.model.TimeFrame;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FailureReportService {

    /**
     * Generates a failure report based on the provided test failures.
     *
     * @param failures    List of RegressionTestFailure records.
     * @param reportStart Start date of the report period.
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
