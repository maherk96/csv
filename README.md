```java
public class SolaceConfig {
    public String host;
    public String username;
    public String password;
    public String vpnname;
}

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class SolaceConfigLoader {

    public static SolaceConfig loadConfig(String path) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(new File(path), SolaceConfigWrapper.class).solace;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Solace config from YAML", e);
        }
    }

    static class SolaceConfigWrapper {
        public SolaceConfig solace;
    }
}

public void send(String topicName, String messageContent) {
    SolaceConfig config = SolaceConfigLoader.loadConfig("solace-config.yaml");

    producer = new SolaceMessageProducer(
        config.host, config.username, config.password, config.vpnname
    );

    try {
        producer.send(topicName, new Message(messageContent));
    } catch (MessageProducerException e) {
        throw new RuntimeException("Failed to send message", e);
    }

    log.debug("Sent to logging to QAP reporting service: {} {}", 
              System.getProperty(SYSTEM_PROPERTY_LAUNCH_ID), topicName);
}

```
