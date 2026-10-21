package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.exception.ApiException;
import br.com.totvs.advisormind.service.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;

class OpenAIClientTest {
  HttpServer server;
  AtomicReference<String> response = new AtomicReference<>();
  AtomicReference<String> request = new AtomicReference<>();
  int status = 200;
  long delay;

  @BeforeEach
  void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/chat",
        e -> {
          try {
            request.set(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (delay > 0) Thread.sleep(delay);
            byte[] out = response.get().getBytes(StandardCharsets.UTF_8);
            e.sendResponseHeaders(status, out.length);
            e.getResponseBody().write(out);
          } catch (Exception ignored) {
          } finally {
            e.close();
          }
        });
    server.start();
    response.set(
        "{\"model\":\"test-model\",\"usage\":{\"total_tokens\":42},\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"Resumo"
            + " de teste\"}}]}");
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  OpenAIClient client(int timeout) {
    return new OpenAIClient(
        "test-key",
        "test-model",
        "http://127.0.0.1:" + server.getAddress().getPort() + "/chat",
        timeout);
  }

  @Test
  void fullTranscriptAndValidJson() {
    String input = "texto com aspas \" e tab \t \n".repeat(200);
    assertEquals("Resumo de teste", client(5).enviarPrompt(input));
    assertTrue(request.get().length() > 2000);
    assertTrue(request.get().contains("test-model"));
  }

  @Test
  void malformedAndEmptyResponses() {
    for (String value :
        new String[] {
          "not json",
          "{}",
          "{\"choices\":[{\"finish_reason\":\"length\",\"message\":{\"content\":\"incompleto\"}}]}"
        }) {
      response.set(value);
      assertEquals(
          502, assertThrows(ApiException.class, () -> client(5).enviarPrompt("a")).getStatus());
    }
  }

  @Test
  void upstreamErrors() {
    for (int code : new int[] {401, 429, 500, 503}) {
      status = code;
      assertEquals(
          code == 429 || code == 503 ? 503 : 502,
          assertThrows(ApiException.class, () -> client(5).enviarPrompt("a")).getStatus());
    }
  }

  @Test
  void timeout() {
    delay = 1600;
    assertEquals(
        504, assertThrows(ApiException.class, () -> client(1).enviarPrompt("a")).getStatus());
  }

  @Test
  void missingKey() {
    assertEquals(
        503,
        assertThrows(
                ApiException.class,
                () -> new OpenAIClient("", "x", "http://localhost", 1).enviarPrompt("a"))
            .getStatus());
  }

  @Test
  void allModulesAndEmptyInput() {
    var c = client(5);
    assertEquals("Resumo de teste", new SmartBriefingService(c).executar("transcrição"));
    assertEquals("Resumo de teste", new ConversationalAnalyticsService(c).executar("transcrição"));
    assertEquals("Resumo de teste", new VoCTrackerService(c).executar("transcrição"));
    assertEquals(
        400,
        assertThrows(ApiException.class, () -> new SmartBriefingService(c).executar(" "))
            .getStatus());
  }

  @Test
  void metadataComesFromProvider() {
    var c = client(5).enviarComMetadados("a");
    assertEquals("test-model", c.getModelo());
    assertEquals(42, c.getTokensUtilizados());
  }

  @Test
  void negativeOrFractionalTokensAreInvalid() {
    for (String body :
        new String[] {
          "{\"model\":\"m\",\"usage\":{\"total_tokens\":-1},\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"a\"}}]}",
          "{\"model\":\"m\",\"usage\":{\"total_tokens\":1.5},\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"a\"}}]}"
        }) {
      response.set(body);
      assertEquals(
          502,
          assertThrows(ApiException.class, () -> client(5).enviarComMetadados("a")).getStatus());
    }
  }
}
