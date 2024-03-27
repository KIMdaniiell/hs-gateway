package ru.itmo.hsgateway.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String BASE_URL = "http://auth-service";

    @Bean("microservice1WebClientBuilder")
    @LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder()
                .baseUrl(BASE_URL);
    }
}
