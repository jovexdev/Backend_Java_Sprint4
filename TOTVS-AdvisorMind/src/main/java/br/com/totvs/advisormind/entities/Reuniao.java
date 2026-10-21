package br.com.totvs.advisormind.entities;

import java.time.LocalDateTime;

public class Reuniao {
  private long id;
  private String transcricao;
  private LocalDateTime data;
  private StatusReuniao status;
  private long clienteId;
  private long vendedorId;
  private long reuniaoRepoId;

  public Reuniao() {}

  public Reuniao(
      long id,
      String transcricao,
      LocalDateTime data,
      StatusReuniao status,
      long clienteId,
      long vendedorId,
      long reuniaoRepoId) {
    this.id = id;
    this.transcricao = transcricao;
    this.data = data;
    this.status = status;
    this.clienteId = clienteId;
    this.vendedorId = vendedorId;
    this.reuniaoRepoId = reuniaoRepoId;
  }

  public long getId() {
    return id;
  }

  public void setId(long value) {
    this.id = value;
  }

  public String getTranscricao() {
    return transcricao;
  }

  public void setTranscricao(String value) {
    this.transcricao = value;
  }

  public LocalDateTime getData() {
    return data;
  }

  public void setData(LocalDateTime value) {
    this.data = value;
  }

  public StatusReuniao getStatus() {
    return status;
  }

  public void setStatus(StatusReuniao value) {
    this.status = value;
  }

  public long getClienteId() {
    return clienteId;
  }

  public void setClienteId(long value) {
    this.clienteId = value;
  }

  public long getVendedorId() {
    return vendedorId;
  }

  public void setVendedorId(long value) {
    this.vendedorId = value;
  }

  public long getReuniaoRepoId() {
    return reuniaoRepoId;
  }

  public void setReuniaoRepoId(long value) {
    this.reuniaoRepoId = value;
  }

  private static final String[] PALAVRAS_POSITIVAS = {
    "comprar", "fechar", "fechamento", "orçamento", "proposta",
    "contratar", "aprovado", "interessado", "avançar", "gostei"
  };

  private static final String[] PALAVRAS_NEGATIVAS = {
    "caro", "concorrente", "cancelar", "desistir", "não temos verba",
    "sem orçamento", "problema", "reclamação", "insatisfeito", "adiar"
  };

  public String calcularTemperaturaComercial() {

    if (transcricao == null || transcricao.trim().isEmpty()) {
      return "SEM DADOS SUFICIENTES";
    }

    String textoLower = transcricao.toLowerCase();

    int pontosPositivos = 0;
    for (String palavra : PALAVRAS_POSITIVAS) {
      if (textoLower.contains(palavra)) {
        pontosPositivos++;
      }
    }

    int pontosNegativos = 0;
    for (String palavra : PALAVRAS_NEGATIVAS) {
      if (textoLower.contains(palavra)) {
        pontosNegativos++;
      }
    }

    int saldo = pontosPositivos - pontosNegativos;

    if (saldo >= 2) {
      return "QUENTE";
    } else if (saldo <= -2) {
      return "FRIO";
    } else {
      return "MORNO";
    }
  }

  // ===================== REGRA DE NEGÓCIO =====================
  // Estima a duração da reunião em minutos a partir da quantidade de
  // palavras da transcrição, considerando um ritmo médio de fala de
  // 130 palavras por minuto (heurística usada para dimensionar agenda).
  public int estimarDuracaoMinutos() {

    if (transcricao == null || transcricao.trim().isEmpty()) {
      return 0;
    }

    String[] palavras = transcricao.trim().split("\\s+");
    int totalPalavras = palavras.length;

    final int PALAVRAS_POR_MINUTO = 130;

    int minutos = (int) Math.ceil((double) totalPalavras / PALAVRAS_POR_MINUTO);

    return Math.max(minutos, 1);
  }

  @Override
  public String toString() {
    return "Reuniao #" + id;
  }
}
