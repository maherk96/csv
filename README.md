```java
-- CucumberTestLaunchDataQuery
SELECT 
    f.FEATURE_NAME         AS featureName,
    t.DISPLAY_NAME         AS scenarioName,
    ts.STEP_NAME           AS stepName,
    ts.STATUS              AS stepStatus,
    tp.TEST_PARAMS         AS testParams,
    tc.NAME                AS testClassName,
    tr.STATUS              AS scenarioStatus,
    u.NAME                 AS userName,
    a.NAME                 AS appName,
    e.NAME                 AS envName,
    f2.FIX                 AS fix,
    l.log                  AS log,
    e2."EXCEPTION"         AS exception,
    tl.LAUNCH_ID           AS launchId,
    tr.START_TIME          AS startTime,
    tr.END_TIME            AS endTime

FROM TEST_LAUNCH tl
INNER JOIN TEST_RUN tr          ON tl.ID = tr.TEST_LAUNCH_ID
INNER JOIN TEST t              ON tr.TEST_ID = t.ID
INNER JOIN TEST_CLASS tc       ON t.TEST_CLASS_ID = tc.ID
INNER JOIN TEST_FEATURE f      ON t.TEST_FEATURE_ID = f.ID
LEFT JOIN TEST_PARAM tp        ON t.ID = tp.TEST_ID AND tl.ID = tp.TEST_LAUNCH_ID
LEFT JOIN TEST_STEP ts         ON ts.TEST_ID = t.ID
INNER JOIN USERS u             ON tl.USER_ID = u.ID
INNER JOIN APPLICATION a       ON tl.APP_ID = a.ID
INNER JOIN ENVIRONMENTS e      ON tl.ENV_ID = e.ID
LEFT JOIN LOG l                ON tr.LOG_ID = l.ID
LEFT JOIN "EXCEPTION" e2       ON tr.EXCEPTION_ID = e2.ID
LEFT JOIN FIX f2               ON tr.FIX_ID = f2.ID

WHERE tl.LAUNCH_ID = ?
ORDER BY f.FEATURE_NAME, t.DISPLAY_NAME, ts.ID ASC;
```
