package com.eqa.eqagatewayservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Configuration
public class LoggingGlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Bean
    public GlobalFilter globalLoggingFilter() {
        return (exchange, chain) -> {
            log.info("Request {}: {} {}", exchange.getRequest().getId(),
                    exchange.getRequest().getMethod(), exchange.getRequest().getURI());

            return chain.filter(exchange)
                    .doOnError(throwable -> {
                        if (throwable instanceof ResponseStatusException) {
                            ResponseStatusException ex = (ResponseStatusException) throwable;
                            log.error("Error {}: {}", exchange.getRequest().getId(), ex.getMessage());
                        } else if (throwable != null) {
                            log.error("Error {}: Unexpected error", exchange.getRequest().getId(), throwable);
                        }
                    })
                    .doOnSuccess(result -> {
                        ServerHttpResponse response = exchange.getResponse();
                        log.info("Response {}: {}", exchange.getRequest().getId(), response.getStatusCode());
                    });
        };
    }
}


