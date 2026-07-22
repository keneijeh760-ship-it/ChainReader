package com.vectoros.fleet.mqtt.config;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin gateway over the Eclipse Paho MQTT client.
 * <p>
 * Isolates connection lifecycle and publish/subscribe operations from business code.
 * Temporary broker failures are logged and do not crash the application.
 */
public class MqttClientGateway {

    private static final Logger log = LoggerFactory.getLogger(MqttClientGateway.class);

    private final MqttClient mqttClient;
    private final MqttConnectOptions connectOptions;
    private final int qos;

    public MqttClientGateway(MqttClient mqttClient, MqttConnectOptions connectOptions, int qos) {
        this.mqttClient = mqttClient;
        this.connectOptions = connectOptions;
        this.qos = qos;
    }

    public synchronized void connect() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect(connectOptions);
                log.info("MQTT broker connected: clientId={} broker={}",
                        mqttClient.getClientId(), mqttClient.getServerURI());
            }
        } catch (MqttException ex) {
            log.error("MQTT connection failed: broker={} reason={}",
                    mqttClient.getServerURI(), ex.getMessage());
        }
    }

    public synchronized void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("MQTT broker disconnected: clientId={}", mqttClient.getClientId());
            }
            mqttClient.close();
        } catch (MqttException ex) {
            log.warn("MQTT disconnect failed: {}", ex.getMessage());
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void publish(String topic, byte[] payload) {
        try {
            ensureConnected();
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setRetained(false);
            mqttClient.publish(topic, message);
            log.info("MQTT event published: topic={}", topic);
        } catch (MqttException ex) {
            log.error("MQTT publish failed: topic={} reason={}", topic, ex.getMessage());
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            ensureConnected();
            mqttClient.subscribe(topic, qos, listener);
            log.info("MQTT subscribed: topic={}", topic);
        } catch (MqttException ex) {
            log.error("MQTT subscribe failed: topic={} reason={}", topic, ex.getMessage());
        }
    }

    private void ensureConnected() throws MqttException {
        if (!mqttClient.isConnected()) {
            log.info("MQTT reconnecting: broker={}", mqttClient.getServerURI());
            mqttClient.connect(connectOptions);
            log.info("MQTT broker reconnected: clientId={}", mqttClient.getClientId());
        }
    }
}
