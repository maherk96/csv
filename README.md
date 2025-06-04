```yaml
spring:
  profiles:
    active: dev

  config:
    import: classpath:heatmap-config.yml

  datasource:
    dbcp2:
      max-wait-millis: 30000
      validation-query: select 1 from dual
      validation-query-timeout: 30
    type: oracle.ucp.jdbc.PoolDataSource

  oracleucp:
    minPoolSize: 4
    maxPoolSize: 20

  jpa:
    hibernate:
      open-in-view: false
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        generate_statistics: false
        jdbc:
          batch_size: 50
          time_zone: UTC
        lob:
          non_contextual_creation: true
    new_generator_mappings: true
```
