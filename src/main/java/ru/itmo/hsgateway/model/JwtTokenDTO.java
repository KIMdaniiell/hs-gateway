package ru.itmo.hsgateway.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenDTO {
    @NotNull(message = "token field can't be null")
    @NotBlank(message = "token field can't be blank")
    private String token;
}
