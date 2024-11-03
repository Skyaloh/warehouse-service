package com.sk.warehouse.service;

import com.sk.warehouse.model.KafkaEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class WareHouseService {

    @Value("${server.address}")
    private String central_service_host;
    private static final int TEMP_PORT = 3344;
    private static final int HUMIDITY_PORT = 3355;

    private final ApplicationEventPublisher eventPublisher;

    private static final int TEMP_THRESHOLD = 35;
    private static final int HUMIDITY_THRESHOLD = 50;

    private final DatagramSocket temperatureSocket;
    private final DatagramSocket humiditySocket;

    public WareHouseService(ApplicationEventPublisher eventPublisher, @Qualifier("temperatureDatagramSocket") DatagramSocket temperatureSocket, @Qualifier("humidityDatagramSocket") DatagramSocket humiditySocket) {
        this.eventPublisher = eventPublisher;
        this.temperatureSocket = temperatureSocket;
        this.humiditySocket = humiditySocket;
    }

    @PostConstruct
    public void init() {
        listenForSensorData(temperatureSocket, "sensor.temperature").subscribeOn(Schedulers.boundedElastic()).subscribe();
        listenForSensorData(humiditySocket, "sensor.humidity").subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

 /*   @PostConstruct
    public void startSendingData() {
        // Send temperature data every 5 seconds
        Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> sendUDPMessage("sensor_id=t1; value=30", TEMP_PORT))
                .subscribe();

        // Send humidity data every 7 seconds
        Flux.interval(Duration.ofSeconds(7))
                .flatMap(tick -> sendUDPMessage("sensor_id=h1; value=40", HUMIDITY_PORT))
                .subscribe();
    }

    public Mono<Void> sendUDPMessage(String message, int port) {
        return Mono.fromRunnable(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] buffer = message.getBytes();
                InetAddress address = InetAddress.getByName(central_service_host);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                log.info("Sent: {}", message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }*/
    public Flux<String> listenForSensorData(DatagramSocket socket, String sensorType) {
        return Flux.create(emitter -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    eventPublisher.publishEvent(new KafkaEvent(this,sensorType,data));
                    emitter.next(data);
                }
            } catch (Exception e) {
                emitter.error(e);
            }
        });
    }



}
