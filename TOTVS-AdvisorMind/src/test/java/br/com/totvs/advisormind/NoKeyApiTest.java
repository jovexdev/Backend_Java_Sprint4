package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.totvs.advisormind.dto.*;
import br.com.totvs.advisormind.entities.StatusReuniao;
import br.com.totvs.advisormind.service.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"advisormind.openai-key=", "spring.datasource.url=jdbc:h2:mem:nokey;MODE=Oracle"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoKeyApiTest {
  @Autowired MockMvc api;
  @Autowired VendedorRepoService vr;
  @Autowired ReuniaoRepoService rr;
  @Autowired VendedorService v;
  @Autowired ClienteService c;
  @Autowired ReuniaoService r;
  @Autowired AnaliseService a;

  @Test
  void allModulesReturn503WithoutSaving() throws Exception {
    var vrid = vr.criar(new VendedorRepoRequest(null)).id();
    var rrid = rr.criar(new ReuniaoRepoRequest()).id();
    var vid = v.criar(new VendedorRequest("Ana", "Consultor", "ERP", 0, vrid)).getId();
    var cid = c.criar(new ClienteRequest("João", "ME", LocalDate.of(1990, 1, 1))).getId();
    long id =
        r.criar(
                new ReuniaoRequest(
                    "Gostei da proposta",
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    StatusReuniao.AGENDADA,
                    cid,
                    vid,
                    rrid))
            .getId();
    for (String type : new String[] {"smart-briefing", "conversational-analytics", "voc-tracker"})
      api.perform(post("/api/reunioes/" + id + "/analises/" + type))
          .andExpect(status().isServiceUnavailable());
    assertEquals(0, a.listar().size());
  }
}
