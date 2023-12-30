package com.eqa.eqagatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class EqaGatewayServiceApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(EqaGatewayServiceApplication.class, args);
	}
//	@Bean
//	RestTemplate restTemplate() {
//		return new RestTemplate();
//	}
}
