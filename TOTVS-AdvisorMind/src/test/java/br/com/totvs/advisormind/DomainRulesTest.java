package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.entities.*;
import java.math.*;
import org.junit.jupiter.api.Test;

class DomainRulesTest {
  @Test
  void legacyRulesPreservedWithExplicitTicket() {
    var c = new Cliente();
    c.setEmpresa("Carmed");
    assertTrue(c.classificarPerfilComercial().startsWith("MÉDIA"));
    c.setEmpresa("Empresa ME");
    assertTrue(c.classificarPerfilComercial().startsWith("PEQUENO"));
    var v = new Vendedor();
    v.setCargo("Consultor");
    v.setTotalReunioes(10);
    assertEquals("JÚNIOR", v.calcularNivelSenioridade());
    assertEquals(new BigDecimal("350.00"), v.calcularComissaoEstimada(new BigDecimal("1000")));
    assertThrows(IllegalStateException.class, v::calcularComissaoEstimada);
    v.registrarReuniao();
    assertEquals(11, v.getTotalReunioes());
  }

  @Test
  void temperatureAndDuration() {
    var r = new Reuniao();
    r.setTranscricao("caro problema cancelar");
    assertEquals("FRIO", r.calcularTemperaturaComercial());
    r.setTranscricao("palavra ".repeat(131));
    assertEquals(2, r.estimarDuracaoMinutos());
    r.setTranscricao("");
    assertEquals(0, r.estimarDuracaoMinutos());
  }

  @Test
  void typeMapping() {
    assertEquals(1, TipoAnalise.porRota("smart-briefing").getCodigo());
    assertEquals(2, TipoAnalise.porRota("conversational-analytics").getCodigo());
    assertEquals(3, TipoAnalise.porRota("voc-tracker").getCodigo());
  }
}
