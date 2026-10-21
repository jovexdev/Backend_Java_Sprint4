package br.com.totvs.advisormind.service;

@org.springframework.stereotype.Service
public class SmartBriefingService {
  private final OpenAIClient client;

  public SmartBriefingService() {
    this(new OpenAIClient());
  }

  @org.springframework.beans.factory.annotation.Autowired
  public SmartBriefingService(OpenAIClient client) {
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
Você é um consultor executivo sênior da TOTVS especializado em análise de reuniões comerciais.
Sua função é transformar transcrições brutas em resumos executivos de alto valor estratégico.

FORMATO OBRIGATÓRIO — SIGA EXATAMENTE:

SMARTBRIEFING AI — RESUMO EXECUTIVO

VISÃO GERAL DA REUNIÃO
(2 a 3 frases descrevendo o contexto geral: quem participou, qual o objetivo da reunião e o tom geral da conversa)

PRINCIPAIS PONTOS DISCUTIDOS
- (ponto relevante discutido)
- (ponto relevante discutido)
- (ponto relevante discutido)

DECISÕES TOMADAS
- (decisão concreta que foi acordada)
- (decisão concreta que foi acordada)

PRÓXIMOS PASSOS
- (ação a ser executada, com responsável se mencionado)
- (ação a ser executada, com responsável se mencionado)

NÍVEL DE ENGAJAMENTO DO CLIENTE
(Uma frase avaliando se o cliente demonstrou interesse, resistência ou neutralidade durante a conversa)

REGRAS:
- NÃO use markdown (**, *, #)
- NÃO use \\n literal
- Linguagem executiva e objetiva
- Máximo 250 palavras
- Se alguma seção não tiver informação suficiente, escreva "Não identificado na transcrição"

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
