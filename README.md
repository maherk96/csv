```yaml
logging:
  file:
    name: logs/app_current.log
  logback:
    rollingpolicy:
      file-name-pattern: logs/app-%d{yyyy-MM-dd-HH-mm-ss}-%i.log
      max-file-size: 10MB
      max-history: 7
      clean-history-on-start: true
```
