package ru.itmo.hsgateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.itmo.hsgateway.configuration.filters.AuthenticationFilterFactory;

@Configuration
@RequiredArgsConstructor
public class GatewayConfiguration {

    private final AuthenticationFilterFactory factory;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-auth",
                        r -> r
                                .path("/auth/**")
                                .filters(f -> f.prefixPath("/api"))
                                .uri("lb://auth-service"))
                .route("auth-service-users",
                        r -> r
                                .path("/users", "/users/**")
                                .filters(f -> f
                                        .filters(factory.apply(new AuthenticationFilterFactory.Config()))
                                        .prefixPath("/api"))
                                .uri("lb://auth-service"))
                .build();
    }
}
