```sql
SELECT a.table_name, a.column_name, c_pk.table_name AS parent_table
FROM all_constraints c
JOIN all_cons_columns a ON c.constraint_name = a.constraint_name
JOIN all_constraints c_pk ON c.r_constraint_name = c_pk.constraint_name
WHERE c.constraint_type = 'R'
  AND c_pk.table_name = 'TEST_LAUNCH'
  AND c.owner = 'QAPORTAL';
```
