package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.AnalisePreview;
import br.com.totvs.advisormind.entities.AnaliseIA;
import br.com.totvs.advisormind.service.AnaliseService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnaliseController {
  private final AnaliseService service;

  public AnaliseController(AnaliseService service) {
    this.service = service;
  }

  @PostMapping("/api/reunioes/{id}/analises/{tipo}")
  public ResponseEntity<AnaliseIA> criar(@PathVariable long id, @PathVariable String tipo) {
    AnaliseIA v = service.criar(id, tipo);
    return ResponseEntity.created(URI.create("/api/analises/" + v.getId())).body(v);
  }

  @PostMapping("/api/reunioes/{id}/analises/{tipo}/previa")
  public AnalisePreview previa(@PathVariable long id, @PathVariable String tipo) {
    return service.previa(id, tipo);
  }

  @GetMapping("/api/analises")
  public List<AnaliseIA> listar() {
    return service.listar();
  }

  @GetMapping("/api/analises/{id}")
  public AnaliseIA buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @GetMapping("/api/reunioes/{id}/analises")
  public List<AnaliseIA> porReuniao(@PathVariable long id) {
    return service.porReuniao(id);
  }

  @DeleteMapping("/api/analises/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
