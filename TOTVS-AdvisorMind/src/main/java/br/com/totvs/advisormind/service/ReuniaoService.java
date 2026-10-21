package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.dto.ReuniaoRequest;
import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.dao.ReuniaoDao;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReuniaoService {
  private final ReuniaoDao dao;
  private final ClienteService clientes;
  private final VendedorService vendedores;
  private final ReuniaoRepoDao repos;
  private final ReuniaoRepoService repoService;
  private final AnaliseIADao analises;
  private final ValidationService valid;

  public ReuniaoService(
      ReuniaoDao dao,
      ClienteService clientes,
      VendedorService vendedores,
      ReuniaoRepoDao repos,
      ReuniaoRepoService repoService,
      AnaliseIADao analises,
      ValidationService valid) {
    this.dao = dao;
    this.clientes = clientes;
    this.vendedores = vendedores;
    this.repos = repos;
    this.repoService = repoService;
    this.analises = analises;
    this.valid = valid;
  }

  public List<Reuniao> listar() {
    return dao.listar();
  }

  public Reuniao buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  private Reuniao convert(long id, ReuniaoRequest r) {
    valid.validar(r);
    valid.texto("transcricao", r.transcricao(), 4000);
    if (r.data().getYear() < 1 || r.data().getYear() > 9999)
      throw new ApiException(400, "Data fora do intervalo suportado (anos 1 a 9999).");
    if (r.data().getNano() != 0)
      throw new ApiException(
          400, "Oracle DATE usa segundos inteiros; não envie frações de segundo.");
    clientes.buscar(r.clienteId());
    vendedores.buscar(r.vendedorId());
    return new Reuniao(
        id,
        r.transcricao(),
        r.data(),
        r.status(),
        r.clienteId(),
        r.vendedorId(),
        r.reuniaoRepoId());
  }

  private void locks(long... ids) {
    Arrays.stream(ids).distinct().sorted().forEach(id -> valid.existe(repos.bloquear(id)));
  }

  @Transactional
  public Reuniao criar(ReuniaoRequest r) {
    var v = convert(0, r);
    locks(v.getReuniaoRepoId());
    long id = dao.criar(v);
    repoService.sincronizar(v.getReuniaoRepoId());
    return buscar(id);
  }

  @Transactional
  public Reuniao atualizar(long id, ReuniaoRequest r) {
    valid.id(id);
    var old = valid.existe(dao.bloquear(id));
    var v = convert(id, r);
    if (!analises.porReuniao(id).isEmpty()
        && (!old.getTranscricao().equals(v.getTranscricao())
            || old.getClienteId() != v.getClienteId()
            || old.getVendedorId() != v.getVendedorId()
            || !old.getData().equals(v.getData())))
      throw new ApiException(
          409, "Exclua as análises antes de alterar os dados analisados da reunião.");
    locks(old.getReuniaoRepoId(), v.getReuniaoRepoId());
    dao.atualizar(v);
    repoService.sincronizar(old.getReuniaoRepoId());
    repoService.sincronizar(v.getReuniaoRepoId());
    return buscar(id);
  }

  @Transactional
  public void deletar(long id) {
    valid.id(id);
    var v = valid.existe(dao.bloquear(id));
    locks(v.getReuniaoRepoId());
    dao.excluir(id);
    repoService.sincronizar(v.getReuniaoRepoId());
  }
}
