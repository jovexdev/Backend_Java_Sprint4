package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.ReuniaoRepoRequest;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.service.ReuniaoRepoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reuniao-repos")
public class ReuniaoRepoController {
  private final ReuniaoRepoService service;

  public ReuniaoRepoController(ReuniaoRepoService service) {
    this.service = service;
  }

  @GetMapping
  public List<ReuniaoRepo> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public ReuniaoRepo buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<ReuniaoRepo> criar(@Valid @RequestBody ReuniaoRepoRequest req) {
    ReuniaoRepo v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/reuniao-repos/" + v.getId())).body(v);
  }

  @PutMapping("/{id}")
  public ReuniaoRepo atualizar(@PathVariable long id, @Valid @RequestBody ReuniaoRepoRequest req) {
    return service.atualizar(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
