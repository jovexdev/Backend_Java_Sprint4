package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.main.AdvisorMindApplication;
import br.com.totvs.advisormind.service.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

/** API completa com JDBC controlado: não há banco alternativo ou conexão Oracle real. */
@SpringBootTest(classes = AdvisorMindApplication.class, properties = "advisormind.openai-key=")
@AutoConfigureMockMvc
@Import(ApiContratoTest.ConfiguracaoTeste.class)
class ApiContratoTest {
  @TestConfiguration
  static class ConfiguracaoTeste {
    @Bean @Primary
    ConexaoFactory conexaoTeste() { return mock(ConexaoFactory.class); }
  }

  @Autowired MockMvc api;
  @Autowired ConexaoFactory factory;
  @Autowired ApplicationContext context;
  List<JdbcFixture> conexoes;
  String falharEm;

  @BeforeEach
  void preparar() throws Exception {
    reset(factory);
    conexoes = new ArrayList<>();
    falharEm = null;
    when(factory.conexao()).thenAnswer(call -> {
      JdbcFixture fixture = new JdbcFixture();
      fixture.falharEm = falharEm;
      conexoes.add(fixture);
      return fixture.connection;
    });
  }

  @Test
  void novaClassePrincipalEncontraTodosOsPacotes() {
    assertNotNull(context.getBean(ReuniaoService.class));
    assertNotNull(context.getBean(AnaliseService.class));
    assertNotNull(context.getBean(br.com.totvs.advisormind.repository.ReuniaoRepository.class));
    verifyNoInteractions(factory);
  }

  @Test
  void seisRecursosMantemCamposERelacionamentos() throws Exception {
    api.perform(get("/api/clientes/11")).andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(11)).andExpect(jsonPath("$.dataNascimento").value("1990-01-01"));
    api.perform(get("/api/vendedores/22")).andExpect(status().isOk())
        .andExpect(jsonPath("$.vendedorRepoId").value(44));
    api.perform(get("/api/reunioes/99")).andExpect(status().isOk())
        .andExpect(jsonPath("$.clienteId").value(11)).andExpect(jsonPath("$.vendedorId").value(22))
        .andExpect(jsonPath("$.reuniaoRepoId").value(33)).andExpect(jsonPath("$.data").value("2026-09-01T10:00:00"));
    api.perform(get("/api/vendedor-repos/44")).andExpect(status().isOk())
        .andExpect(jsonPath("$.totalVendedores").value(1));
    api.perform(get("/api/reuniao-repos/33")).andExpect(status().isOk())
        .andExpect(jsonPath("$.arquivoJson").value("{\"reunioes\":[99]}"));
    api.perform(get("/api/analises/99")).andExpect(status().isOk())
        .andExpect(jsonPath("$.tokensUtilizados").value(42)).andExpect(jsonPath("$.modeloIa").value("fixture"));
    for (JdbcFixture f : conexoes) verify(f.connection).close();
  }

  @Test
  void classesComunsMantemEntradaESaidaDosCadastros() throws Exception {
    api.perform(post("/api/clientes").contentType("application/json")
        .content("{\"nome\":\"Ana\",\"empresa\":\"Empresa ME\",\"dataNascimento\":\"1990-01-01\"}"))
        .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/clientes/99"));
    api.perform(post("/api/vendedores").contentType("application/json")
        .content("{\"nome\":\"Ana\",\"cargo\":\"Consultor\",\"setor\":\"ERP\",\"totalReunioes\":10,\"vendedorRepoId\":44}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(99));
    api.perform(post("/api/reunioes").contentType("application/json")
        .content("{\"transcricao\":\"Texto da reunião\",\"data\":\"2026-09-01T10:00:00\",\"status\":\"AGENDADA\",\"clienteId\":11,\"vendedorId\":22,\"reuniaoRepoId\":33}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(99));
    api.perform(post("/api/vendedor-repos").contentType("application/json").content("{}"))
        .andExpect(status().isCreated());
    api.perform(post("/api/reuniao-repos").contentType("application/json").content("{}"))
        .andExpect(status().isCreated());
  }

  @Test
  void validacaoDeTiposDatasECamposContinuaEstrita() throws Exception {
    for (String corpo : new String[] {
        "{}",
        "{\"nome\":\"Ana\",\"empresa\":\"ME\",\"dataNascimento\":\"2026-02-30\"}",
        "{\"nome\":\"Ana\",\"empresa\":\"ME\",\"dataNascimento\":\"1990-01-01\",\"id\":99}",
        "{\"nome\":{},\"empresa\":\"ME\",\"dataNascimento\":\"1990-01-01\"}"
    }) api.perform(post("/api/clientes").contentType("application/json").content(corpo)).andExpect(status().isBadRequest());
    api.perform(post("/api/vendedores").contentType("application/json")
        .content("{\"nome\":\"Ana\",\"cargo\":\"Consultor\",\"setor\":\"ERP\",\"totalReunioes\":1.5,\"vendedorRepoId\":44}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(factory);
  }

  @Test
  void conflitosSqlContinuam409SemExporSqlOuCredenciais() throws Exception {
    falharEm = "INSERT INTO CLIENTE";
    String saida = api.perform(post("/api/clientes").contentType("application/json")
        .content("{\"nome\":\"Ana\",\"empresa\":\"ME\",\"dataNascimento\":\"1990-01-01\"}"))
        .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();
    assertFalse(saida.contains("INSERT")); assertFalse(saida.contains("fixture"));
    verify(conexoes.get(0).connection).rollback();
  }

  @Test
  void faltaDeChaveMantem503NosTresModulos() throws Exception {
    for (String tipo : new String[] {"smart-briefing", "conversational-analytics", "voc-tracker"}) {
      api.perform(post("/api/reunioes/99/analises/" + tipo)).andExpect(status().isServiceUnavailable());
    }
    for (JdbcFixture f : conexoes) {
      assertFalse(f.sqlExecutado.stream().anyMatch(sql -> sql.startsWith("INSERT")));
    }
  }

  @Test
  void rotasDeListagemAtualizacaoEExclusaoPermanecem() throws Exception {
    for (String tipo : new String[] {"clientes", "vendedores", "reunioes", "vendedor-repos", "reuniao-repos", "analises"})
      api.perform(get("/api/" + tipo)).andExpect(status().isOk());
    api.perform(put("/api/clientes/11").contentType("application/json")
        .content("{\"nome\":\"Ana\",\"empresa\":\"ME\",\"dataNascimento\":\"1990-01-01\"}"))
        .andExpect(status().isOk());
    api.perform(delete("/api/clientes/11")).andExpect(status().isNoContent());
    api.perform(delete("/api/analises/99")).andExpect(status().isNoContent());
    api.perform(get("/api/nao-existe")).andExpect(status().isNotFound());
    api.perform(patch("/api/clientes/11")).andExpect(status().isMethodNotAllowed());
  }
}
