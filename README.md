```yml
spring:
  profiles: test
  datasource:
    url: jdbc:oracle:thin:@//NAFXE1D.oraas.dyn.nsroot.net:8889/NAFXE1D
    username: QAPORTAL
    password: PLF8o90u
    driver-class-name: oracle.jdbc.OracleDriver
    type: oracle.ucp.jdbc.PoolDataSource

oracleucp:
  minPoolSize: 4
  maxPoolSize: 20
```
