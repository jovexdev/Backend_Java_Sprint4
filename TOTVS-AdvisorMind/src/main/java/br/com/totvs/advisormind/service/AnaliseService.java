package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.dao.AnaliseIADao;
import br.com.totvs.advisormind.dto.*;
import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.repository.ReuniaoRepository;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AnaliseService {
  private final ReuniaoService reunioes;
  private final ReuniaoRepository reuniaoDao;
  private final AnaliseIADao dao;
  private final SmartBriefingService briefing;
  private final ConversationalAnalyticsService analytics;
  private final VoCTrackerService voc;
  private final TransactionTemplate tx;
  private final ValidationService valid;

  public AnaliseService(
      ReuniaoService reunioes,
      ReuniaoRepository reuniaoDao,
      AnaliseIADao dao,
      SmartBriefingService briefing,
      ConversationalAnalyticsService analytics,
      VoCTrackerService voc,
      TransactionTemplate tx,
      ValidationService valid) {
    this.reunioes = reunioes;
    this.reuniaoDao = reuniaoDao;
    this.dao = dao;
    this.briefing = briefing;
    this.analytics = analytics;
    this.voc = voc;
    this.tx = tx;
    this.valid = valid;
  }

  private OpenAICompletion executar(Reuniao r, TipoAnalise tipo, boolean compact) {
    return switch (tipo) {
      case SMART_BRIEFING -> briefing.executarComMetadados(r.getTranscricao(), compact);
      case CONVERSATIONAL_ANALYTICS -> analytics.executarComMetadados(r.getTranscricao(), compact);
      case VOC_TRACKER -> voc.executarComMetadados(r.getTranscricao(), compact);
    };
  }

  public AnalisePreview previa(long id, String rota) {
    var tipo = TipoAnalise.porRota(rota);
    var r = reunioes.buscar(id);
    var c = executar(r, tipo, false);
    return new AnalisePreview(id, rota, c.resultado(), c.modelo(), c.tokensUtilizados(), false);
  }

  public AnaliseIA criar(long id, String rota) {
    var tipo = TipoAnalise.porRota(rota);
    var original = reunioes.buscar(id);
    if (dao.existeTipo(id, tipo.getCodigo()))
      throw new ApiException(409, "Já existe análise deste tipo para a reunião.");
    var c = executar(original, tipo, true);
    if (c.resultado().isBlank()
        || c.resultado().length() > 250
        || c.resultado().getBytes(StandardCharsets.UTF_8).length > 250)
      throw new ApiException(
          502,
          "Resultado excede o limite de 250 caracteres/bytes UTF-8; nada foi truncado ou"
              + " persistido. Use a prévia completa ou solicite revisão formal do DDL.");
    if (c.modelo().length() > 75 || c.modelo().getBytes(StandardCharsets.UTF_8).length > 75)
      throw new ApiException(502, "Modelo informado pelo provedor excede o limite do banco.");
    return tx.execute(
        status -> {
          var current = valid.existe(reuniaoDao.bloquear(id));
          if (!original.getTranscricao().equals(current.getTranscricao())
              || original.getClienteId() != current.getClienteId()
              || original.getVendedorId() != current.getVendedorId()
              || !original.getData().equals(current.getData()))
            throw new ApiException(
                409, "Reunião alterada durante a análise; resultado não persistido.");
          if (dao.existeTipo(id, tipo.getCodigo()))
            throw new ApiException(409, "Já existe análise deste tipo para a reunião.");
          var v =
              new AnaliseIA(
                  0,
                  tipo.getCodigo(),
                  c.resultado(),
                  c.modelo(),
                  LocalDateTime.now(ZoneOffset.UTC).withNano(0),
                  c.tokensUtilizados(),
                  id);
          return buscar(dao.criar(v));
        });
  }

  public List<AnaliseIA> listar() {
    return dao.listar();
  }

  public AnaliseIA buscar(long id) {
    valid.id(id);
    return valid.existe(dao.buscar(id));
  }

  public List<AnaliseIA> porReuniao(long id) {
    reunioes.buscar(id);
    return dao.porReuniao(id);
  }

  public void deletar(long id) {
    tx.executeWithoutResult(
        s -> {
          buscar(id);
          dao.excluir(id);
        });
  }
}
