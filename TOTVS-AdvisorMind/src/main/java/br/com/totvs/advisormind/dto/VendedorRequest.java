package br.com.totvs.advisormind.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record VendedorRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Size(max = 50) String cargo,
    @NotBlank @Size(max = 50) String setor,
    @NotNull @PositiveOrZero Integer totalReunioes,
    @NotNull @Positive Long vendedorRepoId) {}
