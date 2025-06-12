```sql
SELECT table_name, constraint_name, r_constraint_name
FROM user_constraints
WHERE constraint_name = 'FKAINY9YA75J7S0EXNK7YVOQS4H';

SELECT table_name, column_name
FROM user_cons_columns
WHERE constraint_name = 'FKAINY9YA75J7S0EXNK7YVOQS4H';
```
