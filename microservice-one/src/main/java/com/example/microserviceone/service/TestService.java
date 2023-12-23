package com.example.microserviceone.service;

import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Observed(name = "testService")
@Service
public class TestService {

    public String greet() {
        return "Hello World!";
    }
}