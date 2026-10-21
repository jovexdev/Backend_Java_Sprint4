package br.com.totvs.advisormind;

public class ConsoleFormatter {

  private static final String LINHA_DUPLA = "=================================================";
  private static final String LINHA_SIMPLES = "-------------------------------------------------";

  public static void imprimir(String titulo, String conteudo) {

    System.out.println("\n" + LINHA_DUPLA);
    System.out.println("  " + titulo);
    System.out.println(LINHA_DUPLA + "\n");

    conteudo = conteudo.replace("\\n", "\n").replace("\r\n", "\n").replace("\r", "\n");

    conteudo = conteudo.replaceAll("\n{3,}", "\n\n");

    String[] linhas = conteudo.split("\n");

    for (String linha : linhas) {
      linha = linha.trim();

      if (linha.isEmpty()) {
        System.out.println();
        continue;
      }

      if (linha.matches("[A-ZÁÉÍÓÚÂÊÔÃÕÇ ]{5,}") && !linha.startsWith("-")) {
        System.out.println(LINHA_SIMPLES);
        System.out.println("  " + linha);
        System.out.println(LINHA_SIMPLES);

      } else if (linha.startsWith("-") || linha.startsWith("•")) {
        System.out.println("   " + linha);

      } else if (linha.endsWith(":")) {
        System.out.println("\n  >> " + linha);

      } else {
        System.out.println("  " + linha);
      }
    }

    System.out.println("\n" + LINHA_DUPLA + "\n");
  }
}
