package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.VendedorDao;
import br.com.totvs.advisormind.dao.VendedorRepoDao;
import br.com.totvs.advisormind.dto.VendedorRequest;
import br.com.totvs.advisormind.entities.Vendedor;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VendedorService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;

  public VendedorService(ConexaoFactory conexoes, ValidationService valid) {
    this.conexoes = conexoes;
    this.valid = valid;
  }

  Vendedor buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new VendedorDao(conexao).buscar(id));
  }

  public List<Vendedor> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new VendedorDao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public Vendedor buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  private Vendedor converter(long id, VendedorRequest r) {
    valid.validar(r);
    valid.texto("nome", r.getNome(), 100);
    valid.texto("cargo", r.getCargo(), 50);
    valid.texto("setor", r.getSetor(), 50);
    return new Vendedor(id, r.getNome(), r.getCargo(), r.getSetor(), r.getTotalReunioes(), r.getVendedorRepoId());
  }

  private void bloquearRepositorios(Connection conexao, long primeiro, long segundo) throws SQLException {
    VendedorRepoDao dao = new VendedorRepoDao(conexao);
    long menor = Math.min(primeiro, segundo);
    long maior = Math.max(primeiro, segundo);
    valid.existe(dao.bloquear(menor));
    if (menor != maior) valid.existe(dao.bloquear(maior));
  }

  public Vendedor criar(VendedorRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        Vendedor valor = converter(0, r);
        bloquearRepositorios(conexao, valor.getVendedorRepoId(), valor.getVendedorRepoId());
        long id = new VendedorDao(conexao).criar(valor);
        new VendedorRepoDao(conexao).sincronizar(valor.getVendedorRepoId());
        Vendedor resultado = buscar(conexao, id);
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
  public Vendedor atualizar(long id, VendedorRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.id(id);
        Vendedor valor = converter(id, r);
        VendedorDao dao = new VendedorDao(conexao);
        Vendedor anterior = valid.existe(dao.bloquear(id));
        bloquearRepositorios(conexao, anterior.getVendedorRepoId(), valor.getVendedorRepoId());
        dao.atualizar(valor);
        VendedorRepoDao repos = new VendedorRepoDao(conexao);
        repos.sincronizar(anterior.getVendedorRepoId());
        if (anterior.getVendedorRepoId() != valor.getVendedorRepoId()) repos.sincronizar(valor.getVendedorRepoId());
        Vendedor resultado = buscar(conexao, id);
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
        valid.id(id);
        VendedorDao dao = new VendedorDao(conexao);
        Vendedor anterior = valid.existe(dao.bloquear(id));
        bloquearRepositorios(conexao, anterior.getVendedorRepoId(), anterior.getVendedorRepoId());
        dao.excluir(id);
        new VendedorRepoDao(conexao).sincronizar(anterior.getVendedorRepoId());
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
  public Vendedor registrarReuniao(long id) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.id(id);
        VendedorDao dao = new VendedorDao(conexao);
        Vendedor valor = valid.existe(dao.bloquear(id));
        if (valor.getTotalReunioes() == Integer.MAX_VALUE) throw new ApiException(409, "Contador atingiu o limite suportado.");
        valor.registrarReuniao();
        dao.atualizar(valor);
        Vendedor resultado = buscar(conexao, id);
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
}
