```yml
spring:
  profiles:
    active: dev

  config:
    import: classpath:heatmap-config.yml

  datasource:
    url: jdbc:oracle:thin:@//NAFXE1U.oraas.dyn.nsroot.net:8889/HANAFXE1U
    username: QAPORTAL
    password: M!FzGm45
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
      ddl-auto: none
      open-in-view: false
      properties:
        hibernate:
          jdbc:
            time_zone: UTC
          lob:
            non_contextual_creation: true
      new_generator_mappings: true

# ===================
# KDB CONFIGURATION
# ===================
kdb:
  environment: PREPROD
  username: fxfusion_cloudkdb_reader
  password: s6p2cu24
  script:
    locations:
      - classpath:sql/qap-kdb-queries.q

# ======================
# SOLACE CONFIGURATION
# ======================
solace:
  hostUrl: solace-fx1-us-1u-virt.nam.nsroot.net:55555,solace-fx1-us-2u-virt.nam.nsroot.net:55555
  queueName: QUE_179380_QA_AUTOMATION_TEST_REPORT_UAT
  vpnName: 179380_QA_REPORT_US_CTI_UAT
  userName: uat_179380_qareporter_pubsub
  password: 5x5yShhxtif
  reconnectionAttempts: 5
  connectionRetriesPerHost: 5
  topicNames:
    - QA/QAP/UAT/REPORT/JUNIT
    - QA/QAP/UAT/REPORT/CUCUMBER

# ====================
# FEATURE FLAGS
# ====================
features:
  enableApiKeyValidation: false

# ====================
# API DOCS
# ====================
springdoc:
  pathsToMatch: /api/**

# ====================
# SERVER SETTINGS
# ====================
server:
  servlet:
    context-path: "/qaportal/"
  port: "8090"
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

spring:
  datasource:
    url: jdbc:oracle:thin:@//NAFXE1D.oraas.dyn.nsroot.net:8889/NAFXE1D
    password: PLfB089oW

  jpa:
    hibernate:
      ddl-auto: update
```
