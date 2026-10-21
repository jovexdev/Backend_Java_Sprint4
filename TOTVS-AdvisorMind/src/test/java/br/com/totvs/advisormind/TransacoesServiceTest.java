package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dto.*;
import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.exception.*;
import br.com.totvs.advisormind.repository.ReuniaoRepository;
import br.com.totvs.advisormind.service.*;
import jakarta.validation.*;
import java.sql.*;
import java.time.*;
import org.junit.jupiter.api.*;

class TransacoesServiceTest {
  ValidatorFactory validatorFactory;
  ValidationService valid;
  ConexaoFactory factory;
  JdbcFixture f;
  ClienteService clientes;
  VendedorService vendedores;
  ReuniaoService reunioes;

  @BeforeEach
  void preparar() throws Exception {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    valid = new ValidationService(validatorFactory.getValidator());
    factory = mock(ConexaoFactory.class);
    f = new JdbcFixture();
    when(factory.conexao()).thenReturn(f.connection);
    clientes = new ClienteService(factory, valid);
    vendedores = new VendedorService(factory, valid);
    reunioes = new ReuniaoService(factory, valid, clientes, vendedores, new ReuniaoRepository());
  }

  @AfterEach
  void fechar() { validatorFactory.close(); }

  ReuniaoRequest pedido() {
    return new ReuniaoRequest(f.transcricao, f.data, StatusReuniao.AGENDADA, 11L, 22L, 33L);
  }

  @Test
  void reuniaoEIndiceUsamMesmaConexaoECommitUnico() throws Exception {
    assertEquals(99, reunioes.criar(pedido()).getId());
    verify(factory, times(1)).conexao();
    verify(f.connection).setAutoCommit(false);
    verify(f.connection).commit();
    verify(f.connection, never()).rollback();
    verify(f.connection).close();
    assertTrue(f.sqlExecutado.stream().anyMatch(s -> s.startsWith("INSERT INTO REUNIAO ")));
    assertTrue(f.sqlExecutado.stream().anyMatch(s -> s.startsWith("UPDATE REUNIAO_REPO")));
  }

  @Test
  void falhaNoIndiceReverteReuniaoSemCommit() throws Exception {
    f.falharEm = "UPDATE REUNIAO_REPO";
    assertThrows(PersistenciaException.class, () -> reunioes.criar(pedido()));
    verify(factory, times(1)).conexao();
    verify(f.connection).rollback();
    verify(f.connection, never()).commit();
    verify(f.connection).close();
  }

  @Test
  void falhaNoContadorReverteVendedor() throws Exception {
    f.falharEm = "UPDATE VENDEDOR_REPO";
    assertThrows(PersistenciaException.class, () -> vendedores.criar(new VendedorRequest("Ana", "Consultor", "ERP", 10, 44L)));
    verify(factory, times(1)).conexao();
    verify(f.connection).rollback();
    verify(f.connection, never()).commit();
    verify(f.connection).close();
  }

  @Test
  void erroNoRollbackNaoEscondeErroOriginal() throws Exception {
    f.falharEm = "UPDATE VENDEDOR_REPO";
    doThrow(new SQLException("falha rollback")).when(f.connection).rollback();
    PersistenciaException erro = assertThrows(PersistenciaException.class,
        () -> vendedores.criar(new VendedorRequest("Ana", "Consultor", "ERP", 10, 44L)));
    assertInstanceOf(SQLIntegrityConstraintViolationException.class, erro.getCause());
    assertEquals(1, erro.getCause().getSuppressed().length);
    verify(f.connection).close();
  }

  @Test
  void validacaoDeNegocioTambemReverteTransacao() throws Exception {
    assertEquals(400, assertThrows(ApiException.class,
        () -> vendedores.criar(new VendedorRequest("", "Consultor", "ERP", 10, 44L))).getStatus());
    verify(f.connection).rollback();
    verify(f.connection, never()).commit();
    assertTrue(f.sqlExecutado.isEmpty());
  }

  @Test
  void contadorHistoricoPermaneceAcaoExplicita() throws Exception {
    assertEquals(99, vendedores.registrarReuniao(99).getId());
    PreparedStatement update = f.statements.entrySet().stream()
        .filter(e -> e.getKey().startsWith("UPDATE VENDEDOR SET")).findFirst().get().getValue();
    verify(update).setInt(4, 11);
    verify(f.connection).commit();
  }

  @Test
  void operacaoSomenteLeituraFechaConexaoSemCommit() throws Exception {
    assertEquals(99, clientes.buscar(99).getId());
    verify(f.connection).close();
    verify(f.connection, never()).setAutoCommit(false);
    verify(f.connection, never()).commit();
  }

  AnaliseService analises(JdbcFixture pre, JdbcFixture escrita, SmartBriefingService briefing,
      ConversationalAnalyticsService analytics, VoCTrackerService voc) throws Exception {
    when(factory.conexao()).thenReturn(pre.connection, escrita.connection);
    ReuniaoService leitura = mock(ReuniaoService.class);
    when(leitura.buscar(99)).thenReturn(new Reuniao(99, pre.transcricao, pre.data, StatusReuniao.AGENDADA, 11, 22, 33));
    return new AnaliseService(factory, valid, leitura, briefing, analytics, voc);
  }

  @Test
  void tresModulosMantemMetadadosEHttpForaDaTransacao() throws Exception {
    for (String rota : new String[] {"smart-briefing", "conversational-analytics", "voc-tracker"}) {
      JdbcFixture pre = new JdbcFixture();
      JdbcFixture escrita = new JdbcFixture();
      SmartBriefingService briefing = mock(SmartBriefingService.class);
      ConversationalAnalyticsService analytics = mock(ConversationalAnalyticsService.class);
      VoCTrackerService voc = mock(VoCTrackerService.class);
      org.mockito.stubbing.Answer<OpenAICompletion> resposta = call -> {
        verify(pre.connection).close();
        verify(escrita.connection, never()).setAutoCommit(false);
        return new OpenAICompletion("Resumo", "fixture", 42);
      };
      when(briefing.executarComMetadados(anyString(), eq(true))).thenAnswer(resposta);
      when(analytics.executarComMetadados(anyString(), eq(true))).thenAnswer(resposta);
      when(voc.executarComMetadados(anyString(), eq(true))).thenAnswer(resposta);
      AnaliseService service = analises(pre, escrita, briefing, analytics, voc);
      AnaliseIA salvo = service.criar(99, rota);
      assertEquals(42, salvo.getTokensUtilizados());
      assertEquals("fixture", salvo.getModeloIa());
      verify(escrita.insercao("ANALISE_IA")).setInt(1, TipoAnalise.porRota(rota).getCodigo());
      verify(escrita.insercao("ANALISE_IA")).setString(2, "Resumo");
      verify(escrita.connection).commit();
      verify(escrita.connection).close();
    }
  }

  @Test
  void mudancaDuranteIaDescartaResultadoEReverte() throws Exception {
    JdbcFixture pre = new JdbcFixture();
    JdbcFixture escrita = new JdbcFixture();
    escrita.transcricao = "Transcrição alterada";
    SmartBriefingService briefing = mock(SmartBriefingService.class);
    when(briefing.executarComMetadados(anyString(), eq(true))).thenReturn(new OpenAICompletion("Resumo", "fixture", 42));
    AnaliseService service = analises(pre, escrita, briefing, mock(ConversationalAnalyticsService.class), mock(VoCTrackerService.class));
    assertEquals(409, assertThrows(ApiException.class, () -> service.criar(99, "smart-briefing")).getStatus());
    verify(escrita.connection).rollback();
    verify(escrita.connection, never()).commit();
    assertFalse(escrita.sqlExecutado.stream().anyMatch(s -> s.startsWith("INSERT")));
  }

  @Test
  void respostaLongaNuncaTruncadaNemGravada() throws Exception {
    JdbcFixture pre = new JdbcFixture();
    JdbcFixture escrita = new JdbcFixture();
    SmartBriefingService briefing = mock(SmartBriefingService.class);
    when(briefing.executarComMetadados(anyString(), eq(true))).thenReturn(new OpenAICompletion("a".repeat(251), "fixture", 42));
    AnaliseService service = analises(pre, escrita, briefing, mock(ConversationalAnalyticsService.class), mock(VoCTrackerService.class));
    assertEquals(502, assertThrows(ApiException.class, () -> service.criar(99, "smart-briefing")).getStatus());
    verify(escrita.connection, never()).setAutoCommit(false);
    assertTrue(escrita.sqlExecutado.isEmpty());
  }

  @Test
  void duplicidadeNaoChamaProvedor() throws Exception {
    JdbcFixture pre = new JdbcFixture();
    pre.analiseExiste = true;
    SmartBriefingService briefing = mock(SmartBriefingService.class);
    AnaliseService service = analises(pre, new JdbcFixture(), briefing,
        mock(ConversationalAnalyticsService.class), mock(VoCTrackerService.class));
    assertEquals(409, assertThrows(ApiException.class, () -> service.criar(99, "smart-briefing")).getStatus());
    verifyNoInteractions(briefing);
  }
}
