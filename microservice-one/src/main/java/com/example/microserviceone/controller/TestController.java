package com.example.microserviceone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/serviceA")
public class TestController {

    @Autowired
    private Environment environment;
    @GetMapping("/displayMessage")
    public ResponseEntity<String> showMessage(){
        throw new RuntimeException("Simulated failure in Microservice1");
//        String serverPort = environment.getProperty("local.server.port");
//
//        return ResponseEntity.ok("Microservice 1 controller executed: " +serverPort);
    }
}
