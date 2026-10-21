package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.entities.*;
import java.io.Reader;
import java.sql.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DaoJdbcTest {
  @Test
  void crudCliente() throws Exception {
    JdbcFixture f = new JdbcFixture();
    ClienteDao dao = new ClienteDao(f.connection);
    Cliente valor = new Cliente(99, "Ana", "Empresa ME", LocalDate.of(1990, 1, 1));
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void crudVendedor() throws Exception {
    JdbcFixture f = new JdbcFixture();
    VendedorDao dao = new VendedorDao(f.connection);
    Vendedor valor = new Vendedor(99, "Ana", "Consultor", "ERP", 10, 44);
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void crudReuniao() throws Exception {
    JdbcFixture f = new JdbcFixture();
    ReuniaoDao dao = new ReuniaoDao(f.connection);
    Reuniao valor = new Reuniao(99, "Texto da reunião", f.data, StatusReuniao.AGENDADA, 11, 22, 33);
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void crudVendedorRepo() throws Exception {
    JdbcFixture f = new JdbcFixture();
    VendedorRepoDao dao = new VendedorRepoDao(f.connection);
    VendedorRepo valor = new VendedorRepo(99, 1, "Todos");
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void crudReuniaoRepo() throws Exception {
    JdbcFixture f = new JdbcFixture();
    ReuniaoRepoDao dao = new ReuniaoRepoDao(f.connection);
    ReuniaoRepo valor = new ReuniaoRepo(99, "{\"reunioes\":[99]}", 10);
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void crudAnaliseIA() throws Exception {
    JdbcFixture f = new JdbcFixture();
    AnaliseIADao dao = new AnaliseIADao(f.connection);
    AnaliseIA valor = new AnaliseIA(99, 1, "Resumo", "fixture", f.data, 42, 99);
    assertEquals(99, dao.criar(valor));
    assertEquals(99, dao.buscar(99).getId());
    assertEquals(99, dao.bloquear(99).getId());
    assertEquals(1, dao.listar().size());
    assertEquals(1, dao.atualizar(valor));
    assertEquals(1, dao.excluir(99));
    dao.close();
    verify(f.connection, never()).commit();
    verify(f.connection, never()).close();
    for (PreparedStatement stmt : f.statements.values()) verify(stmt).close();
    for (ResultSet rs : f.resultados) verify(rs).close();
  }

  @Test
  void parametrosNaoViramComandosSql() throws Exception {
    JdbcFixture f = new JdbcFixture();
    String nome = "x'); DELETE FROM CLIENTE; --";
    new ClienteDao(f.connection).criar(new Cliente(0, nome, "ME", LocalDate.of(1990, 1, 1)));
    PreparedStatement stmt = f.insercao("CLIENTE");
    verify(stmt).setString(1, nome);
    verify(stmt).setDate(3, java.sql.Date.valueOf("1990-01-01"));
    assertFalse(f.sqlExecutado.get(0).contains(nome));
    ArgumentCaptor<String[]> coluna = ArgumentCaptor.forClass(String[].class);
    verify(f.connection).prepareStatement(anyString(), coluna.capture());
    assertArrayEquals(new String[] {"ID_CLIENTE"}, coluna.getValue());
  }

  @Test
  void clobLongoEDatasPreservados() throws Exception {
    JdbcFixture f = new JdbcFixture();
    String json = "{\"texto\":\"" + "ação".repeat(2000) + "\"}";
    new ReuniaoRepoDao(f.connection).criar(new ReuniaoRepo(0, json, 0));
    ArgumentCaptor<Reader> reader = ArgumentCaptor.forClass(Reader.class);
    verify(f.insercao("REUNIAO_REPO")).setCharacterStream(eq(1), reader.capture());
    java.io.StringWriter out = new java.io.StringWriter();
    reader.getValue().transferTo(out);
    assertEquals(json, out.toString());
    new ReuniaoDao(f.connection).criar(new Reuniao(0, "Texto", f.data, StatusReuniao.AGENDADA, 11, 22, 33));
    verify(f.insercao("REUNIAO")).setTimestamp(2, Timestamp.valueOf(f.data));
    assertEquals(f.data, new ReuniaoDao(f.connection).buscar(99).getData());
  }

  @Test
  void erroSqlFechaStatementSemFecharConexaoCompartilhada() throws Exception {
    JdbcFixture f = new JdbcFixture();
    f.falharEm = "INSERT INTO CLIENTE";
    assertThrows(SQLException.class, () -> new ClienteDao(f.connection).criar(new Cliente(0, "Ana", "ME", LocalDate.of(1990, 1, 1))));
    verify(f.insercao("CLIENTE")).close();
    verify(f.connection, never()).close();
  }

  @Test
  void consultaAusenteRetornaNullEFechaRecursos() throws Exception {
    Connection c = mock(Connection.class);
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);
    when(c.prepareStatement(anyString())).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);
    assertNull(new ClienteDao(c).buscar(50));
    verify(stmt).setLong(1, 50);
    verify(rs).close(); verify(stmt).close();
  }
}
