package ru.itmo.hsgateway.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.itmo.hsgateway.model.ErrorDTO;
import ru.itmo.hsgateway.model.JwtTokenDTO;
import ru.itmo.hsgateway.model.MessageDTO;

@Component
@RequiredArgsConstructor
public class AuthProxy {

    private final WebClient.Builder webClientBuilder;

    public Mono<MessageDTO> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri("/api/auth/validateToken")
                .bodyValue(new JwtTokenDTO(token))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                        System.out.println("[Веб-Клиент | Валидация JWT] Код ответа - ОК");
                        return clientResponse.bodyToMono(MessageDTO.class);
                    } else {
                        System.out.println("[Веб-Клиент | Валидация JWT] Код ответа - ОШИБКА");
                        return clientResponse.bodyToMono(ErrorDTO.class)
                                .map(errorDTO -> new MessageDTO(errorDTO.getError(), errorDTO.getMessage(), ""));
                    }
                });
//                .retrieve()
//                .bodyToMono(MessageDTO.class);
    }

}
