package br.com.totvs.advisormind.dto;

public record OpenAICompletion(String resultado, String modelo, int tokensUtilizados) {}
