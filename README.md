**Technical Document: Cucumber Test Integration into Test Management System**

---

# **Introduction**
This document details the integration of Cucumber test results into the existing test management system. It explains the three new entity classes (`TestFeature`, `TestStep`, and `TestStepData`), their fields, relationships, and how data flows from a received Cucumber JSON payload into these entities. Additionally, an example dataset and a tabular representation of the stored data are provided.

---

# **1. New Entity Classes**

## **1.1 TestFeature (Feature Level)**
**Purpose:** Represents a Cucumber feature file.

### **Fields:**
- **`id`** (Primary Key) – Unique identifier for the feature.
- **`featureName`** – Name of the Cucumber feature file.
- **`featureDescription`** – Description of what the feature tests.
- **`app_id`** (Foreign Key) – Links the feature to a specific application under test.
- **`created`** – Timestamp when the feature was recorded.

### **Relationships:**
- One **`TestFeature`** can have multiple **`Test` (Scenarios)**.

---

## **1.2 TestStep (Step Level)**
**Purpose:** Represents an individual step within a Cucumber scenario.

### **Fields:**
- **`id`** (Primary Key) – Unique identifier for the test step.
- **`test_id`** (Foreign Key) – Links the step to its respective test scenario.
- **`stepName`** – The action performed in this step.
- **`status`** – Execution result (PASSED, FAILED, SKIPPED, etc.).
- **`exception_id`** (Foreign Key) – Links to the existing **`EXCEPTION`** table for failure reasons.
- **`created`** – Timestamp when the step was recorded.

### **Relationships:**
- One **`TestStep`** belongs to one **`Test`**.
- One **`TestStep`** can have multiple **`TestStepData`** (if the step includes data).
- If a step fails, it is linked to an **`EXCEPTION`** entry.

---

## **1.3 TestStepData (Step Data)**
**Purpose:** Stores key-value pairs when a Cucumber step includes a data table.

### **Fields:**
- **`id`** (Primary Key) – Unique identifier for the data entry.
- **`test_step_id`** (Foreign Key) – Links the data to the respective test step.
- **`dataKey`** – Represents the parameter name (e.g., `quantity`).
- **`dataValue`** – Represents the parameter value (e.g., `1000`).
- **`created`** – Timestamp when the data entry was recorded.

### **Relationships:**
- One **`TestStepData`** belongs to one **`TestStep`**.
- Each entry represents a single row from the Cucumber **data table**.

---

# **2. Data Flow for Processing Cucumber JSON**

### **Example Cucumber JSON Input**

#### **Case 1: Scenario Without Data Steps**
```json
{
    "feature": {
        "featureName": "tradeServiceTests.feature",
        "featureDescription": "FX Trade Service",
        "scenarios": [
            {
                "scenarioName": "Successfully place a single trade",
                "steps": [
                    {"stepName": "user logs in", "status": "PASSED"},
                    {"stepName": "user places trade", "status": "FAILED", "errorMessage": "Insufficient balance"}
                ]
            }
        ]
    }
}
```

#### **Case 2: Scenario With Data Steps**
```json
{
    "feature": {
        "featureName": "tradeServiceTests.feature",
        "featureDescription": "FX Trade Service",
        "scenarios": [
            {
                "scenarioName": "Place multiple trades",
                "steps": [
                    {
                        "stepName": "the user places the following trades",
                        "status": "PASSED",
                        "dataTable": [
                            {"quantity": "1000", "symbol": "USD/CAD"},
                            {"quantity": "1200", "symbol": "EUR/USD"}
                        ]
                    }
                ]
            }
        ]
    }
}
```

---

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

---

# **3. Conclusion**
The addition of `TestFeature`, `TestStep`, and `TestStepData` ensures that **Cucumber’s hierarchical test structure** (Feature → Scenario → Steps) is properly mapped into the database. This approach allows seamless integration of both simple and data-driven tests while maintaining compatibility with existing queries.

---

# **4. Next Steps**
- **Update database schema** with these new entities.
- **Modify service layer** to parse and store incoming Cucumber JSON results.
- **Implement test cases** to validate that the entity relationships function correctly.

