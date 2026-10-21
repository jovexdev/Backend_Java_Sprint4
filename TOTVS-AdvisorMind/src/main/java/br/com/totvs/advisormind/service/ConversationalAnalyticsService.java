package br.com.totvs.advisormind.service;

@org.springframework.stereotype.Service
public class ConversationalAnalyticsService {
  private final OpenAIClient client;

  public ConversationalAnalyticsService() {
    this(new OpenAIClient());
  }

  @org.springframework.beans.factory.annotation.Autowired
  public ConversationalAnalyticsService(OpenAIClient client) {
    this.client = client;
  }

  public String executar(String transcricao) {
    return executarComMetadados(transcricao, false).getResultado();
  }

  public br.com.totvs.advisormind.dto.OpenAICompletion executarComMetadados(
      String transcricao, boolean compacto) {

    if (transcricao == null || transcricao.isBlank() || transcricao.length() > 4000)
      throw new br.com.totvs.advisormind.exception.ApiException(
          400, "Transcrição obrigatória, limite de 4000 caracteres.");
    String prompt =
"""
Você é um analista de dados conversacionais da TOTVS.
Sua função é transformar transcrições de reuniões comerciais em métricas e padrões visuais mensuráveis.

FORMATO OBRIGATÓRIO — SIGA EXATAMENTE:

CONVERSATIONAL ANALYTICS — ANÁLISE DA CONVERSA

ARCO DE SENTIMENTO DO CLIENTE
Início:
(Como o cliente estava no começo: receptivo, desconfiado, neutro? Justifique com base na conversa)

Meio:
(Houve mudança de humor? O cliente ficou mais engajado ou resistente?)

Fim:
(Como o cliente encerrou a conversa: positivo, indeciso, insatisfeito?)

TALK TO LISTEN RATIO
(Estime com base na transcrição quem falou mais)
Vendedor:  ██████████ (XX%)
Cliente:   ████████   (XX%)
Avaliação: (Uma frase sobre se o equilíbrio foi saudável para uma reunião comercial)

PRINCIPAIS TEMAS ABORDADOS
- (tema ou produto mencionado)
- (tema ou produto mencionado)
- (tema ou produto mencionado)
- (tema ou produto mencionado)

MOMENTO CRÍTICO DA REUNIÃO
(Descreva o momento mais decisivo da conversa: onde o cliente demonstrou maior reação, positiva ou negativa)

REGRAS:
- NÃO use markdown (**, *, #)
- NÃO use \\n literal
- Linguagem analítica e direta
- Máximo 280 palavras
- Se não houver dados suficientes para alguma seção, escreva "Não identificado na transcrição"

TRANSCRIÇÃO DA REUNIÃO:
"""
            + transcricao;

    if (compacto)
      prompt +=
          "\n"
              + "MODO PERSISTÊNCIA: as seções acima são contexto da análise. Nesta chamada responda"
              + " somente um parágrafo compacto, sem títulos, com até 120 caracteres e no máximo"
              + " 250 bytes UTF-8. Preserve apenas os achados evidenciados; não invente dados.";
    return client.enviarComMetadados(prompt);
  }
}
