package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.entities.Reuniao;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {
  private final ClienteService clientes;

  public RelatorioService(ClienteService clientes) {
    this.clientes = clientes;
  }

  public Map<String, Object> calcularEstatisticas(List<Reuniao> rows) {
    long companies =
        rows.stream().map(r -> clientes.buscar(r.getClienteId()).getEmpresa()).distinct().count();
    return Map.of(
        "totalReunioes",
        rows.size(),
        "empresasAtendidas",
        companies,
        "reuniaoMaisExtensaId",
        rows.stream()
            .max(Comparator.comparingInt(r -> r.getTranscricao().length()))
            .map(Reuniao::getId)
            .orElse(0L));
  }

  public void exibirEstatisticas(List<Reuniao> rows) {
    System.out.println(calcularEstatisticas(rows));
  }
}
