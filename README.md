```java
SELECT
    tr.ID AS TEST_RUN_ID,
    f.ID AS FIX_ID,
    f.CREATED AS FIX_CREATED,
    f.FIX AS FIX_BLOB
FROM
    TEST_RUN tr
LEFT JOIN FIX f ON tr.FIX_ID = f.ID
WHERE
    tr.ID = :testRunId;
```
