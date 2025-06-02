```sql


✅ Step 1: Get the TEST_LAUNCH.ID for the given LAUNCH_ID

SELECT ID FROM TEST_LAUNCH 
WHERE LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6';

✅ Step 2: Get all TEST_RUN.IDs for that launch

SELECT ID FROM TEST_RUN 
WHERE TEST_LAUNCH_ID = (
    SELECT ID FROM TEST_LAUNCH 
    WHERE LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6'
);


✅ Step 3: Get all TEST_STEP_RUNs for those test runs

SELECT * FROM TEST_STEP_RUN 
WHERE TEST_RUN_ID IN (
    SELECT ID FROM TEST_RUN 
    WHERE TEST_LAUNCH_ID = (
        SELECT ID FROM TEST_LAUNCH 
        WHERE LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6'
    )
);


✅ Step 4: Get all TEST_STEPs used in those step runs

SELECT * FROM TEST_STEP 
WHERE ID IN (
    SELECT TEST_STEP_ID 
    FROM TEST_STEP_RUN 
    WHERE TEST_RUN_ID IN (
        SELECT ID FROM TEST_RUN 
        WHERE TEST_LAUNCH_ID = (
            SELECT ID FROM TEST_LAUNCH 
            WHERE LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6'
        )
    )
);

✅ Step 5: Get all TEST_STEP_DATA linked to those steps

SELECT * FROM TEST_STEP_DATA 
WHERE TEST_STEP_ID IN (
    SELECT ID FROM TEST_STEP 
    WHERE ID IN (
        SELECT TEST_STEP_ID 
        FROM TEST_STEP_RUN 
        WHERE TEST_RUN_ID IN (
            SELECT ID FROM TEST_RUN 
            WHERE TEST_LAUNCH_ID = (
                SELECT ID FROM TEST_LAUNCH 
                WHERE LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6'
            )
        )
    )
);


✅ Step 6: One-shot full diagnostic query

SELECT 
    tl.ID AS launch_id,
    tr.ID AS test_run_id,
    ts.ID AS test_step_id,
    tsr.ID AS test_step_run_id,
    tsd.ID AS step_data_id,
    ts.STEP_NAME,
    tsd.KEY_NAME,
    tsd.KEY_VALUE
FROM TEST_LAUNCH tl
JOIN TEST_RUN tr ON tr.TEST_LAUNCH_ID = tl.ID
LEFT JOIN TEST_STEP_RUN tsr ON tsr.TEST_RUN_ID = tr.ID
LEFT JOIN TEST_STEP ts ON ts.ID = tsr.TEST_STEP_ID
LEFT JOIN TEST_STEP_DATA tsd ON tsd.TEST_STEP_ID = ts.ID
WHERE tl.LAUNCH_ID = 'TestLaunch_e52153c7-a70c-4817-96a6';



Table	What You Should See
TEST_RUN	At least one row returned
TEST_STEP_RUN	Rows with TEST_RUN_ID matching step run
TEST_STEP	Rows with STEP_NAME
TEST_STEP_DATA	Rows with KEY_NAME, KEY_VALUE
Diagnostic query	All columns populated (step + status + data)

```
