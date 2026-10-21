package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.VendedorRepoDao;
import br.com.totvs.advisormind.dto.VendedorRepoRequest;
import br.com.totvs.advisormind.entities.VendedorRepo;
import br.com.totvs.advisormind.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VendedorRepoService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;

  public VendedorRepoService(ConexaoFactory conexoes, ValidationService valid) {
    this.conexoes = conexoes;
    this.valid = valid;
  }

  VendedorRepo buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new VendedorRepoDao(conexao).buscar(id));
  }

  public List<VendedorRepo> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new VendedorRepoDao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public VendedorRepo buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  private void validar(VendedorRepoRequest r) {
    valid.validar(r);
    valid.texto("filtro", r.getFiltro(), 100);
  }

  public VendedorRepo criar(VendedorRepoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        validar(r);
        VendedorRepoDao dao = new VendedorRepoDao(conexao);
        long id = dao.criar(new VendedorRepo(0, 0, r.getFiltro()));
        VendedorRepo resultado = buscar(conexao, id);
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
  public VendedorRepo atualizar(long id, VendedorRepoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.id(id);
        validar(r);
        VendedorRepoDao dao = new VendedorRepoDao(conexao);
        VendedorRepo anterior = valid.existe(dao.bloquear(id));
        dao.atualizar(new VendedorRepo(id, anterior.getTotalVendedores(), r.getFiltro()));
        dao.sincronizar(id);
        VendedorRepo resultado = buscar(conexao, id);
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
        new VendedorRepoDao(conexao).excluir(id);
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
