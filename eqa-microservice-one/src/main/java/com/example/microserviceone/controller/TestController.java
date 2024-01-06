package com.example.microserviceone.controller;

import com.example.microserviceone.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/ms-one")

public class TestController {

    @Autowired
    private Environment environment;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private final TestService testService;

    public TestController(RestTemplate restTemplate, TestService testService){
        this.restTemplate = restTemplate;
        this.testService = testService;
    }


    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok(testService.hello());
    }
    @GetMapping("/displayMessage")
    public ResponseEntity<String> showMessage(){
//        ResponseEntity<String> response = restTemplate.exchange("http://localhost:8765/serviceA/displayMessage",
//                HttpMethod.GET, null, String.class);
//        String res = response.getBody();
//        return ResponseEntity.ok(res);

        //throw new RuntimeException("Simulated failure in Microservice1");
        String serverPort = environment.getProperty("local.server.port");
        return ResponseEntity.ok("Microservice 1 controller executed: " +serverPort);
    }
}
