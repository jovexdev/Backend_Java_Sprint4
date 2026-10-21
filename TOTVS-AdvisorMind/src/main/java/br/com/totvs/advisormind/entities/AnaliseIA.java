package br.com.totvs.advisormind.entities;

public record AnaliseIA(
    long id,
    int tipo,
    String resultado,
    String modeloIa,
    java.time.LocalDateTime datahoraExecucao,
    int tokensUtilizados,
    long reuniaoId) {}
