```sql
ALTER TABLE "TEST_LAUNCH" ADD COLUMN "FEATURE_NAME" VARCHAR2(255 CHAR);
ALTER TABLE "TEST_LAUNCH" ADD COLUMN "FEATURE_DESCRIPTION" VARCHAR2(500 CHAR);

CREATE TABLE "TEST_STEP" (
    "ID" NUMBER(19,0) NOT NULL ENABLE,
    "TEST_ID" NUMBER(19,0),
    "STEP_NAME" VARCHAR2(255 CHAR),
    "STATUS" VARCHAR2(20 CHAR),
    "ERROR_MESSAGE" CLOB,
    PRIMARY KEY ("ID"),
    FOREIGN KEY ("TEST_ID") REFERENCES "TEST" ("ID") ENABLE
);

CREATE TABLE "TEST_STEP_DATA" (
    "ID" NUMBER(19,0) NOT NULL ENABLE,
    "TEST_STEP_ID" NUMBER(19,0),
    "KEY" VARCHAR2(255 CHAR),
    "VALUE" VARCHAR2(255 CHAR),
    PRIMARY KEY ("ID"),
    FOREIGN KEY ("TEST_STEP_ID") REFERENCES "TEST_STEP" ("ID") ENABLE
);

```

```java
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(length = 255, nullable = false)
    private String featureName;

    @Column(length = 500)
    private String featureDescription;

    @ManyToOne(fetch = FetchType.LAZY) // Many features belong to one application
    @JoinColumn(name = "app_id")
    private Application app;

    @OneToMany(mappedBy = "testFeature", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TestLaunch> testLaunches;

    @CreationTimestamp
    private Instant created;
}

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;  // Scenario it belongs to

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private TestFeature testFeature;  // Direct relation to the feature

    @Column(name = "step_name", length = 255, nullable = false)
    private String stepName;

    @Column(name = "status", length = 20, nullable = false)
    private String status;  // PASSED, FAILED, SKIPPED, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exception_id")
    private ExceptionEntity exception;  // Links to the EXCEPTION table

    @OneToMany(mappedBy = "testStep", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TestStepData> stepData;  // Holds input values (data tables)

    @CreationTimestamp
    private Instant created;
}

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestStepData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // Each data entry belongs to a step
    @JoinColumn(name = "test_step_id", nullable = false)
    private TestStep testStep;

    @Column(name = "key_name", length = 255, nullable = false)
    private String key;  // Example: "quantity", "symbol"

    @Column(name = "value", length = 255, nullable = false)
    private String value;  // Example: "1000", "USD/CAD"
}
```
