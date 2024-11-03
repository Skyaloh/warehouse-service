package com.sk.warehouse.model;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
@ToString
public class KafkaEvent extends ApplicationEvent {

    private final String timeStamp;

    @NotNull
    private final String sensorType;

    @NotNull
    private final String data;


    public KafkaEvent(Object source, String sensorType, String data) {
        super(source);
        this.sensorType = sensorType;
        this.timeStamp = Instant.now().toString();
        this.data = data;
    }
}