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
```
