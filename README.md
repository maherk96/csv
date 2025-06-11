```java
package com.solace.demo.solace_events_demo;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.SolaceProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.publisher.DirectMessagePublisher;
import com.solace.messaging.publisher.OutboundMessage;
import com.solace.messaging.resources.Topic;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

@Component
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);

    private MessagingService messagingService;
    private DirectMessagePublisher publisher;

    @Autowired
    private SolaceConfigProperties config;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.setProperty(SolaceProperties.TransportLayerProperties.HOST, config.getHostUrl());
        props.setProperty(SolaceProperties.ServiceProperties.VPN_NAME, config.getVpnName());
        props.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME, config.getUserName());
        props.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD, config.getPassword());
        props.setProperty(SolaceProperties.TransportLayerProperties.RECONNECTION_ATTEMPTS, config.getReconnectionAttempts());
        props.setProperty(SolaceProperties.TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, config.getConnectionRetriesPerHost());

        messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(props)
                .build()
                .connect();

        publisher = messagingService.createDirectMessagePublisherBuilder()
                .build()
                .start();

        log.info("EventProducer initialized and connected to Solace.");
    }

    public void publish(String topicName, String payload) {
        Topic topic = Topic.of(topicName);

        OutboundMessage message = messagingService.messageBuilder()
                .withProperty("correlationId", UUID.randomUUID().toString())
                .build(payload.getBytes());

        publisher.publish(message, topic);
        log.info("Message published to topic: {}", topicName);
    }

    @PreDestroy
    public void shutdown() {
        if (publisher != null) {
            publisher.terminate(500);
        }
        if (messagingService != null) {
            messagingService.disconnect();
        }
        log.info("EventProducer shut down.");
    }
}
```
