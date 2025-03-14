# Cucumber Test Data Flow

## Overview
This document describes how Cucumber test data is processed and stored in the database. It outlines the relationships between entities and provides example tables using the provided JSON input.

## Data Flow Process

### **1. TestFeature Table**
- **What gets stored?**
  - The feature name and description from the JSON.
  - Links to the corresponding application (`APP_ID`).
- **Example Entry:**
  
  | ID  | FEATURE_NAME                  | FEATURE_DESCRIPTION      | APP_ID |
  |-----|--------------------------------|--------------------------|--------|
  | 1   | tradeServiceTests.feature      | FX Trade Service         | 10     |

---
### **2. Test Table (Scenarios)**
- **What gets stored?**
  - Each scenario from the feature is stored in the **Test** table.
  - Linked to `TestFeature` via `TEST_FEATURE_ID`.
  
  **Example Entries:**
  
  | ID  | DISPLAY_NAME                          | TEST_FEATURE_ID |
  |-----|---------------------------------------|----------------|
  | 1   | Successfully place a single trade    | 1              |
  | 2   | Place multiple trades               | 1              |
  
---
### **3. TestStep Table**
- **What gets stored?**
  - Each step within a scenario is inserted.
  - Linked to the corresponding `Test` entry via `TEST_ID`.
  - If the step fails, an exception reference is stored.
  
  **Example Entries:**
  
  | ID  | STEP_NAME                         | STATUS  | TEST_ID | EXCEPTION_ID |
  |-----|----------------------------------|---------|--------|--------------|
  | 1   | The user is logged into the trade service  | PASSED  | 1      | NULL         |
  | 2   | The user places a trade for 100 shares of "EUR/USD" | PASSED  | 1      | NULL         |
  | 3   | The trade is successfully placed | PASSED  | 1      | NULL         |
  | 4   | The user places the following trades | PASSED  | 2      | NULL         |
  | 5   | All trades are successfully placed | PASSED  | 2      | NULL         |

---
### **4. Exception Table (For Failed Steps)**
- **What gets stored?**
  - If a step fails, the error message is stored as an exception.
  
  **Example Entry:**
  
  | ID  | MESSAGE                 |
  |-----|--------------------------|
  | 1   | Insufficient balance     |
  
---
### **5. TestStepData Table (For Steps With Data Tables)**
- **What gets stored?**
  - If a step has a data table, each key-value pair is stored.
  - Linked to the corresponding `TestStep` entry.
  
  **Example Entries:**
  
  | ID  | TEST_STEP_ID | DATA_KEY  | DATA_VALUE |
  |-----|-------------|-----------|------------|
  | 1   | 4           | quantity  | 1000       |
  | 2   | 4           | symbol    | USD/CAD    |
  | 3   | 4           | quantity  | 1200       |
  | 4   | 4           | symbol    | EUR/USD    |
  | 5   | 4           | quantity  | 1400       |
  | 6   | 4           | symbol    | AUD/USD    |
  
---
## **Summary of Flow**
1. The **TestFeature** table receives the feature name and description.
2. Each **scenario** from the JSON gets stored in the **Test** table, linked to the feature.
3. Each **step** from a scenario is stored in **TestStep**, linked to a test.
4. If a step fails, an entry is made in the **Exception** table and referenced in **TestStep**.
5. If a step has **data**, it is stored in **TestStepData**.

This ensures that Cucumber’s hierarchical structure (Feature → Scenario → Steps → Data) is efficiently stored in the database.

