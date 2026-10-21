package br.com.totvs.advisormind.service;

@org.springframework.stereotype.Service
public class VoCTrackerService {
  private final OpenAIClient client;

  public VoCTrackerService() {
    this(new OpenAIClient());
  }

  @org.springframework.beans.factory.annotation.Autowired
  public VoCTrackerService(OpenAIClient client) {
    this.client = client;
  }

  public String executar(String transcricao) {
    return executarComMetadados(transcricao, false).resultado();
  }

  public br.com.totvs.advisormind.dto.OpenAICompletion executarComMetadados(
      String transcricao, boolean compacto) {

    if (transcricao == null || transcricao.isBlank() || transcricao.length() > 4000)
      throw new br.com.totvs.advisormind.exception.ApiException(
          400, "Transcrição obrigatória, limite de 4000 caracteres.");
    String prompt =
"""
Você é um especialista em Voz do Cliente (VoC) da TOTVS.
Sua função é identificar feedbacks estratégicos nas falas do cliente durante reuniões comerciais,
conectando essas informações tanto à evolução de produto quanto a oportunidades comerciais.

FORMATO OBRIGATÓRIO — SIGA EXATAMENTE:

VOC TRACKER — VOZ DO CLIENTE

RECLAMAÇÕES E PONTOS DE DOR
- (problema ou insatisfação mencionada pelo cliente)
- (problema ou insatisfação mencionada pelo cliente)

SUGESTÕES DE MELHORIA
- (sugestão ou pedido de melhoria feito pelo cliente)
- (sugestão ou pedido de melhoria feito pelo cliente)

ELOGIOS E PERCEPÇÕES POSITIVAS
- (elogio ou ponto positivo mencionado pelo cliente)
- (elogio ou ponto positivo mencionado pelo cliente)

OPORTUNIDADES COMERCIAIS IDENTIFICADAS
- (produto ou módulo TOTVS que pode atender uma necessidade detectada na fala do cliente)
- (produto ou módulo TOTVS que pode atender uma necessidade detectada na fala do cliente)

RECOMENDAÇÃO ESTRATÉGICA PARA O VENDEDOR
(Parágrafo direto com orientação clara sobre como o vendedor deve agir após essa reunião:
qual abordagem usar, qual produto priorizar, qual dor explorar na próxima conversa)

IMPACTO PARA PRODUTO
(Uma frase indicando qual feedback desta reunião deveria ser repassado ao time de produto TOTVS)

REGRAS:
- NÃO use markdown (**, *, #)
- NÃO use \\n literal
- Linguagem comercial e estratégica
- Máximo 300 palavras
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
