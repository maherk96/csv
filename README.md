```java
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.citi.fx.qa.qap.db")
public class TestAppCfg {

    @Value("${sql.script.locations}")
    private String[] locations;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Bean
    public DataSource dataSource() {
        OracleDataSource dataSource = null;
        try {
            dataSource = new OracleDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dataSource.setURL("jdbc:oracle:thin:@//NAFXE1D.oraas.dyn.nsroot.net:8889/NAFXE1D");
        dataSource.setUser("QAPORTAL");
        dataSource.setPassword("PLF8o90u");
        return dataSource;
    }

    @Bean
    public Map<String, String> qapQueries() {
        return Arrays.stream(locations)
            .flatMap(location -> {
                try {
                    return Arrays.stream(resourcePatternResolver.getResources(location));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(resource -> {
                try {
                    return new AbstractMap.SimpleEntry<>(
                        resource.getFilename(),
                        StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8)
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(entry -> parseSqlQueries(entry.getValue()))
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
    }

    private Map<String, String> parseSqlQueries(String content) {
        return Arrays.stream(content.split("\\r?\\n"))
            .map(String::trim)
            .collect(
                LinkedHashMap::new,
                (queries, line) -> {
                    if (line.startsWith("--")) {
                        queries.put(line.substring(2).trim(), "");
                    } else if (!line.isEmpty()) {
                        String lastKey = queries.keySet().stream().reduce((f, s) -> s).orElse(null);
                        if (lastKey != null) {
                            queries.put(lastKey, queries.getOrDefault(lastKey, "") + line + " ");
                        }
                    }
                },
                LinkedHashMap::putAll
            );
    }
}
```
