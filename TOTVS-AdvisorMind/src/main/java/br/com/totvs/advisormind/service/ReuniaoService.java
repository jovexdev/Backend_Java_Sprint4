package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.AnaliseIADao;
import br.com.totvs.advisormind.dao.ReuniaoDao;
import br.com.totvs.advisormind.dao.ReuniaoRepoDao;
import br.com.totvs.advisormind.dto.ReuniaoRequest;
import br.com.totvs.advisormind.entities.Reuniao;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.exception.PersistenciaException;
import br.com.totvs.advisormind.repository.ReuniaoRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReuniaoService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;
  private final ClienteService clientes;
  private final VendedorService vendedores;
  private final ReuniaoRepository repository;

  public ReuniaoService(ConexaoFactory conexoes, ValidationService valid, ClienteService clientes, VendedorService vendedores, ReuniaoRepository repository) {
    this.conexoes = conexoes;
    this.valid = valid;
    this.clientes = clientes;
    this.vendedores = vendedores;
    this.repository = repository;
  }

  Reuniao buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new ReuniaoDao(conexao).buscar(id));
  }

  public List<Reuniao> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new ReuniaoDao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public Reuniao buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  private Reuniao converter(Connection conexao, long id, ReuniaoRequest r) throws SQLException {
    valid.validar(r);
    valid.texto("transcricao", r.getTranscricao(), 4000);
    if (r.getData().getYear() < 1 || r.getData().getYear() > 9999)
      throw new ApiException(400, "Data fora do intervalo suportado (anos 1 a 9999).");
    if (r.getData().getNano() != 0)
      throw new ApiException(400, "Oracle DATE usa segundos inteiros; não envie frações de segundo.");
    clientes.buscar(conexao, r.getClienteId());
    vendedores.buscar(conexao, r.getVendedorId());
    return new Reuniao(id, r.getTranscricao(), r.getData(), r.getStatus(), r.getClienteId(), r.getVendedorId(), r.getReuniaoRepoId());
  }

  private void bloquearRepositorios(Connection conexao, long primeiro, long segundo) throws SQLException {
    ReuniaoRepoDao dao = new ReuniaoRepoDao(conexao);
    long menor = Math.min(primeiro, segundo);
    long maior = Math.max(primeiro, segundo);
    valid.existe(dao.bloquear(menor));
    if (menor != maior) valid.existe(dao.bloquear(maior));
  }

  public Reuniao criar(ReuniaoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        Reuniao valor = converter(conexao, 0, r);
        bloquearRepositorios(conexao, valor.getReuniaoRepoId(), valor.getReuniaoRepoId());
        long id = new ReuniaoDao(conexao).criar(valor);
        repository.sincronizar(conexao, valor.getReuniaoRepoId());
        Reuniao resultado = buscar(conexao, id);
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
  public Reuniao atualizar(long id, ReuniaoRequest r) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        valid.id(id);
        ReuniaoDao dao = new ReuniaoDao(conexao);
        Reuniao anterior = valid.existe(dao.bloquear(id));
        Reuniao valor = converter(conexao, id, r);
        if (!new AnaliseIADao(conexao).porReuniao(id).isEmpty()
            && (!anterior.getTranscricao().equals(valor.getTranscricao())
                || anterior.getClienteId() != valor.getClienteId()
                || anterior.getVendedorId() != valor.getVendedorId()
                || !anterior.getData().equals(valor.getData()))) {
          throw new ApiException(409, "Exclua as análises antes de alterar os dados analisados da reunião.");
        }
        bloquearRepositorios(conexao, anterior.getReuniaoRepoId(), valor.getReuniaoRepoId());
        dao.atualizar(valor);
        repository.sincronizar(conexao, anterior.getReuniaoRepoId());
        if (anterior.getReuniaoRepoId() != valor.getReuniaoRepoId()) repository.sincronizar(conexao, valor.getReuniaoRepoId());
        Reuniao resultado = buscar(conexao, id);
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
        ReuniaoDao dao = new ReuniaoDao(conexao);
        Reuniao valor = valid.existe(dao.bloquear(id));
        bloquearRepositorios(conexao, valor.getReuniaoRepoId(), valor.getReuniaoRepoId());
        dao.excluir(id);
        repository.sincronizar(conexao, valor.getReuniaoRepoId());
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
