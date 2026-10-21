package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.ReuniaoRequest;
import br.com.totvs.advisormind.entities.Reuniao;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reunioes")
public class ReuniaoController {
  private final ReuniaoService service;

  public ReuniaoController(
      ReuniaoService service, RelatorioService relatorio, ClienteService clientes) {
    this.service = service;
    this.relatorio = relatorio;
    this.clientes = clientes;
  }

  @GetMapping
  public List<Reuniao> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public Reuniao buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<Reuniao> criar(@Valid @RequestBody ReuniaoRequest req) {
    var v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/reunioes/" + v.getId())).body(v);
  }

  @PutMapping("/{id}")
  public Reuniao atualizar(@PathVariable long id, @Valid @RequestBody ReuniaoRequest req) {
    return service.atualizar(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }

  private final RelatorioService relatorio;
  private final ClienteService clientes;

  @GetMapping("/estatisticas")
  public Map<String, Object> estatisticas() {
    return relatorio.calcularEstatisticas(service.listar());
  }

  @GetMapping("/{id}/indicadores")
  public Map<String, Object> indicadores(@PathVariable long id) {
    var r = service.buscar(id);
    return Map.of(
        "temperaturaComercial",
        r.calcularTemperaturaComercial(),
        "duracaoEstimadaMinutos",
        r.estimarDuracaoMinutos(),
        "perfilComercial",
        clientes.buscar(r.getClienteId()).classificarPerfilComercial(),
        "natureza",
        "Heurísticas acadêmicas, não métricas oficiais da TOTVS.");
  }
}
