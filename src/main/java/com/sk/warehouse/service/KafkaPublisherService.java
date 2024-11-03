package com.sk.warehouse.service;

import com.sk.warehouse.model.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @EventListener
    @Async
    public void publish(KafkaEvent event) {
        log.info("Publishing message {}", event);
        kafkaTemplate.send(event.getSensorType(), event.getData());
        log.info("Message published successfully");
    }
}
