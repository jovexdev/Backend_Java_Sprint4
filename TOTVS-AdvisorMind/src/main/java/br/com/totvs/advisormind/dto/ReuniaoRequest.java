package br.com.totvs.advisormind.dto;

import br.com.totvs.advisormind.entities.StatusReuniao;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ReuniaoRequest {
  @NotBlank @Size(max = 4000)
  private String transcricao;
  @NotNull @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss", lenient = OptBoolean.FALSE)
  private LocalDateTime data;
  @NotNull
  private StatusReuniao status;
  @NotNull @Positive
  private Long clienteId;
  @NotNull @Positive
  private Long vendedorId;
  @NotNull @Positive
  private Long reuniaoRepoId;

  public ReuniaoRequest() {}

  public ReuniaoRequest(String transcricao, LocalDateTime data, StatusReuniao status, Long clienteId, Long vendedorId, Long reuniaoRepoId) {
    this.transcricao = transcricao;
    this.data = data;
    this.status = status;
    this.clienteId = clienteId;
    this.vendedorId = vendedorId;
    this.reuniaoRepoId = reuniaoRepoId;
  }

  public String getTranscricao() {
    return transcricao;
  }

  public void setTranscricao(String transcricao) {
    this.transcricao = transcricao;
  }

  public LocalDateTime getData() {
    return data;
  }

  public void setData(LocalDateTime data) {
    this.data = data;
  }

  public StatusReuniao getStatus() {
    return status;
  }

  public void setStatus(StatusReuniao status) {
    this.status = status;
  }

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
    this.clienteId = clienteId;
  }

  public Long getVendedorId() {
    return vendedorId;
  }

  public void setVendedorId(Long vendedorId) {
    this.vendedorId = vendedorId;
  }

  public Long getReuniaoRepoId() {
    return reuniaoRepoId;
  }

  public void setReuniaoRepoId(Long reuniaoRepoId) {
    this.reuniaoRepoId = reuniaoRepoId;
  }
}
