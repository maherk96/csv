```sql
SELECT
  ts.STEP_NAME AS stepName,
  tsr.STATUS AS stepStatus,
  t.DISPLAY_NAME AS scenarioName,
  f.FEATURE_NAME AS featureName,
  f.FEATURE_DESCRIPTION AS featureDescription,
  t.STATUS AS scenarioStatus,
  u.NAME AS userName,
  a.NAME AS appName,
  e.NAME AS envName,
  tr.FIX_ID AS fix,
  tr.LOG_ID AS log,
  e2."EXCEPTION" AS stepException,
  e3."EXCEPTION" AS scenarioException,
  tr.TEST_LAUNCH_ID AS launchId,
  tr.START_TIME AS startTime,
  tr.END_TIME AS endTime,
  tsd.ROW_INDEX AS rowIndex,
  tsd.KEY_NAME AS dataKey,
  tsd."VALUE" AS dataValue
FROM TEST_LAUNCH tl
INNER JOIN TEST_RUN tr ON tl.ID = tr.TEST_LAUNCH_ID
INNER JOIN TEST t ON tr.TEST_ID = t.ID
INNER JOIN TEST_FEATURE f ON t.TEST_FEATURE_ID = f.ID
INNER JOIN USERS u ON tl.USER_ID = u.ID
INNER JOIN APPLICATION a ON tl.APP_ID = a.ID
INNER JOIN ENVIRONMENTS e ON tl.ENV_ID = e.ID
LEFT JOIN LOG l ON tr.LOG_ID = l.ID
LEFT JOIN EXCEPTION e3 ON tr.EXCEPTION_ID = e3.ID
LEFT JOIN FIX f ON tr.FIX_ID = f.ID
INNER JOIN TEST_STEP_RUN tsr ON tsr.TEST_RUN_ID = tr.ID
LEFT JOIN TEST_STEP ts ON ts.ID = tsr.TEST_STEP_ID
LEFT JOIN EXCEPTION e2 ON ts.EXCEPTION_ID = e2.ID
LEFT JOIN TEST_STEP_DATA tsd ON tsd.TEST_STEP_ID = ts.ID
                              AND tsd.TEST_LAUNCH_ID = tl.ID
WHERE tl.LAUNCH_ID = 'TestLaunch_ffe850d9-30d2-48d4-af9c'
ORDER BY tr.END_TIME DESC, tsr.ID, tsd.ROW_INDEX, tsd.ID;
```
