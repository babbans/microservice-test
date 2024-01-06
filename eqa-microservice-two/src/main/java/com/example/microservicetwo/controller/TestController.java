package com.example.microservicetwo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ms-two")
@RestController
public class TestController {
    @Autowired
    private Environment environment;

    @GetMapping("/hello")
    public ResponseEntity<String> showMessage(){
        String serverPort = environment.getProperty("local.server.port");
        return ResponseEntity.ok("Microservice 2 controller executed:"+serverPort);
    }
}