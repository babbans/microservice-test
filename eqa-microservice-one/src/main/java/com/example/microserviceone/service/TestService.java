package com.example.microserviceone.service;

import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Observed(name = "testService")
@Service
public class TestService {

    @Autowired
    private Environment environment;

    public String hello() {
        String serverPort = environment.getProperty("local.server.port");
        return "Microservice 1 controller executed:" + serverPort;
    }
}