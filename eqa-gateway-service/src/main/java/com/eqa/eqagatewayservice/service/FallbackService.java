package com.eqa.eqagatewayservice.service;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Observed(name = "testService")
public class FallbackService {

    private static final Logger log = LoggerFactory.getLogger(FallbackService.class);
    public String getDefaultFallbackMessage() {
        return "This is default msg for circuit breaker";
    }

    public String hello() {
        log.info("hello()");
        return "Hello ! from Gateway";
    }
}
