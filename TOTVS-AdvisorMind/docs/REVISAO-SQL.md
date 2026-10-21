# Revisão técnica do SQL recebido

O SQL foi enviado como texto no chat em 19/10/2026. sql/recebido.sql transcreve seus comandos/literais e sequência, com espaçamento e formatação normalizados. Não é alegada identidade binária com um arquivo original não fornecido. Auxiliares estão separados e identificados; não alteram silenciosamente o modelo.

| Item | Resultado / impacto | Proposta |
|---|---|---|
| Ordem do DDL | Correta: pais antes de dependentes | Preservar |
| Ordem do DML | Correta em schema recém-criado com identidades iniciando em 1 | Para carga suplementar, usar IDs recuperados/chaves naturais |
| Limpeza anunciada | Não há comando de limpeza | Corrigir descrição do roteiro; não acrescentamos DROP automático |
| Reexecução | CREATE falha em tabelas existentes; sem UNIQUE em repos, parte da carga pode duplicar repos se cliente SQL continuar após erros | Executar original uma vez em schema vazio; auxiliar DEMO separado |
| IDs fixos de FK | 1..5 pressupõem identidade e schema inicial | Não usar essa técnica no JDBC/API; pedir coluna de identidade explicitamente |
| VENDEDOR_REPO | Contadores recebidos 5,2,3,1,4; relações efetivas produzem 2,1,1,1,0 | API e auxiliar recalculam por FK |
| FILTRO e associação | Gabriel é Serviços mas aponta ao repo descrito como Setor Tecnologia | FILTRO tratado como descrição; não mudar FKs oficiais por adivinhação |
| REUNIAO_REPO | Cada repo recebe uma reunião, mas contadores são 2,1,2,1,2 e JSON usa nomes externos, não IDs relacionais comprovados | Definida projeção {reunioes:[IDs]} derivada das FKs |
| Clientes/vendedores | Unicidades, NOT NULL e tamanhos da carga são compatíveis em avaliação local | Oracle real ainda pendente |
| Datas | TO_DATE sem hora gera meia-noite; DATE também aceita hora | UNIQUE compara cliente + segundo completo, não dia |
| RESULTADO=250 | Respostas completas dos prompts podem exceder | Compacto validado para persistir; prévia completa não salva; sugestão formal CLOB |
| MODELO_IA/tokens da carga | Gemini e números são exemplos acadêmicos, não prova de consumo de API | Não apresentar como execução real; produção usa metadados retornados |
| Tokens * 1.10 | Projeção hipotética válida; não custo monetário efetivo | Preservar e identificar hipótese |
| Cinco categorias DQL | Todas presentes e executadas no H2 adaptado | Consultas preservadas em auxiliar |
| DQL repetida ao final | Não altera dados; produz relatórios repetidos | Preservado no recebido, auxiliar contém uma cópia de cada |
| Constraints de domínio | DDL não limita negativos nem status/código aos valores da API | Validação de aplicação sem inventar CHECKs oficiais |
| Comentários | Tabelas comentadas; faltam comentários de colunas exigidos na rubrica Database da Sprint 3 | Equipe Database pode complementar formalmente |

## Propostas separadas
- oracle-create.sql: DDL recebido separado, para schema vazio, sem limpeza automática.
- proposta-demo-oracle.sql: carga suplementar mínima com natural keys/RETURNING; sequencial, sem concorrência e sem produzir IA fictícia. Não substitui os cinco registros por tabela da carga acadêmica.
- proposta-sincronizar-repos-oracle.sql: ajusta apenas contadores/índice derivado, com execução manual e sem concorrência. JSON_ARRAYAGG requer Oracle 12.2+; aplicação JDBC não depende dessa função.
- Não foi criada migração automática de JSON antigo: faltam data de nascimento, data/status da reunião e FKs. Não há valores-padrão inventados para dados reais.

## Validação realizada
A transcrição do script foi executada em um H2 novo em MODE=Oracle, alterando somente a expressão de identidade para a sintaxe H2. DDL, DML, UPDATE/DELETE e SELECTs foram aceitos. Ao final, cinco linhas em cada uma das seis tabelas; o segundo CREATE foi corretamente rejeitado. Isso não comprova semântica/performance/NLS/privilegios de Oracle remoto.
