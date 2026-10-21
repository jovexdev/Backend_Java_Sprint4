package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.ReuniaoRepoDao;
import br.com.totvs.advisormind.dto.ReuniaoRepoRequest;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.util.JsonUtil;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReuniaoRepoService {
  private final ReuniaoRepoDao dao;
  private final ValidationService valid;

  public ReuniaoRepoService(ReuniaoRepoDao dao, ValidationService valid) {
    this.dao = dao;
    this.valid = valid;
  }

  public List<ReuniaoRepo> listar() {
    return dao.listar();
  }

  public ReuniaoRepo buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  @Transactional
  public ReuniaoRepo criar(ReuniaoRepoRequest r) {
    valid.validar(r);
    return buscar(dao.criar(new ReuniaoRepo(0, JsonUtil.indice(List.of()), 0)));
  }

  @Transactional
  public ReuniaoRepo atualizar(long id, ReuniaoRepoRequest r) {
    valid.validar(r);
    valid.id(id);
    valid.existe(dao.bloquear(id));
    sincronizar(id);
    return buscar(id);
  }

  public void sincronizar(long id) {
    var ids = dao.reunioes(id);
    dao.atualizar(new ReuniaoRepo(id, JsonUtil.indice(ids), ids.size()));
  }

  @Transactional
  public void deletar(long id) {
    buscar(id);
    dao.excluir(id);
  }
}
