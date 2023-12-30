package com.eqa.eqagatewayservice.controller;

import com.eqa.eqagatewayservice.service.FallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @Autowired
    FallbackService fallbackService;

    @GetMapping("/custom-fallback")
    public Mono<String> getDefaultFallbackMessage() {
        log.info("getDefaultFallbackMessage()");
        String message = fallbackService.getDefaultFallbackMessage();
        return Mono.just(message);
    }
    @GetMapping("/hello")
    public Mono<String> hello() {
        log.info("hello()");
        String message = fallbackService.hello();
        return Mono.just(message);
    }

}
