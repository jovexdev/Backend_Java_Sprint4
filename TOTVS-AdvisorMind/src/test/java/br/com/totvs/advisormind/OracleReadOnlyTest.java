package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Opt-in: consulta as colunas Oracle sem executar DDL ou alterar dados. */
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_READONLY", matches = "true")
class OracleReadOnlyTest {
  @Test
  void inspectRequiredColumnsAndConnectivity() throws Exception {
    try (var c =
        DriverManager.getConnection(
            System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))) {
      c.setReadOnly(true);
      String[] queries = {
        "SELECT ID_VEND_REPO,TOTAL_VENDEDORES,FILTRO FROM VENDEDOR_REPO WHERE 1=0",
        "SELECT ID_CLIENTE,NOME,EMPRESA,DATA_NASCIMENTO FROM CLIENTE WHERE 1=0",
        "SELECT ID_REUN_REPO,ARQUIVO_JSON,TOTAL_REUNIOES FROM REUNIAO_REPO WHERE 1=0",
        "SELECT ID_VENDEDOR,NOME,CARGO,SETOR,TOTAL_REUNIOES,VENDEDOR_REPO_ID_VEND_REPO FROM"
            + " VENDEDOR WHERE 1=0",
        "SELECT"
            + " ID_REUNIAO,TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO"
            + " FROM REUNIAO WHERE 1=0",
        "SELECT"
            + " ID_ANALISE,TIPO,RESULTADO,MODELO_IA,DATAHORA_EXECUCAO,TOKENS_UTILIZADOS,REUNIAO_ID_REUNIAO"
            + " FROM ANALISE_IA WHERE 1=0"
      };
      for (String q : queries)
        try (var s = c.prepareStatement(q);
            var r = s.executeQuery()) {
          assertTrue(r.getMetaData().getColumnCount() >= 3);
        }
    }
  }
}
