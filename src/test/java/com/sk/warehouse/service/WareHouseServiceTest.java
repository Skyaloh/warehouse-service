package com.sk.warehouse.service;

import com.sk.warehouse.model.KafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WareHouseServiceTest {

    @Mock
    private DatagramSocket socket;

    private WareHouseService wareHouseService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DatagramSocket temperatureSocket;

    @Mock
    private DatagramSocket humiditySocket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wareHouseService = new WareHouseService(eventPublisher, temperatureSocket, humiditySocket);
    }


    @Test
    void testListenForSensorData() throws Exception {
        // Mock the DatagramSocket to simulate receiving data
        byte[] data = "test data".getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length);
        doAnswer(invocation -> {
            DatagramPacket p = invocation.getArgument(0);
            System.arraycopy(data, 0, p.getData(), 0, data.length);
            p.setLength(data.length);
            return null;
        }).when(temperatureSocket).receive(any(DatagramPacket.class));

        // Call the method to test
        Flux<String> result = wareHouseService.listenForSensorData(temperatureSocket, "sensor.temperature");

        // Verify that the KafkaEvent is published with the correct parameters
        StepVerifier.create(result)
                .expectNext("test data")
                .thenCancel()
                .verify();

        ArgumentCaptor<KafkaEvent> eventCaptor = ArgumentCaptor.forClass(KafkaEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        KafkaEvent event = eventCaptor.getValue();
        assertEquals("sensor.temperature", event.getSensorType());
        assertEquals("test data", event.getData());
    }
}