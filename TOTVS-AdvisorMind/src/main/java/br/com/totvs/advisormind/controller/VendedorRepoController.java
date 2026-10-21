package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.VendedorRepoRequest;
import br.com.totvs.advisormind.entities.VendedorRepo;
import br.com.totvs.advisormind.service.VendedorRepoService;
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
    VendedorRepo v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/vendedor-repos/" + v.getId())).body(v);
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
