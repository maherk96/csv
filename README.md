```sql
@Query("select (count(t) > 0) from Test t where t.displayName = :displayName and t.testClass.id = :id")
boolean scenarioNameExistForFeature(@Param("displayName") String displayName,
                                    @Param("id") Long id);

```
