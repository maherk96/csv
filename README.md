```sql
@Autowired
private JdbcTemplate jdbcTemplate;

public void batchInsertStepData(List<TestStepDataDTO> dtos) {
    String sql = "INSERT INTO test_step_data " +
                 "(created, key_name, row_index, test_launch_id, test_step_id, value, id) " +
                 "VALUES (?, ?, ?, ?, ?, ?, test_step_data_seq.NEXTVAL)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            TestStepDataDTO dto = dtos.get(i);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); // or dto.getCreated()
            ps.setString(2, dto.getKeyName());
            ps.setInt(3, dto.getRowIndex());
            ps.setLong(4, dto.getTestLaunch());
            ps.setLong(5, dto.getTestStep());
            ps.setString(6, dto.getValue());
            // Don't set ID — Oracle sequence is handling it
        }

        @Override
        public int getBatchSize() {
            return dtos.size();
        }
    });
}
```
