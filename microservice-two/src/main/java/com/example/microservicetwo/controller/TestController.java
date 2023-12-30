package com.example.microservicetwo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ms-two")
@RestController
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<String> showMessage(){
        return ResponseEntity.ok("Microservice 2 controller executed");
    }
}