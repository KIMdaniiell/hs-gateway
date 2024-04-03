package ru.itmo.hsgateway.configuration.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.itmo.hsgateway.model.MessageDTO;
import ru.itmo.hsgateway.proxy.AuthProxy;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

    private final RouteValidator validator;
    private final AuthProxy authProxy;


    public AuthenticationFilterFactory(RouteValidator validator, AuthProxy authProxy) {
        super(Config.class);
        this.validator = validator;
        this.authProxy = authProxy;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Шаг 0: Защищаемый путь или нет?
            if (!validator.isSecured.test(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            // Шаг 1: Содержит заголовок или нет?
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.out.println("Запрос не содержит заголовок AUTHORIZATION");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.just(response.bufferFactory()
                        .wrap("{\"message\": \"The request does not contain the AUTHORIZATION header\"}"
                                .getBytes(StandardCharsets.UTF_8))));
//                return response.setComplete();
            }

            // Шаг 2: Содержит токен в заголовке или нет?
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                System.out.println("В заголовке AUTHORIZATION запроса не содержится Bearer токен");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.just(response.bufferFactory()
                        .wrap("{\"message\": \"The AUTHORIZATION header of the request does not contain a Bearer token\"}"
                                .getBytes(StandardCharsets.UTF_8))));
//                return response.setComplete();
            }

            // Шаг 3: Валидация токена
            try {
                Mono<MessageDTO> messageDTOmono = authProxy.validateToken(authHeader);

                return messageDTOmono
                        .flatMap(r -> {

                            System.out.println("\t┗━━━ Message: " + r.getMessage().replaceFirst("\n"," "));

                            if (r.getSummary().equals("Token is valid")) {
                                ServerWebExchange exchange1 = exchange.mutate().request(
                                        exchange.getRequest().mutate().header("ROLE_NAME", getRolesFromMessage(r.getMessage())).build()
                                ).build();
                                return chain.filter(exchange1);
                            } else {
                                ServerHttpResponse response = exchange.getResponse();
                                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                return response.writeWith(Mono.just(response.bufferFactory()
                                        .wrap("{\n    \"timestamp\": \"%s\",\n \"summary\": \"%s\",\n \"message\": \"%s\",\n \"token\": \"%s\"\n }"
                                                .formatted(r.getTimestamp().toString(),
                                                        r.getSummary(),
                                                        r.getMessage(),
                                                        r.getToken())
                                                .getBytes(StandardCharsets.UTF_8))));
//                                return response.setComplete();
                            }
                        });
            } catch (Exception e) {
                System.out.println("invalid access...!");
                System.out.println(e.getMessage());
            }
            return null;
        };
    }

    public static class Config {
    }

    private String getRolesFromMessage(String message) {
        String m1 = message.substring(message.indexOf("roles=[") + 7);
        return m1.substring(0, m1.indexOf("]"));
    }
}
