package com.eqa.eqagatewayservice.filter;

import com.eqa.eqagatewayservice.exception.TokenValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.error("Missing authorization header");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String requestBody = "{\"token\": \"" + authHeader + "\"}";

                HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(validateTokenUrl, HttpMethod.POST, httpEntity, String.class);

                HttpStatusCode statusCode = response.getStatusCode();
                String responseBody = response.getBody();

                // Check response status code
                if (statusCode == HttpStatus.OK) {
                    // Parse JSON response body
                    String status = extractStatusFromJson(responseBody);

                    // Check the value of the "status" field
                    if ("success".equalsIgnoreCase(status)) {
                        log.info("Token is valid!");
                    } else {
                        log.error("Token validation failed. Response body: {}", responseBody);
                        throw new TokenValidationException("Token validation failed");
                    }
                } else {
                    log.error("Token validation failed. HTTP Status Code: {}, Response body: {}", statusCode, responseBody);
                    throw new TokenValidationException("Token validation failed");
                }
            } catch (ResponseStatusException ex) {
                throw ex;
            } catch (TokenValidationException ex) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
            } catch (Exception e) {
                log.error("Error while validating token", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
            }
            return chain.filter(exchange);
        });
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

    public static class Config {

    }
}
