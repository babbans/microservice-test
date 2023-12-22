package com.example.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FallbackFilter extends AbstractGatewayFilterFactory<FallbackFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(FallbackFilter.class);

    private final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    public FallbackFilter(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
        super(Config.class);
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String circuitBreakerName = "default"; // Or specify your circuit breaker name
            return circuitBreakerFactory.create(circuitBreakerName).run(chain.filter(exchange), throwable -> {
                // Logging for troubleshooting
                logger.error("Fallback logic triggered an error", throwable);
                return Mono.just("This is the default message for the circuit breaker");
            }).flatMap(response -> {
                // Ensure that the response is processed properly
                ServerHttpResponse serverHttpResponse = exchange.getResponse();
                serverHttpResponse.getHeaders().add("Content-Type", "application/json");
                serverHttpResponse.setStatusCode(HttpStatus.OK);
                return serverHttpResponse.writeWith(Mono.just(response.bufferFactory().wrap("This is the default message for the circuit breaker".getBytes())));
            });

        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}










