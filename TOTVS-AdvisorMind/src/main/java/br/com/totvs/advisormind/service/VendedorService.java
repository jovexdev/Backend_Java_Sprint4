package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.dto.VendedorRequest;
import br.com.totvs.advisormind.entities.Vendedor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendedorService {
  private final VendedorDao dao;
  private final VendedorRepoDao repos;
  private final ValidationService valid;

  public VendedorService(VendedorDao dao, VendedorRepoDao repos, ValidationService valid) {
    this.dao = dao;
    this.repos = repos;
    this.valid = valid;
  }

  public List<Vendedor> listar() {
    return dao.listar();
  }

  public Vendedor buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  private Vendedor convert(long id, VendedorRequest r) {
    valid.validar(r);
    valid.texto("nome", r.nome(), 100);
    valid.texto("cargo", r.cargo(), 50);
    valid.texto("setor", r.setor(), 50);
    return new Vendedor(id, r.nome(), r.cargo(), r.setor(), r.totalReunioes(), r.vendedorRepoId());
  }

  private void locks(long... ids) {
    Arrays.stream(ids).distinct().sorted().forEach(id -> valid.existe(repos.bloquear(id)));
  }

  @Transactional
  public Vendedor criar(VendedorRequest r) {
    var v = convert(0, r);
    locks(v.getVendedorRepoId());
    long id = dao.criar(v);
    repos.sincronizar(v.getVendedorRepoId());
    return buscar(id);
  }

  @Transactional
  public Vendedor atualizar(long id, VendedorRequest r) {
    valid.id(id);
    var v = convert(id, r);
    var old = valid.existe(dao.bloquear(id));
    locks(old.getVendedorRepoId(), v.getVendedorRepoId());
    dao.atualizar(v);
    repos.sincronizar(old.getVendedorRepoId());
    repos.sincronizar(v.getVendedorRepoId());
    return buscar(id);
  }

  @Transactional
  public void deletar(long id) {
    valid.id(id);
    var old = valid.existe(dao.bloquear(id));
    locks(old.getVendedorRepoId());
    dao.excluir(id);
    repos.sincronizar(old.getVendedorRepoId());
  }

  @Transactional
  public Vendedor registrarReuniao(long id) {
    valid.id(id);
    var v = valid.existe(dao.bloquear(id));
    if (v.getTotalReunioes() == Integer.MAX_VALUE)
      throw new br.com.totvs.advisormind.exception.ApiException(
          409, "Contador atingiu o limite suportado.");
    v.registrarReuniao();
    dao.atualizar(v);
    return buscar(id);
  }
}
