package com.sk.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.DatagramSocket;
import java.net.SocketException;

@Configuration
public class DatagramSocketConfig {

    private static final int TEMP_PORT = 3344;
    private static final int HUMIDITY_PORT = 3355;

    @Bean
    public DatagramSocket temperatureDatagramSocket() throws SocketException {
        return new DatagramSocket(TEMP_PORT);
    }

    @Bean
    public DatagramSocket humidityDatagramSocket() throws SocketException {
        return new DatagramSocket(HUMIDITY_PORT);
    }
}