```java

String launchId = "TestLaunch_75999d5b-bbd6-483d-b61b-...";

databaseClient
    .runQuery("SELECT * FROM TEST_LAUNCH WHERE LAUNCH_ID = ?")
    .withParams(launchId)
    .expectSingleRow(row -> {
        Assertions.assertEquals("main", row.getString("GIT_BRANCH"));
        Assertions.assertEquals("1.0.0", row.getString("TEST_RUNNER_VERSION"));
        Assertions.assertEquals(1, row.getInt("TOTAL"));
        Assertions.assertEquals(1, row.getInt("PASSED"));
        Assertions.assertEquals("17", row.getString("JDK_VERSION"));
        Assertions.assertEquals("Linux 5.4", row.getString("OS_VERSION"));
        Assertions.assertEquals("1", row.getString("REGRESSION"));
        Assertions.assertEquals(16002L, row.getLong("APP_ID"));
        Assertions.assertEquals(30152L, row.getLong("TEST_CLASS_ID"));
        Assertions.assertEquals(20152L, row.getLong("USER_ID"));

        // Optional: assert timestamps are not null
        Assertions.assertNotNull(row.get("START_TIME"));
        Assertions.assertNotNull(row.get("END_TIME"));
    });
```
