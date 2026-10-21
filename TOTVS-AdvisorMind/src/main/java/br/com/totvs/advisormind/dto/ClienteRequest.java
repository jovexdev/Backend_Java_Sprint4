package br.com.totvs.advisormind.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record ClienteRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Size(max = 100) String empresa,
    @NotNull
        @PastOrPresent
        @com.fasterxml.jackson.annotation.JsonFormat(
            pattern = "uuuu-MM-dd",
            lenient = com.fasterxml.jackson.annotation.OptBoolean.FALSE)
        LocalDate dataNascimento) {}
