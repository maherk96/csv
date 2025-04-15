```sql
SELECT column_name, data_length 
FROM all_tab_columns 
WHERE table_name = 'PERFORMANCE_METRICS' 
  AND column_name = 'MODEL_LATENCY';

```
