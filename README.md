### **Step-by-Step Data Flow Into Database**
1. **TestFeature Table**
   - If the feature does not exist, a new record is inserted.
   - Example row:
   | ID  | FEATURE_NAME                  | FEATURE_DESCRIPTION      | APP_ID |
   |-----|--------------------------------|--------------------------|--------|
   | 1   | tradeServiceTests.feature      | FX Trade Service         | 10     |

2. **Test Table** (Scenarios)
   - Each scenario in the feature is inserted into the **Test** table.
   - Example row:
   | ID  | DISPLAY_NAME                          | TEST_FEATURE_ID |
   |-----|---------------------------------------|----------------|
   | 1   | Successfully place a single trade    | 1              |

3. **TestStep Table**
   - Each step within the scenario is inserted.
   - Example rows:
   | ID  | STEP_NAME                         | STATUS  | TEST_ID | EXCEPTION_ID |
   |-----|----------------------------------|---------|--------|--------------|
   | 1   | user logs in                     | PASSED  | 1      | NULL         |
   | 2   | user places trade                | FAILED  | 1      | 1            |

4. **Exception Table (For Failed Steps)**
   - If a step fails, the `errorMessage` is stored as an exception.
   - Example row:
   | ID  | MESSAGE                 |
   |-----|--------------------------|
   | 1   | Insufficient balance     |

5. **TestStepData Table (For Steps With Data Tables)**
   - If a step has a data table, each row is stored in **TestStepData**.
   - Example rows:
   | ID  | TEST_STEP_ID | DATA_KEY  | DATA_VALUE |
   |-----|-------------|-----------|------------|
   | 1   | 3           | quantity  | 1000       |
   | 2   | 3           | symbol    | USD/CAD    |
   | 3   | 3           | quantity  | 1200       |
   | 4   | 3           | symbol    | EUR/USD    |
