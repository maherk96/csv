```sql
SELECT a.table_name AS child_table,
       a.column_name AS child_column,
       a.constraint_name,
       c.owner,
       c.r_owner,
       c_pk.table_name AS parent_table
FROM all_cons_columns a
JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name
JOIN all_constraints c_pk ON c.r_constraint_name = c_pk.constraint_name
WHERE c.constraint_type = 'R'
  AND c.constraint_name = 'FKAINY9YA75J7S0EXNK7YVOQS4H';
```
