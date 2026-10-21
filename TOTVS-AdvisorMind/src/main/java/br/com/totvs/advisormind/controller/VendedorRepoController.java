package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.VendedorRepoRequest;
import br.com.totvs.advisormind.entities.VendedorRepo;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendedor-repos")
public class VendedorRepoController {
  private final VendedorRepoService service;

  public VendedorRepoController(VendedorRepoService service) {
    this.service = service;
  }

  @GetMapping
  public List<VendedorRepo> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public VendedorRepo buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<VendedorRepo> criar(@Valid @RequestBody VendedorRepoRequest req) {
    var v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/vendedor-repos/" + v.id())).body(v);
  }

  @PutMapping("/{id}")
  public VendedorRepo atualizar(
      @PathVariable long id, @Valid @RequestBody VendedorRepoRequest req) {
    return service.atualizar(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
