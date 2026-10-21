package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.ReuniaoRepoDao;
import br.com.totvs.advisormind.dto.ReuniaoRepoRequest;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.exception.PersistenciaException;
import br.com.totvs.advisormind.repository.ReuniaoRepository;
import br.com.totvs.advisormind.util.JsonUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReuniaoRepoService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;
  private final ReuniaoRepository repository;

  public ReuniaoRepoService(ConexaoFactory conexoes, ValidationService valid, ReuniaoRepository repository) {
    this.conexoes = conexoes;
    this.valid = valid;
    this.repository = repository;
  }

  ReuniaoRepo buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new ReuniaoRepoDao(conexao).buscar(id));
  }

  public List<ReuniaoRepo> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new ReuniaoRepoDao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public ReuniaoRepo buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public ReuniaoRepo criar(ReuniaoRepoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.validar(r);
        long id = new ReuniaoRepoDao(conexao).criar(new ReuniaoRepo(0, JsonUtil.indice(new ArrayList<Long>()), 0));
        ReuniaoRepo resultado = buscar(conexao, id);
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
  public ReuniaoRepo atualizar(long id, ReuniaoRepoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.validar(r);
        valid.id(id);
        valid.existe(new ReuniaoRepoDao(conexao).bloquear(id));
        repository.sincronizar(conexao, id);
        ReuniaoRepo resultado = buscar(conexao, id);
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
        new ReuniaoRepoDao(conexao).excluir(id);
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
