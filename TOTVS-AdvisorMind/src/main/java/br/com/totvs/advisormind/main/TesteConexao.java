package br.com.totvs.advisormind.main;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {
  public static void main(String[] args) {
    try (Connection conexao = new ConexaoFactory().conexao()) {
      System.out.println("Conexão com o Oracle realizada com sucesso.");
    } catch (SQLException | ClassNotFoundException e) {
      System.out.println("Não foi possível conectar. Confira a conta e a URL em ConexaoFactory.");
    }
  }
}
