```java
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class SolaceConnectionsConfig {

    private String host;
    private String topic;
    private String vpnname;
    private String username;
    private String password;

    // Getters
    public String getHost()     { return host; }
    public String getTopic()    { return topic; }
    public String getVpnname()  { return vpnname; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Static loader
    public static SolaceConnectionsConfig load() {
        Yaml yaml = new Yaml();
        try (InputStream input = SolaceConnectionsConfig.class
                .getClassLoader()
                .getResourceAsStream("config.yml")) {

            // loadAs requires a wrapper map since our YAML uses a top-level key
            var map = yaml.loadAs(input, java.util.Map.class);
            Object inner = map.get("solaceConnections");

            // convert inner map to SolaceConnectionsConfig
            return yaml.loadAs(yaml.dump(inner), SolaceConnectionsConfig.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load solaceConnections from YAML", e);
        }
    }
}
```
