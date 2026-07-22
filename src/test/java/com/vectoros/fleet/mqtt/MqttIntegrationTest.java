package com.vectoros.fleet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.mqtt.config.MqttClientGateway;
import com.vectoros.fleet.mqtt.config.MqttProperties;
import com.vectoros.fleet.mqtt.events.RobotStatusEvent;
import com.vectoros.fleet.mqtt.events.TaskAssignedEvent;
import com.vectoros.fleet.mqtt.publisher.RobotCommandPublisher;
import com.vectoros.fleet.mqtt.serialization.MqttEventSerializer;
import com.vectoros.fleet.mqtt.topics.MqttTopics;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class MqttIntegrationTest {

    @Container
    static GenericContainer<?> mosquitto = new GenericContainer<>("eclipse-mosquitto:2")
            .withExposedPorts(1883)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mosquitto-test.conf"),
                    "/mosquitto/config/mosquitto.conf");

    private MqttClientGateway gateway;
    private MqttEventSerializer serializer;
    private RobotCommandPublisher publisher;
    private MqttClient subscriberClient;

    @BeforeEach
    void setUp() throws Exception {
        String brokerUrl = "tcp://" + mosquitto.getHost() + ":" + mosquitto.getMappedPort(1883);

        MqttProperties properties = new MqttProperties();
        properties.setBrokerUrl(brokerUrl);
        properties.setClientId("vectoros-fleet-test");
        properties.setQos(1);
        properties.setConnectionTimeout(10);
        properties.setKeepAliveInterval(30);
        properties.setAutomaticReconnect(true);
        properties.setCleanSession(true);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        MqttClient publisherClient = new MqttClient(brokerUrl, "fleet-publisher-" + System.currentTimeMillis(),
                new MemoryPersistence());
        gateway = new MqttClientGateway(publisherClient, options, 1);
        gateway.connect();
        assertTrue(gateway.isConnected());

        serializer = new MqttEventSerializer(new ObjectMapper().findAndRegisterModules());
        ObjectProvider<MqttClientGateway> provider = new ObjectProvider<>() {
            @Override
            public MqttClientGateway getObject() throws BeansException {
                return gateway;
            }

            @Override
            public MqttClientGateway getObject(Object... args) throws BeansException {
                return gateway;
            }

            @Override
            public MqttClientGateway getIfAvailable() throws BeansException {
                return gateway;
            }

            @Override
            public MqttClientGateway getIfAvailable(java.util.function.Supplier<MqttClientGateway> defaultSupplier)
                    throws BeansException {
                return gateway;
            }

            @Override
            public void ifAvailable(Consumer<MqttClientGateway> dependencyConsumer) throws BeansException {
                dependencyConsumer.accept(gateway);
            }

            @Override
            public MqttClientGateway getIfUnique() throws BeansException {
                return gateway;
            }

            @Override
            public MqttClientGateway getIfUnique(java.util.function.Supplier<MqttClientGateway> defaultSupplier)
                    throws BeansException {
                return gateway;
            }

            @Override
            public void ifUnique(Consumer<MqttClientGateway> dependencyConsumer) throws BeansException {
                dependencyConsumer.accept(gateway);
            }

            @Override
            public Stream<MqttClientGateway> stream() {
                return Stream.of(gateway);
            }

            @Override
            public Stream<MqttClientGateway> orderedStream() {
                return Stream.of(gateway);
            }
        };
        publisher = new RobotCommandPublisher(provider, serializer);

        subscriberClient = new MqttClient(brokerUrl, "fleet-subscriber-" + System.currentTimeMillis(),
                new MemoryPersistence());
        subscriberClient.connect(options);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (subscriberClient != null && subscriberClient.isConnected()) {
            subscriberClient.disconnect();
            subscriberClient.close();
        }
        if (gateway != null) {
            gateway.disconnect();
        }
    }

    @Test
    @Timeout(30)
    void publishTaskAssignedEvent_isReceivedBySubscriber() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TaskAssignedEvent> received = new AtomicReference<>();

        subscriberClient.subscribe(MqttTopics.TASKS_ASSIGNED, (topic, message) -> {
            received.set(serializer.deserialize(message.getPayload(), TaskAssignedEvent.class));
            latch.countDown();
        });

        TaskAssignedEvent event = TaskAssignedEvent.builder()
                .taskId(15L)
                .taskNumber("TASK-000015")
                .robotId(3L)
                .pickupLocation("A12")
                .dropoffLocation("C18")
                .priority(Priority.HIGH)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        publisher.publishTaskAssigned(event);

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertNotNull(received.get());
        assertEquals(event, received.get());
    }

    @Test
    @Timeout(30)
    void subscribeRobotStatus_deserializesIncomingEvent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RobotStatusEvent> received = new AtomicReference<>();

        subscriberClient.subscribe(MqttTopics.ROBOTS_STATUS, (topic, message) -> {
            received.set(serializer.deserialize(message.getPayload(), RobotStatusEvent.class));
            latch.countDown();
        });

        RobotStatusEvent event = RobotStatusEvent.builder()
                .robotId(2L)
                .status(RobotStatus.WORKING)
                .batteryLevel(76)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        gateway.publish(MqttTopics.ROBOTS_STATUS, serializer.serialize(event));

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(event, received.get());
    }
}
