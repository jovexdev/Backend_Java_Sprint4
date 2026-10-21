package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.entities.Reuniao;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {
  private final ClienteService clientes;

  public RelatorioService(ClienteService clientes) {
    this.clientes = clientes;
  }

  public Map<String, Object> calcularEstatisticas(List<Reuniao> reunioes) {
    Set<String> empresas = new HashSet<>();
    Reuniao maisExtensa = null;
    for (Reuniao reuniao : reunioes) {
      empresas.add(clientes.buscar(reuniao.getClienteId()).getEmpresa());
      if (maisExtensa == null || reuniao.getTranscricao().length() > maisExtensa.getTranscricao().length()) {
        maisExtensa = reuniao;
      }
    }
    Map<String, Object> resultado = new LinkedHashMap<>();
    resultado.put("totalReunioes", reunioes.size());
    resultado.put("empresasAtendidas", empresas.size());
    resultado.put("reuniaoMaisExtensaId", maisExtensa == null ? 0L : maisExtensa.getId());
    return resultado;
  }

  public void exibirEstatisticas(List<Reuniao> rows) {
    System.out.println(calcularEstatisticas(rows));
  }
}
