package com.vectoros.fleet.mqtt.config;

import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
@ConditionalOnProperty(prefix = "vectoros.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MqttConfiguration.class);

    private MqttClientGateway gateway;

    @Bean
    public MqttConnectOptions mqttConnectOptions(MqttProperties properties) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(properties.isAutomaticReconnect());
        options.setCleanSession(properties.isCleanSession());
        options.setConnectionTimeout(properties.getConnectionTimeout());
        options.setKeepAliveInterval(properties.getKeepAliveInterval());

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
        }
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            options.setPassword(properties.getPassword().toCharArray());
        }

        return options;
    }

    @Bean
    public MqttClient mqttClient(MqttProperties properties) throws MqttException {
        String clientId = properties.getClientId() + "-" + System.currentTimeMillis();
        return new MqttClient(properties.getBrokerUrl(), clientId, new MemoryPersistence());
    }

    @Bean
    public MqttClientGateway mqttClientGateway(MqttClient mqttClient,
                                               MqttConnectOptions connectOptions,
                                               MqttProperties properties) {
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverUri) {
                log.info("MQTT connection complete: reconnect={} broker={}", reconnect, serverUri);
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT broker disconnected: reason={}",
                        cause == null ? "unknown" : cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // Handled by topic-specific IMqttMessageListener subscriptions.
            }

            @Override
            public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                // No-op
            }
        });

        this.gateway = new MqttClientGateway(mqttClient, connectOptions, properties.getQos());
        this.gateway.connect();
        return this.gateway;
    }

    @PreDestroy
    public void shutdown() {
        if (gateway != null) {
            gateway.disconnect();
        }
    }
}
