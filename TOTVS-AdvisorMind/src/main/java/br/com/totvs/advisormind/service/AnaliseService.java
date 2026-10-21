package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.AnaliseIADao;
import br.com.totvs.advisormind.dao.ReuniaoDao;
import br.com.totvs.advisormind.dto.AnalisePreview;
import br.com.totvs.advisormind.dto.OpenAICompletion;
import br.com.totvs.advisormind.entities.AnaliseIA;
import br.com.totvs.advisormind.entities.Reuniao;
import br.com.totvs.advisormind.entities.TipoAnalise;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.exception.PersistenciaException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnaliseService {
  private final ConexaoFactory conexoes;
  private final ValidationService valid;
  private final ReuniaoService reunioes;
  private final SmartBriefingService briefing;
  private final ConversationalAnalyticsService analytics;
  private final VoCTrackerService voc;

  public AnaliseService(ConexaoFactory conexoes, ValidationService valid, ReuniaoService reunioes, SmartBriefingService briefing, ConversationalAnalyticsService analytics, VoCTrackerService voc) {
    this.conexoes = conexoes;
    this.valid = valid;
    this.reunioes = reunioes;
    this.briefing = briefing;
    this.analytics = analytics;
    this.voc = voc;
  }

  private OpenAICompletion executar(Reuniao reuniao, TipoAnalise tipo, boolean compacto) {
    if (tipo == TipoAnalise.SMART_BRIEFING) return briefing.executarComMetadados(reuniao.getTranscricao(), compacto);
    if (tipo == TipoAnalise.CONVERSATIONAL_ANALYTICS) return analytics.executarComMetadados(reuniao.getTranscricao(), compacto);
    return voc.executarComMetadados(reuniao.getTranscricao(), compacto);
  }

  public AnalisePreview previa(long id, String rota) {
    TipoAnalise tipo = TipoAnalise.porRota(rota);
    Reuniao reuniao = reunioes.buscar(id);
    OpenAICompletion resposta = executar(reuniao, tipo, false);
    return new AnalisePreview(id, rota, resposta.getResultado(), resposta.getModelo(), resposta.getTokensUtilizados(), false);
  }

  public AnaliseIA criar(long id, String rota) {
    TipoAnalise tipo = TipoAnalise.porRota(rota);
    Reuniao original = reunioes.buscar(id);
    try (Connection conexao = conexoes.conexao()) {
      if (new AnaliseIADao(conexao).existeTipo(id, tipo.getCodigo()))
        throw new ApiException(409, "Já existe análise deste tipo para a reunião.");
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
    // A requisição HTTP ocorre antes de abrir a transação de gravação.
    OpenAICompletion resposta = executar(original, tipo, true);
    if (resposta.getResultado().isBlank() || resposta.getResultado().length() > 250
        || resposta.getResultado().getBytes(StandardCharsets.UTF_8).length > 250)
      throw new ApiException(502, "Resultado excede o limite de 250 caracteres/bytes UTF-8; nada foi truncado ou persistido. Use a prévia completa.");
    if (resposta.getModelo().length() > 75 || resposta.getModelo().getBytes(StandardCharsets.UTF_8).length > 75)
      throw new ApiException(502, "Modelo informado pelo provedor excede o limite do banco.");
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        Reuniao atual = valid.existe(new ReuniaoDao(conexao).bloquear(id));
        if (!original.getTranscricao().equals(atual.getTranscricao())
            || original.getClienteId() != atual.getClienteId()
            || original.getVendedorId() != atual.getVendedorId()
            || !original.getData().equals(atual.getData()))
          throw new ApiException(409, "Reunião alterada durante a análise; resultado não persistido.");
        AnaliseIADao dao = new AnaliseIADao(conexao);
        if (dao.existeTipo(id, tipo.getCodigo()))
          throw new ApiException(409, "Já existe análise deste tipo para a reunião.");
        AnaliseIA valor = new AnaliseIA(0, tipo.getCodigo(), resposta.getResultado(), resposta.getModelo(),
            LocalDateTime.now(ZoneOffset.UTC).withNano(0), resposta.getTokensUtilizados(), id);
        AnaliseIA resultado = valid.existe(dao.buscar(dao.criar(valor)));
        conexao.commit();
        return resultado;
      } catch (SQLException | RuntimeException e) {
        try { conexao.rollback(); } catch (SQLException falhaRollback) { e.addSuppressed(falhaRollback); }
        throw e;
      }
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  AnaliseIA buscar(Connection conexao, long id) throws SQLException {
    valid.id(id);
    return valid.existe(new AnaliseIADao(conexao).buscar(id));
  }

  public List<AnaliseIA> listar() {
    try (Connection conexao = conexoes.conexao()) {
      return new AnaliseIADao(conexao).listar();
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public AnaliseIA buscar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      return buscar(conexao, id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }

  public List<AnaliseIA> porReuniao(long id) {
    try (Connection conexao = conexoes.conexao()) {
      reunioes.buscar(conexao, id);
      return new AnaliseIADao(conexao).porReuniao(id);
    } catch (SQLException | ClassNotFoundException e) {
      throw new PersistenciaException(e);
    }
  }
  public void deletar(long id) {
    try (Connection conexao = conexoes.conexao()) {
      conexao.setAutoCommit(false);
      try {
        buscar(conexao, id);
        new AnaliseIADao(conexao).excluir(id);
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
