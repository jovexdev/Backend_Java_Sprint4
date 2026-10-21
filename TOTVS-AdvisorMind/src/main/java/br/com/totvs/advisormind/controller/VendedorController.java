package br.com.totvs.advisormind.controller;

import br.com.totvs.advisormind.dto.VendedorRequest;
import br.com.totvs.advisormind.entities.Vendedor;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {
  private final VendedorService service;

  public VendedorController(VendedorService service) {
    this.service = service;
  }

  @GetMapping
  public List<Vendedor> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public Vendedor buscar(@PathVariable long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<Vendedor> criar(@Valid @RequestBody VendedorRequest req) {
    var v = service.criar(req);
    return ResponseEntity.created(URI.create("/api/vendedores/" + v.getId())).body(v);
  }

  @PutMapping("/{id}")
  public Vendedor atualizar(@PathVariable long id, @Valid @RequestBody VendedorRequest req) {
    return service.atualizar(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletar(@PathVariable long id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/indicadores")
  public Map<String, Object> indicadores(
      @PathVariable long id, @RequestParam(required = false) java.math.BigDecimal ticketMedio) {
    var v = service.buscar(id);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("senioridade", v.calcularNivelSenioridade());
    out.put(
        "comissaoEstimada", ticketMedio == null ? null : v.calcularComissaoEstimada(ticketMedio));
    out.put("ticketMedioInformado", ticketMedio);
    out.put(
        "natureza",
        "Simulação acadêmica; ticket não persistido. Sem ticket, comissão indisponível.");
    return out;
  }

  @PostMapping("/{id}/registrar-reuniao")
  public Vendedor registrar(@PathVariable long id) {
    return service.registrarReuniao(id);
  }
}
