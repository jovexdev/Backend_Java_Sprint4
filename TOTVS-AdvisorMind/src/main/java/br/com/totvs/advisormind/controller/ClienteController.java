package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.ClienteRequest;
import br.com.totvs.advisormind.entities.Cliente;
import br.com.totvs.advisormind.service.ClienteService;
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
    Cliente v = service.criar(req);
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
