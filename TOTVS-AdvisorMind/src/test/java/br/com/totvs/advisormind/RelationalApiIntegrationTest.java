package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelationalApiIntegrationTest {
  @Autowired MockMvc api;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  static HttpServer server;
  static AtomicReference<String> answer = new AtomicReference<>();
  static AtomicInteger calls = new AtomicInteger();

  static {
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext(
          "/chat",
          e -> {
            calls.incrementAndGet();
            e.getRequestBody().readAllBytes();
            byte[] b = answer.get().getBytes(StandardCharsets.UTF_8);
            e.sendResponseHeaders(200, b.length);
            e.getResponseBody().write(b);
            e.close();
          });
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void config(DynamicPropertyRegistry r) {
    r.add("advisormind.openai-key", () -> "test-only");
    r.add(
        "advisormind.openai-endpoint",
        () -> "http://127.0.0.1:" + server.getAddress().getPort() + "/chat");
  }

  @AfterAll
  static void stop() {
    server.stop(0);
  }

  long vr, rr, c, v;

  String completion(String text) throws Exception {
    return json.writeValueAsString(
        Map.of(
            "model",
            "controlled-test-model",
            "usage",
            Map.of("total_tokens", 123),
            "choices",
            List.of(Map.of("finish_reason", "stop", "message", Map.of("content", text)))));
  }

  JsonNode call(String method, String path, Object body, int code) throws Exception {
    var req =
        request(org.springframework.http.HttpMethod.valueOf(method), path)
            .contentType("application/json");
    if (body != null) req.content(json.writeValueAsString(body));
    String out =
        api.perform(req)
            .andExpect(status().is(code))
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
    return out.isBlank() ? json.nullNode() : json.readTree(out);
  }

  Map<String, Object> cliente(String name) {
    return new LinkedHashMap<>(
        Map.of("nome", name, "empresa", "Empresa ME", "dataNascimento", "1990-03-15"));
  }

  Map<String, Object> vendedor(String name, long repo) {
    return new LinkedHashMap<>(
        Map.of(
            "nome",
            name,
            "cargo",
            "Consultor",
            "setor",
            "Tecnologia",
            "totalReunioes",
            10,
            "vendedorRepoId",
            repo));
  }

  Map<String, Object> reuniao() {
    return new LinkedHashMap<>(
        Map.of(
            "transcricao",
            "Cliente: \"Gostei\", vamos avançar.\nVendedor: proposta! ação 🚀",
            "data",
            "2026-09-01T10:00:00",
            "status",
            "AGENDADA",
            "clienteId",
            c,
            "vendedorId",
            v,
            "reuniaoRepoId",
            rr));
  }

  long createMeeting() throws Exception {
    return call("POST", "/api/reunioes", reuniao(), 201).get("id").asLong();
  }

  @BeforeEach
  void prepare() throws Exception {
    for (String table :
        new String[] {
          "ANALISE_IA", "REUNIAO", "VENDEDOR", "CLIENTE", "REUNIAO_REPO", "VENDEDOR_REPO"
        }) jdbc.update("DELETE FROM " + table);
    answer.set(completion("Cliente interessado; combinar demonstração."));
    calls.set(0);
    vr = call("POST", "/api/vendedor-repos", Map.of("filtro", "Todos"), 201).get("id").asLong();
    rr = call("POST", "/api/reuniao-repos", Map.of(), 201).get("id").asLong();
    c = call("POST", "/api/clientes", cliente("João"), 201).get("id").asLong();
    v = call("POST", "/api/vendedores", vendedor("Ana", vr), 201).get("id").asLong();
  }

  @Test
  void generatedIdentitiesAndSellerCrud() throws Exception {
    assertTrue(v > 0);
    var seller = call("GET", "/api/vendedores/" + v, null, 200);
    assertFalse(seller.has("ticketMedioNegociado"));
    assertEquals(vr, seller.get("vendedorRepoId").asLong());
    assertEquals(1, call("GET", "/api/vendedores", null, 200).size());
    var updated = vendedor("Beatriz", vr);
    call("PUT", "/api/vendedores/" + v, updated, 200);
    assertEquals("Beatriz", call("GET", "/api/vendedores/" + v, null, 200).get("nome").asText());
    assertEquals(
        11,
        call("POST", "/api/vendedores/" + v + "/registrar-reuniao", null, 200)
            .get("totalReunioes")
            .asInt());
    call("DELETE", "/api/vendedores/" + v, null, 204);
    call("GET", "/api/vendedores/" + v, null, 404);
    call("DELETE", "/api/vendedores/" + v, null, 404);
    call("PUT", "/api/vendedores/" + v, updated, 404);
    assertEquals(
        0, call("GET", "/api/vendedor-repos/" + vr, null, 200).get("totalVendedores").asInt());
  }

  @Test
  void sellerIndicatorsRequireExplicitTicket() throws Exception {
    assertTrue(
        call("GET", "/api/vendedores/" + v + "/indicadores", null, 200)
            .get("comissaoEstimada")
            .isNull());
    assertEquals(
        525,
        call("GET", "/api/vendedores/" + v + "/indicadores?ticketMedio=1500", null, 200)
            .get("comissaoEstimada")
            .asDouble());
    call("GET", "/api/vendedores/" + v + "/indicadores?ticketMedio=-1", null, 400);
    call("GET", "/api/vendedores/" + v + "/indicadores?ticketMedio=1.001", null, 400);
  }

  @Test
  void sellerMoveSynchronizesBothRepos() throws Exception {
    long other = call("POST", "/api/vendedor-repos", Map.of(), 201).get("id").asLong();
    call("PUT", "/api/vendedores/" + v, vendedor("Ana", other), 200);
    assertEquals(
        0, call("GET", "/api/vendedor-repos/" + vr, null, 200).get("totalVendedores").asInt());
    assertEquals(
        1, call("GET", "/api/vendedor-repos/" + other, null, 200).get("totalVendedores").asInt());
  }

  @Test
  void clientCrudAndUniqueness() throws Exception {
    call("POST", "/api/clientes", cliente("João"), 409);
    var body = cliente("Maria");
    call("PUT", "/api/clientes/" + c, body, 200);
    assertEquals("Maria", call("GET", "/api/clientes/" + c, null, 200).get("nome").asText());
    call("GET", "/api/clientes", null, 200);
    call("DELETE", "/api/clientes/" + c, null, 204);
    call("GET", "/api/clientes/" + c, null, 404);
    call("PUT", "/api/clientes/" + c, body, 404);
  }

  @Test
  void repositoriesCrudAndServerOwnedCounters() throws Exception {
    call("GET", "/api/vendedor-repos", null, 200);
    call("GET", "/api/reuniao-repos", null, 200);
    var x = call("PUT", "/api/vendedor-repos/" + vr, Map.of("filtro", "Nova descrição"), 200);
    assertEquals(1, x.get("totalVendedores").asInt());
    call("PUT", "/api/reuniao-repos/" + rr, Map.of(), 200);
    call("POST", "/api/vendedor-repos", Map.of("totalVendedores", 8), 400);
    call("POST", "/api/reuniao-repos", Map.of("arquivoJson", "[]"), 400);
    call("DELETE", "/api/vendedor-repos/" + vr, null, 409);
    call("DELETE", "/api/reuniao-repos/" + rr, null, 204);
    call("GET", "/api/reuniao-repos/" + rr, null, 404);
    call("DELETE", "/api/vendedores/" + v, null, 204);
    call("DELETE", "/api/vendedor-repos/" + vr, null, 204);
    call("GET", "/api/vendedor-repos/" + vr, null, 404);
  }

  @Test
  void meetingCrudAndJsonDerivedIndex() throws Exception {
    long id = createMeeting();
    var body = reuniao();
    var row = call("GET", "/api/reunioes/" + id, null, 200);
    assertEquals(body.get("transcricao"), row.get("transcricao").asText());
    assertEquals(c, row.get("clienteId").asLong());
    assertFalse(row.has("cliente"));
    var repo = call("GET", "/api/reuniao-repos/" + rr, null, 200);
    assertEquals(1, repo.get("totalReunioes").asInt());
    assertEquals(
        id, json.readTree(repo.get("arquivoJson").asText()).get("reunioes").get(0).asLong());
    body.put("status", "ATENDIDA");
    call("PUT", "/api/reunioes/" + id, body, 200);
    assertEquals("ATENDIDA", call("GET", "/api/reunioes/" + id, null, 200).get("status").asText());
    call("GET", "/api/reunioes", null, 200);
    call("GET", "/api/reunioes/estatisticas", null, 200);
    assertEquals(
        "QUENTE",
        call("GET", "/api/reunioes/" + id + "/indicadores", null, 200)
            .get("temperaturaComercial")
            .asText());
    call("DELETE", "/api/reunioes/" + id, null, 204);
    call("GET", "/api/reunioes/" + id, null, 404);
    call("PUT", "/api/reunioes/" + id, body, 404);
    call("DELETE", "/api/reunioes/" + id, null, 404);
    assertEquals(
        0, call("GET", "/api/reuniao-repos/" + rr, null, 200).get("totalReunioes").asInt());
  }

  @Test
  void meetingMoveSynchronizesRepos() throws Exception {
    long id = createMeeting(),
        other = call("POST", "/api/reuniao-repos", Map.of(), 201).get("id").asLong();
    var b = reuniao();
    b.put("reuniaoRepoId", other);
    call("PUT", "/api/reunioes/" + id, b, 200);
    assertEquals(
        0, call("GET", "/api/reuniao-repos/" + rr, null, 200).get("totalReunioes").asInt());
    assertEquals(
        1, call("GET", "/api/reuniao-repos/" + other, null, 200).get("totalReunioes").asInt());
  }

  @Test
  void foreignKeysAndDependentDeletes() throws Exception {
    var sv = vendedor("Outra", 999999);
    call("POST", "/api/vendedores", sv, 404);
    for (String field : new String[] {"clienteId", "vendedorId", "reuniaoRepoId"}) {
      var r = reuniao();
      r.put(field, 999999);
      call("POST", "/api/reunioes", r, 404);
    }
    createMeeting();
    for (String path :
        new String[] {
          "/api/clientes/" + c,
          "/api/vendedores/" + v,
          "/api/reuniao-repos/" + rr,
          "/api/vendedor-repos/" + vr
        }) call("DELETE", path, null, 409);
  }

  @Test
  void uniqueMeetingIncludesTimeToTheSecond() throws Exception {
    createMeeting();
    call("POST", "/api/reunioes", reuniao(), 409);
    var r = reuniao();
    r.put("data", "2026-09-01T10:00:01");
    call("POST", "/api/reunioes", r, 201);
    r.put("data", "2026-09-01T10:00:02.001");
    call("POST", "/api/reunioes", r, 400);
    assertEquals(2, call("GET", "/api/reunioes", null, 200).size());
  }

  @Test
  void transcriptBoundariesAndByteSemantics() throws Exception {
    var r = reuniao();
    r.put("transcricao", "a".repeat(4000));
    long id = call("POST", "/api/reunioes", r, 201).get("id").asLong();
    assertEquals(
        4000, call("GET", "/api/reunioes/" + id, null, 200).get("transcricao").asText().length());
    r.put("transcricao", "a".repeat(4001));
    call("POST", "/api/reunioes", r, 400);
    r.put("transcricao", "é".repeat(2001));
    call("POST", "/api/reunioes", r, 400);
    r.put("transcricao", " ");
    call("POST", "/api/reunioes", r, 400);
  }

  @Test
  void requiredFieldsTypesAndDates() throws Exception {
    for (String path : new String[] {"clientes", "vendedores", "reunioes"})
      call("POST", "/api/" + path, Map.of(), 400);
    for (String field :
        new String[] {"data", "status", "clienteId", "vendedorId", "reuniaoRepoId"}) {
      var r = reuniao();
      r.remove(field);
      call("POST", "/api/reunioes", r, 400);
    }
    for (String date : new String[] {"2026-02-30T10:00:00", "2026-09-01", "2026-09-01T10:00:00Z"}) {
      var r = reuniao();
      r.put("data", date);
      call("POST", "/api/reunioes", r, 400);
    }
    var r = reuniao();
    r.put("status", "INVALIDO");
    call("POST", "/api/reunioes", r, 400);
    r.put("status", "AGENDADA");
    r.put("vendedorId", 0);
    call("POST", "/api/reunioes", r, 400);
    for (String date : new String[] {"2100-01-01", "2026-02-30", "0000-01-01"}) {
      var b = cliente("Outro");
      b.put("dataNascimento", date);
      call("POST", "/api/clientes", b, 400);
    }
    var sv = vendedor("Nova", vr);
    sv.put("totalReunioes", -1);
    call("POST", "/api/vendedores", sv, 400);
    sv.put("totalReunioes", 1.5);
    call("POST", "/api/vendedores", sv, 400);
    sv.put("totalReunioes", 2147483648L);
    call("POST", "/api/vendedores", sv, 400);
    call("GET", "/api/clientes/0", null, 400);
    call("GET", "/api/clientes/abc", null, 400);
    call("GET", "/api/nao-existe", null, 404);
    call("PATCH", "/api/vendedores/" + v, Map.of(), 405);
    api.perform(post("/api/clientes").contentType("text/plain").content("x"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void stringLimitsAndDuplicates() throws Exception {
    var cl = cliente("a".repeat(101));
    call("POST", "/api/clientes", cl, 400);
    cl = cliente("Outro");
    cl.put("empresa", "é".repeat(51));
    call("POST", "/api/clientes", cl, 400);
    for (String field : new String[] {"nome", "cargo", "setor"}) {
      var b = vendedor("Outra", vr);
      b.put(field, "x".repeat(field.equals("nome") ? 101 : 51));
      call("POST", "/api/vendedores", b, 400);
    }
    call("POST", "/api/vendedores", vendedor("Ana", vr), 409);
    call("POST", "/api/vendedor-repos", Map.of("filtro", "x".repeat(101)), 400);
    var b = vendedor("Outra", vr);
    b.put("id", 123);
    call("POST", "/api/vendedores", b, 400);
    b.remove("id");
    b.put("ticketMedioNegociado", 1000);
    call("POST", "/api/vendedores", b, 400);
  }

  @Test
  void rollbackAfterMeetingInsertWhenCacheUpdateFails() throws Exception {
    jdbc.execute("ALTER TABLE REUNIAO_REPO ADD CONSTRAINT TEST_ROLLBACK CHECK(TOTAL_REUNIOES=0)");
    try {
      call("POST", "/api/reunioes", reuniao(), 409);
      assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM REUNIAO", Integer.class));
      assertEquals(
          0, call("GET", "/api/reuniao-repos/" + rr, null, 200).get("totalReunioes").asInt());
    } finally {
      jdbc.execute("ALTER TABLE REUNIAO_REPO DROP CONSTRAINT TEST_ROLLBACK");
    }
  }

  @Test
  void rollbackAfterSellerInsertWhenCounterUpdateFails() throws Exception {
    long id = call("POST", "/api/vendedor-repos", Map.of(), 201).get("id").asLong();
    jdbc.execute(
        "ALTER TABLE VENDEDOR_REPO ADD CONSTRAINT TEST_SELLER_ROLLBACK CHECK(ID_VEND_REPO<>"
            + id
            + " OR TOTAL_VENDEDORES=0)");
    try {
      call("POST", "/api/vendedores", vendedor("Rollback", id), 409);
      assertEquals(
          0,
          jdbc.queryForObject(
              "SELECT COUNT(*) FROM VENDEDOR WHERE NOME='Rollback'", Integer.class));
    } finally {
      jdbc.execute("ALTER TABLE VENDEDOR_REPO DROP CONSTRAINT TEST_SELLER_ROLLBACK");
    }
  }

  @Test
  void clobIndexAboveFourThousandCharacters() throws Exception {
    var args = new ArrayList<Object[]>();
    for (int i = 0; i < 1200; i++)
      args.add(
          new Object[] {
            "Fixture CLOB",
            java.sql.Timestamp.valueOf(LocalDateTime.of(2026, 9, 1, 10, 0).plusSeconds(i)),
            "AGENDADA",
            c,
            v,
            rr
          });
    jdbc.batchUpdate(
        "INSERT INTO"
            + " REUNIAO(TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO)"
            + " VALUES(?,?,?,?,?,?)",
        args);
    var repo = call("PUT", "/api/reuniao-repos/" + rr, Map.of(), 200);
    assertEquals(1200, repo.get("totalReunioes").asInt());
    assertTrue(repo.get("arquivoJson").asText().length() > 4000);
    assertEquals(1200, json.readTree(repo.get("arquivoJson").asText()).get("reunioes").size());
  }

  @Test
  void aiPersistsRealMetadataForAllThreeTypesAndBlocksDuplicate() throws Exception {
    long id = createMeeting();
    int type = 0;
    for (String route :
        new String[] {"smart-briefing", "conversational-analytics", "voc-tracker"}) {
      var a = call("POST", "/api/reunioes/" + id + "/analises/" + route, null, 201);
      assertEquals(++type, a.get("tipo").asInt());
      assertEquals(123, a.get("tokensUtilizados").asInt());
      assertEquals("controlled-test-model", a.get("modeloIa").asText());
      assertTrue(a.get("id").asLong() > 0);
      call("GET", "/api/analises/" + a.get("id").asLong(), null, 200);
    }
    assertEquals(3, call("GET", "/api/reunioes/" + id + "/analises", null, 200).size());
    assertEquals(3, call("GET", "/api/analises", null, 200).size());
    int n = calls.get();
    call("POST", "/api/reunioes/" + id + "/analises/smart-briefing", null, 409);
    assertEquals(n, calls.get());
  }

  @Test
  void aiLengthLimitNeverTruncates() throws Exception {
    long id = createMeeting();
    answer.set(completion("a".repeat(250)));
    assertEquals(
        250,
        call("POST", "/api/reunioes/" + id + "/analises/smart-briefing", null, 201)
            .get("resultado")
            .asText()
            .length());
    answer.set(completion("a".repeat(251)));
    call("POST", "/api/reunioes/" + id + "/analises/voc-tracker", null, 502);
    answer.set(completion("é".repeat(130)));
    call("POST", "/api/reunioes/" + id + "/analises/conversational-analytics", null, 502);
    assertEquals(1, call("GET", "/api/analises", null, 200).size());
  }

  @Test
  void fullPreviewIsNotStored() throws Exception {
    long id = createMeeting();
    answer.set(completion("Resposta completa. ".repeat(100)));
    var preview =
        call("POST", "/api/reunioes/" + id + "/analises/smart-briefing/previa", null, 200);
    assertFalse(preview.get("persistida").asBoolean());
    assertTrue(preview.get("resultado").asText().length() > 250);
    assertEquals(0, call("GET", "/api/analises", null, 200).size());
  }

  @Test
  void missingTokensOrInvalidResponseCannotPersist() throws Exception {
    long id = createMeeting();
    for (String res :
        new String[] {
          "{}",
          "{\"model\":\"m\",\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"a\"}}]}",
          "{broken"
        }) {
      answer.set(res);
      call("POST", "/api/reunioes/" + id + "/analises/smart-briefing", null, 502);
    }
    assertEquals(0, call("GET", "/api/analises", null, 200).size());
  }

  @Test
  void analysisRelationsDeletesAndStaleTranscriptProtection() throws Exception {
    long id = createMeeting();
    long analysis =
        call("POST", "/api/reunioes/" + id + "/analises/smart-briefing", null, 201)
            .get("id")
            .asLong();
    call("DELETE", "/api/reunioes/" + id, null, 409);
    var b = reuniao();
    b.put("transcricao", "Mudança");
    call("PUT", "/api/reunioes/" + id, b, 409);
    b = reuniao();
    b.put("status", "ATENDIDA");
    call("PUT", "/api/reunioes/" + id, b, 200);
    call("DELETE", "/api/analises/" + analysis, null, 204);
    call("GET", "/api/analises/" + analysis, null, 404);
    call("DELETE", "/api/analises/" + analysis, null, 404);
    call("DELETE", "/api/reunioes/" + id, null, 204);
    call("GET", "/api/reunioes/999999/analises", null, 404);
    call("POST", "/api/reunioes/999999/analises/smart-briefing", null, 404);
    call("POST", "/api/reunioes/" + id + "/analises/invalido", null, 404);
  }

  @Test
  void persistenceFailureDoesNotExposeSql() throws Exception {
    jdbc.execute("ALTER TABLE ANALISE_IA RENAME TO TEST_BACKUP");
    try {
      String text = call("GET", "/api/analises", null, 500).toString();
      assertFalse(text.contains("SELECT"));
      assertFalse(text.contains("TEST_BACKUP"));
      assertFalse(text.contains("stack"));
    } finally {
      jdbc.execute("ALTER TABLE TEST_BACKUP RENAME TO ANALISE_IA");
    }
  }
}
