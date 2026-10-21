package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.ClienteDao;
import br.com.totvs.advisormind.dto.ClienteRequest;
import br.com.totvs.advisormind.entities.Cliente;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
  private final ClienteDao dao;
  private final ValidationService valid;

  public ClienteService(ClienteDao dao, ValidationService valid) {
    this.dao = dao;
    this.valid = valid;
  }

  public List<Cliente> listar() {
    return dao.listar();
  }

  public Cliente buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  private Cliente convert(long id, ClienteRequest r) {
    valid.validar(r);
    if (r.dataNascimento().getYear() < 1 || r.dataNascimento().getYear() > 9999)
      throw new br.com.totvs.advisormind.exception.ApiException(
          400, "Data fora do intervalo suportado (anos 1 a 9999).");
    valid.texto("nome", r.nome(), 100);
    valid.texto("empresa", r.empresa(), 100);
    return new Cliente(id, r.nome(), r.empresa(), r.dataNascimento());
  }

  @Transactional
  public Cliente criar(ClienteRequest r) {
    var v = convert(0, r);
    return buscar(dao.criar(v));
  }

  @Transactional
  public Cliente atualizar(long id, ClienteRequest r) {
    buscar(id);
    dao.atualizar(convert(id, r));
    return buscar(id);
  }

  @Transactional
  public void deletar(long id) {
    buscar(id);
    dao.excluir(id);
  }
}
