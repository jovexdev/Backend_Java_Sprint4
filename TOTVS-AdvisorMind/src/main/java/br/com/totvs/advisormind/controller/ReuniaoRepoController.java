package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.ReuniaoRepoRequest;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    var v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/reuniao-repos/" + v.id())).body(v);
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
