package com.example.microservicetwo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/serviceB")
@RestController
public class TestController {
    @GetMapping("/displayMessage")
    public ResponseEntity<String> showMessage(){
        return ResponseEntity.ok("Microservice 2 controller executed");
    }
}