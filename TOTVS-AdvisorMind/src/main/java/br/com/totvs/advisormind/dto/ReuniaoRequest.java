package br.com.totvs.advisormind.dto;

import br.com.totvs.advisormind.entities.StatusReuniao;
import jakarta.validation.constraints.*;
import java.time.*;

public record ReuniaoRequest(
    @NotBlank @Size(max = 4000) String transcricao,
    @NotNull
        @com.fasterxml.jackson.annotation.JsonFormat(
            pattern = "uuuu-MM-dd'T'HH:mm:ss",
            lenient = com.fasterxml.jackson.annotation.OptBoolean.FALSE)
        LocalDateTime data,
    @NotNull StatusReuniao status,
    @NotNull @Positive Long clienteId,
    @NotNull @Positive Long vendedorId,
    @NotNull @Positive Long reuniaoRepoId) {}
