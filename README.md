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

        -- Delete related EXCEPTION entries
        DELETE FROM QAPORTAL.EXCEPTION
        WHERE ID IN (
            SELECT DISTINCT EXCEPTION_ID
            FROM QAPORTAL.TEST_RUN
            WHERE TEST_LAUNCH_ID = record.ID
              AND EXCEPTION_ID IS NOT NULL
        );

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

    -- Cleanup EXCEPTION entries not associated with any TEST_RUN
    DELETE FROM QAPORTAL.EXCEPTION
    WHERE ID NOT IN (
        SELECT DISTINCT EXCEPTION_ID
        FROM QAPORTAL.TEST_RUN
        WHERE EXCEPTION_ID IS NOT NULL
    );
END;
