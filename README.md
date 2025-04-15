```sql
@Query("select t from Test t where t.displayName = :displayName and t.testClass.id = :id")
Test getTestScenarioForTestFeature(@Param("displayName") String displayName,
                                   @Param("id") Long id);

```
