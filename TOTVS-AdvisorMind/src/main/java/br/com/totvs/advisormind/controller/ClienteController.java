package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.ClienteRequest;
import br.com.totvs.advisormind.entities.Cliente;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
  private final ClienteService service;

  public ClienteController(ClienteService service) {
    this.service = service;
  }

  @GetMapping
  public List<Cliente> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public Cliente buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<Cliente> criar(@Valid @RequestBody ClienteRequest req) {
    var v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/clientes/" + v.getId())).body(v);
  }

  @PutMapping("/{id}")
  public Cliente atualizar(@PathVariable long id, @Valid @RequestBody ClienteRequest req) {
    return service.atualizar(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
