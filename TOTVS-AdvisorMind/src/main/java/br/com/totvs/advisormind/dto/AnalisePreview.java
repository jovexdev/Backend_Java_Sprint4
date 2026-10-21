package br.com.totvs.advisormind.dto;

public record AnalisePreview(
    long reuniaoId,
    String tipo,
    String resultado,
    String modeloIa,
    int tokensUtilizados,
    boolean persistida) {}
