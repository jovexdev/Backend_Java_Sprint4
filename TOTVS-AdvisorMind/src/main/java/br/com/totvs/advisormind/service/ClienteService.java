package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.ClienteDao;
import br.com.totvs.advisormind.dto.ClienteRequest;
import br.com.totvs.advisormind.entities.Cliente;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;

  public ClienteService(ConexaoFactory conexoes, ValidationService valid) {
    this.conexoes = conexoes;
    this.valid = valid;
  }

  Cliente buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new ClienteDao(conexao).buscar(id));
  }

  public List<Cliente> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new ClienteDao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public Cliente buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  private Cliente converter(long id, ClienteRequest r) {
    valid.validar(r);
    if (r.getDataNascimento().getYear() < 1 || r.getDataNascimento().getYear() > 9999)
      throw new ApiException(400, "Data fora do intervalo suportado (anos 1 a 9999).");
    valid.texto("nome", r.getNome(), 100);
    valid.texto("empresa", r.getEmpresa(), 100);
    return new Cliente(id, r.getNome(), r.getEmpresa(), r.getDataNascimento());
  }

  public Cliente criar(ClienteRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        Cliente valor = converter(0, r);
        ClienteDao dao = new ClienteDao(conexao);
        Cliente resultado = buscar(conexao, dao.criar(valor));
        conexao.commit();
        return resultado;
      } catch (SQLException | RuntimeException e) {
        try {
          conexao.rollback();
        } catch (SQLException falhaRollback) {
          e.addSuppressed(falhaRollback);
        }
        throw e;
      }
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }
  public Cliente atualizar(long id, ClienteRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        buscar(conexao, id);
        new ClienteDao(conexao).atualizar(converter(id, r));
        Cliente resultado = buscar(conexao, id);
        conexao.commit();
        return resultado;
      } catch (SQLException | RuntimeException e) {
        try {
          conexao.rollback();
        } catch (SQLException falhaRollback) {
          e.addSuppressed(falhaRollback);
        }
        throw e;
      }
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }
  public void deletar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        buscar(conexao, id);
        new ClienteDao(conexao).excluir(id);
        conexao.commit();
      } catch (SQLException | RuntimeException e) {
        try {
          conexao.rollback();
        } catch (SQLException falhaRollback) {
          e.addSuppressed(falhaRollback);
        }
        throw e;
      }
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }
}
