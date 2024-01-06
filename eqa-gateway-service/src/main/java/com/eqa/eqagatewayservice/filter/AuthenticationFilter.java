package com.eqa.eqagatewayservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;

@Component
@RefreshScope
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
            ServerHttpRequest request = exchange.getRequest();
            if (this.isAuthMissing(request))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is missing in request");

            final String token = this.getAuthHeader(request);
            validateToken(token, exchange);

            return chain.filter(exchange);
        });
    }

    private void validateToken(String token, ServerWebExchange exchange) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"token\": \"" + token + "\"}";
        log.info("Token validation url is {}", validateTokenUrl);
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token - Didn't received success status");
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while validating token");
        }
    }
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0).substring("Bearer ".length());
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
    public static class Config {

    }
}
