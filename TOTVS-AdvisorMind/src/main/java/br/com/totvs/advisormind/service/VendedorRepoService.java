package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.VendedorRepoDao;
import br.com.totvs.advisormind.dto.VendedorRepoRequest;
import br.com.totvs.advisormind.entities.VendedorRepo;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendedorRepoService {
  private final VendedorRepoDao dao;
  private final ValidationService valid;

  public VendedorRepoService(VendedorRepoDao dao, ValidationService valid) {
    this.dao = dao;
    this.valid = valid;
  }

  public List<VendedorRepo> listar() {
    return dao.listar();
  }

  public VendedorRepo buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  @Transactional
  public VendedorRepo criar(VendedorRepoRequest r) {
    valid.validar(r);
    valid.texto("filtro", r.filtro(), 100);
    return buscar(dao.criar(new VendedorRepo(0, 0, r.filtro())));
  }

  @Transactional
  public VendedorRepo atualizar(long id, VendedorRepoRequest r) {
    valid.id(id);
    valid.validar(r);
    valid.texto("filtro", r.filtro(), 100);
    var old = valid.existe(dao.bloquear(id));
    dao.atualizar(new VendedorRepo(id, old.totalVendedores(), r.filtro()));
    dao.sincronizar(id);
    return buscar(id);
  }

  @Transactional
  public void deletar(long id) {
    buscar(id);
    dao.excluir(id);
  }
}
