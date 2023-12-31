package com.eqa.eqagatewayservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${validateTokenUrl}")
    private String validateTokenUrl;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                if (this.isAuthMissing(request))
                    return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);

                final String token = this.getAuthHeader(request);
                validateToken(token, exchange);
            } catch (Exception e) {
                log.error("Error while validating token", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
            }
            return chain.filter(exchange);
        });
    }
    private void validateToken(String token, ServerWebExchange exchange) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"token\": \"" + token + "\"}";

        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(validateTokenUrl, HttpMethod.POST, httpEntity, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        String responseBody = response.getBody();

        if (statusCode == HttpStatus.OK) {
            String status = extractStatusFromJson(responseBody);
            if ("success".equalsIgnoreCase(status)) {
                log.info("Token is valid!");
            } else {
                log.error("Token validation failed. Response body: {}", responseBody);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
            }
        } else {
            log.error("Token validation failed. HTTP Status Code: {}, Response body: {}", statusCode, responseBody);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Didn't received Ok status from token api");
        }
    }
    private String extractStatusFromJson(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            return jsonNode.get("status").asText();
        } catch (IOException e) {
            log.error("Error extracting 'status' from JSON response: {}", jsonResponse, e);
            throw new RuntimeException("Error extracting 'status' from JSON response");
        }
    }
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
    public static class Config {

    }
}
