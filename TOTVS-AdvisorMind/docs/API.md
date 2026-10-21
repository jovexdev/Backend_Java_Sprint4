# Contrato REST - FINAL 3.0.0

Base local: http://127.0.0.1:8080. JSON UTF-8. Todos os IDs são long positivos gerados pelo banco. Não envie id nos corpos. Campos desconhecidos, coerção de escalares, null em primitivos e frações para inteiros são rejeitados. Listagens vazias retornam []. DELETE 204 não tem corpo.

| Método | URI | Finalidade | Entrada | Sucesso | Erros |
|---|---|---|---|---|---|
| POST | /api/clientes | Criar com identidade gerada | ClienteRequest | 201 | 400, 404, 409, 500 |
| GET | /api/clientes | Listar | Nenhum | 200 | 500 |
| GET | /api/clientes/{id} | Consultar | id positivo | 200 | 400, 404, 500 |
| PUT | /api/clientes/{id} | Atualizar | id + ClienteRequest | 200 | 400, 404, 409, 500 |
| DELETE | /api/clientes/{id} | Excluir; dependências impedem exclusão | id positivo | 204 | 400, 404, 409, 500 |
| POST | /api/vendedores | Criar com identidade gerada | VendedorRequest | 201 | 400, 404, 409, 500 |
| GET | /api/vendedores | Listar | Nenhum | 200 | 500 |
| GET | /api/vendedores/{id} | Consultar | id positivo | 200 | 400, 404, 500 |
| PUT | /api/vendedores/{id} | Atualizar | id + VendedorRequest | 200 | 400, 404, 409, 500 |
| DELETE | /api/vendedores/{id} | Excluir; dependências impedem exclusão | id positivo | 204 | 400, 404, 409, 500 |
| POST | /api/vendedor-repos | Criar com identidade gerada | VendedorRepoRequest | 201 | 400, 404, 409, 500 |
| GET | /api/vendedor-repos | Listar | Nenhum | 200 | 500 |
| GET | /api/vendedor-repos/{id} | Consultar | id positivo | 200 | 400, 404, 500 |
| PUT | /api/vendedor-repos/{id} | Atualizar | id + VendedorRepoRequest | 200 | 400, 404, 409, 500 |
| DELETE | /api/vendedor-repos/{id} | Excluir; dependências impedem exclusão | id positivo | 204 | 400, 404, 409, 500 |
| POST | /api/reuniao-repos | Criar com identidade gerada | ReuniaoRepoRequest | 201 | 400, 404, 409, 500 |
| GET | /api/reuniao-repos | Listar | Nenhum | 200 | 500 |
| GET | /api/reuniao-repos/{id} | Consultar | id positivo | 200 | 400, 404, 500 |
| PUT | /api/reuniao-repos/{id} | Regerar índice e contador | id + ReuniaoRepoRequest | 200 | 400, 404, 409, 500 |
| DELETE | /api/reuniao-repos/{id} | Excluir; dependências impedem exclusão | id positivo | 204 | 400, 404, 409, 500 |
| POST | /api/reunioes | Criar com identidade gerada | ReuniaoRequest | 201 | 400, 404, 409, 500 |
| GET | /api/reunioes | Listar | Nenhum | 200 | 500 |
| GET | /api/reunioes/{id} | Consultar | id positivo | 200 | 400, 404, 500 |
| PUT | /api/reunioes/{id} | Atualizar | id + ReuniaoRequest | 200 | 400, 404, 409, 500 |
| DELETE | /api/reunioes/{id} | Excluir; dependências impedem exclusão | id positivo | 204 | 400, 404, 409, 500 |
| GET | /api/vendedores/{id}/indicadores | Senioridade; simulação opcional de comissão | id; query ticketMedio opcional | 200 | 400, 404, 500 |
| POST | /api/vendedores/{id}/registrar-reuniao | Incrementar contador histórico explícito | id; sem corpo | 200 | 400, 404, 409, 500 |
| GET | /api/reunioes/estatisticas | Contagens de reuniões/empresas e maior transcrição | Nenhum | 200 | 500 |
| GET | /api/reunioes/{id}/indicadores | Temperatura, duração e perfil acadêmicos | id positivo | 200 | 400, 404, 500 |
| POST | /api/reunioes/{id}/analises/{tipo} | Gerar análise compacta e persistir | id; tipo; sem corpo | 201 | 400, 404, 409, 500, 502, 503, 504 |
| POST | /api/reunioes/{id}/analises/{tipo}/previa | Gerar análise completa sem persistir | id; tipo; sem corpo | 200 | 400, 404, 500, 502, 503, 504 |
| GET | /api/analises | Listar resultados persistidos | Nenhum | 200 | 500 |
| GET | /api/analises/{id} | Consultar resultado persistido | id positivo | 200 | 400, 404, 500 |
| GET | /api/reunioes/{id}/analises | Listar resultados da reunião | id positivo | 200 | 400, 404, 500 |
| DELETE | /api/analises/{id} | Excluir para permitir nova análise/exclusão da reunião | id positivo | 204 | 400, 404, 500 |

Métodos não suportados: 405; mídia incompatível com JSON: 415. Todas as URIs com id exigem inteiro positivo: erro de tipo/valor retorna 400. Referência ausente detectada pelo Service retorna 404; conflito de unicidade/FK ou alteração de reunião já analisada retorna 409. A restrição final é sempre do banco, inclusive em concorrência. Falhas internas devolvem 500 sem SQL/credenciais/stacktrace. Campos do erro: status, mensagem e timestamp UTC.

## Corpos de requisição
POST cria; PUT substitui campos editáveis. Referências usam IDs reais obtidos das respostas, não os números hipotéticos abaixo.

### VendedorRepoRequest
```json
{"filtro":"Todos os vendedores"}
```
filtro opcional até 100 caracteres/bytes UTF-8; texto é uma descrição, não um predicado SQL. totalVendedores é derivado do número de VENDEDOR associados, nunca editável pelo cliente. POST inicializa 0. PUT altera filtro e recalcula contador.

### ClienteRequest
```json
{"nome":"João","empresa":"Empresa ME","dataNascimento":"1990-03-15"}
```
Nome e empresa obrigatórios até 100 caracteres/bytes UTF-8. Nascimento obrigatório LocalDate, não futuro, ISO yyyy-MM-dd, anos de 1 a 9999. UNIQUE (nome, empresa) segue comparação do banco; nenhuma normalização de caixa ou acento é imposta pela API.

### ReuniaoRepoRequest
```json
{}
```
POST cria CLOB `{"reunioes":[]}` e totalReunioes 0. PUT {} reconstrói índice e contador a partir das FKs de REUNIAO. Não aceita arquivoJson/totalReunioes enviados pelo cliente. CLOB é somente índice derivado; não é fonte independente de transcrições. Não existe arquivo JSON local ativo.

### VendedorRequest
```json
{"nome":"Ana","cargo":"Consultor","setor":"Tecnologia","totalReunioes":10,"vendedorRepoId":1}
```
Nome obrigatório até 100; cargo e setor obrigatórios até 50; limites também em bytes UTF-8. totalReunioes obrigatório entre 0 e Integer.MAX_VALUE. vendedorRepoId obrigatório e existente. Nome é único no banco. Não existe ticketMedioNegociado no DTO nem coluna TICKET_MEDIO.

### ReuniaoRequest
```json
{"transcricao":"Cliente: gostei da proposta.","data":"2026-09-01T10:00:00","status":"AGENDADA","clienteId":1,"vendedorId":1,"reuniaoRepoId":1}
```
Todos obrigatórios. Transcrição não branca, no máximo 4000 caracteres E 4000 bytes UTF-8; nada é truncado. IDs devem existir. Status aceitos pela aplicação: AGENDADA, ATENDIDA, CANCELADA (domínio dos exemplos recebidos; DDL só limita comprimento). Data LocalDateTime ISO yyyy-MM-ddTHH:mm:ss, sem offset/fuso, sem frações; anos 1 a 9999. Oracle DATE inclui hora/minuto/segundo; UNIQUE(clienteId, data) não limita a uma reunião por dia: horários diferentes no mesmo dia são permitidos.

Reuniao retorna id, transcricao, data, status, clienteId, vendedorId e reuniaoRepoId. Vendedor retorna os campos do pedido mais id. Cliente retorna os campos do pedido mais id. Repositórios retornam id e seus três/dois dados persistidos. Somente colunas efetivas são serializadas; não existem snapshots embutidos de cliente/vendedor.

## Regras adicionais
- Total de reuniões de Vendedor é contador histórico declarado, preservado da Sprint 3. Não é igual por definição à contagem da tabela REUNIAO. POST registrar-reuniao incrementa explicitamente; criar reunião não o incrementa automaticamente. totalVendedores de VENDEDOR_REPO e totalReunioes de REUNIAO_REPO são derivados das FKs presentes.
- Exclusões de pais com dependentes retornam 409. Sem cascata escondida: excluir análise, reunião, vendedor/cliente e depois repositórios quando necessário. Não excluir dados reais para demonstração.
- Reunião com análise não permite alterar transcrição, cliente, vendedor ou data sem excluir análises. Status e repositório podem ser alterados. A atualização preserva sentido do resultado analisado.
- GET /vendedores/{id}/indicadores sem ticketMedio devolve comissaoEstimada:null e motivo. Com `?ticketMedio=1500`, calcula BigDecimal com duas casas (HALF_EVEN), preservando a fórmula acadêmica. Ticket entre 0 e 999999999999.99 e até duas casas; nunca é salvo. calcularComissaoEstimada() sem parâmetro falha explicitamente; overload com BigDecimal funciona.

## IA
TIPO: 1 = smart-briefing; 2 = conversational-analytics; 3 = voc-tracker. Nomes de rota são case-sensitive. Tipo desconhecido: 404. POST principal cria (201 + Location /api/analises/id). Retorno: id, tipo, resultado, modeloIa, datahoraExecucao, tokensUtilizados e reuniaoId. Execução é registrada em UTC sem offset, com segundos, porque Oracle DATE não armazena fuso.

Há apenas um resultado por (reuniaoId, tipo), por UNIQUE. Repetição retorna 409 sem chamar o provedor se já existir; concorrência pode consumir duas chamadas, mas apenas uma grava. Excluir o resultado permite nova chamada. Não há PUT de texto/modelo/tokens para não falsificar proveniência, nem POST de resultado manual. As saídas dos três serviços são geradas pelo provedor; não por fixtures em produção.

Modo persistido solicita parágrafo compacto (até 120 caracteres, limite máximo 250 bytes). O conteúdo, o modelo e os tokens vêm do provedor. Não há substring, redução silenciosa ou zero padrão para tokens ausentes. Resposta >250 caracteres/bytes retorna 502 e nada é salvo. Modelo não informado ou tokens ausentes/inválidos retornam 502. modeloIa até 75 caracteres/bytes. Uso informado é usage.total_tokens (tokens da chamada inteira), não contagem estimada.

POST /previa preserva os prompts completos da Sprint 3, não aplica o limite de persistência de 250 e retorna {reuniaoId,tipo,resultado,modeloIa,tokensUtilizados,persistida:false}. Não salva. Chamada paga nova a cada POST. Sem chave: 503; timeout: 504; limite/indisponibilidade do provedor: 503; erro externo/JSON inválido/saída incompleta: 502. Não há retry automático. Tempo de conexão 10 s, da requisição 45 s.

Após chamada externa, uma transação bloqueia a reunião, compara os dados lidos antes da chamada, verifica duplicidade e insere. Se a reunião mudou nesse intervalo, retorna 409 sem persistir resultado obsoleto. Banco pode falhar depois de uma chamada já cobrada; não existe transação distribuída com o provedor.

## Exemplos reais
Ver docs/evidencias/http-smoke.json: 52 chamadas reais ao JAR, incluindo requisições, respostas e três inicializações. As análises nesse arquivo são fixtures de um servidor HTTP controlado, identificadas como tal; não comprovam uso da OpenAI real.

## Integração visual
Front-End não foi fornecido; autenticação e origem CORS não foram definidas. Listener padrão em loopback. Integração visual não validada. A prévia estática publicada anteriormente corresponde à versão anterior e não foi alterada por esta entrega.
