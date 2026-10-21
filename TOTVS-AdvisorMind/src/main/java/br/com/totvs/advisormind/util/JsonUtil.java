package br.com.totvs.advisormind.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class JsonUtil {
  private static final ObjectMapper JSON = new ObjectMapper();

  public static String indice(List<Long> ids) {
    try {
      return JSON.writeValueAsString(Map.of("reunioes", ids));
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Falha ao construir índice derivado.", e);
    }
  }
}
