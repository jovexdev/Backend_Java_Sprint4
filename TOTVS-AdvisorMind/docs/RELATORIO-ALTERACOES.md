# Relatório de alterações - FINAL 3.0.0

Base preservada: versão2.0.0 gerada anteriormente nesta conversa. O projeto foi evoluído, mantendo artefato Maven, Java21, Spring Boot/JDBC, pacote br.com.totvs.advisormind, entidades centrais e regras úteis. O SQL recebido passa a ser fonte prioritária do esquema. Versão final3.0.0. Não foi alterado banco remoto.

## Mudanças principais
- T_TOTVS_VENDEDOR e persistência em arquivo deixam de ser usados; DAOs refletem seis tabelas e identidades.
- Cliente ganha ID/dataNascimento; reunião ganha data/status/FKs obrigatórias; vendedor perde ticket persistido e ganha FK de agrupamento.
- Repositórios são entidades reais com caches/contadores derivados, atualizados sob transações.
- AnáliseIA ganha persistência com metadados reais, unicidade e verificação de limite, além de prévia completa não salva.
- Entradas por DTO, novos controllers/services/DAOs, exceções409 para integridade, formato de datas estrito e proteção de conteúdo já analisado.
- Console preservado e adaptado. Exemplos manuais antigos dependiam do esquema anterior; substituídos por testes automatizados e probes Oracle opt-in.
- Documentação, diagramas, Postman, exemplos e scriptHTTP final atualizados; SQL recebido e propostas separados.
- JSONs legados em examples são somente arquivo histórico; não há importação automática com datas/FKs inventadas.

## Inventário por arquivo

| Arquivo | Estado | Motivo |
|---|---|---|
|.env.example|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|.gitignore|Preservado|Compatível, reaproveitado sem alteração.|
|LEIA-ME.md|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|README.md|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|docs/ALTERACOES.md|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/API.md|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|docs/AdvisorMind-FINAL.postman_collection.json|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|docs/AdvisorMind.postman_collection.json|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/DECISOES.md|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|docs/RELATORIO-ALTERACOES.md|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|docs/RELATORIO-TESTES.md|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|docs/REVISAO-SQL.md|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|docs/TOTVS-AdvisorMind-Sprint4-Documentacao-FINAL.pdf|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|docs/TOTVS-AdvisorMind-Sprint4-Documentacao.pdf|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/VALIDACAO.md|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/arquitetura.svg|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/classes-dominio.mmd|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/classes-dominio.svg|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|docs/diagramas/arquitetura.png|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/arquitetura.svg|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/camadas.mmd|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/classes-dominio.mmd|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/classes-dominio.png|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/classes-dominio.svg|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/modelo-relacional.mmd|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/modelo-relacional.png|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/diagramas/modelo-relacional.svg|Criado|Diagrama editável/exportado correspondente às seis tabelas e classes finais.|
|docs/endpoints.json|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|docs/evidencias/ambiente.json|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.ApiIntegrationTest.txt|Removido|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.DomainRulesTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.NoKeyApiTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.OpenAIClientTest.txt|Modificado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.OracleReadOnlyTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.OracleTransactionalProbeTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.RelationalApiIntegrationTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.RepositoryAndRulesTest.txt|Removido|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/br.com.totvs.advisormind.SqlScriptTest.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/http-smoke.json|Modificado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/evidencias/maven-verify.txt|Criado|Registro real da execução final de Maven/testes/HTTP, com limitações indicadas.|
|docs/requests.http|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|examples/reunioes-sprint3.json|Preservado|Compatível, reaproveitado sem alteração.|
|pom.xml|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|reunioes.json|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|scripts/smoke_http.py|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|sql/dql-academico.sql|Criado|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|sql/oracle-create.sql|Criado|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|sql/oracle.sql|Removido|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|sql/proposta-demo-oracle.sql|Criado|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|sql/proposta-sincronizar-repos-oracle.sql|Criado|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|sql/recebido.sql|Criado|Separar SQL recebido, DDL, DQL e propostas auxiliares sem alteração silenciosa.|
|src/main/java/br/com/totvs/advisormind/AdvisorMindApplication.java|Preservado|Compatível, reaproveitado sem alteração.|
|src/main/java/br/com/totvs/advisormind/ConsoleFormatter.java|Preservado|Compatível, reaproveitado sem alteração.|
|src/main/java/br/com/totvs/advisormind/Main.java|Modificado|Adaptar console aos campos obrigatórios/IDs gerados e mesmos Services.|
|src/main/java/br/com/totvs/advisormind/conexoes/ConexaoFactory.java|Preservado|Compatível, reaproveitado sem alteração.|
|src/main/java/br/com/totvs/advisormind/controller/AnaliseController.java|Criado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/controller/ClienteController.java|Criado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/controller/ReuniaoController.java|Modificado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/controller/ReuniaoRepoController.java|Criado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/controller/VendedorController.java|Modificado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/controller/VendedorRepoController.java|Criado|Rotas e DTOs de entrada separados dos IDs gerados/modelo persistido; códigos HTTP consistentes.|
|src/main/java/br/com/totvs/advisormind/dao/AnaliseIADao.java|Criado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dao/ClienteDao.java|Criado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dao/JdbcSupport.java|Criado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dao/ReuniaoRepoDao.java|Criado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dao/VendedorDao.java|Modificado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dao/VendedorRepoDao.java|Criado|Mapeamento explícito de colunas oficiais, PreparedStatement e identidades via coluna PK.|
|src/main/java/br/com/totvs/advisormind/dto/AnalisePreview.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/dto/ClienteRequest.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/dto/OpenAICompletion.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/dto/ReuniaoRepoRequest.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/dto/ReuniaoRequest.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/dto/VendedorRepoRequest.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/dto/VendedorRequest.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/entities/AnaliseIA.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/entities/Cliente.java|Modificado|Adicionar identidade e LocalDate de nascimento, preservando regra de perfil.|
|src/main/java/br/com/totvs/advisormind/entities/Reuniao.java|Modificado|Representar identidade, data/status e três FKs; manter heurísticas.|
|src/main/java/br/com/totvs/advisormind/entities/ReuniaoRepo.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/entities/StatusReuniao.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/entities/TipoAnalise.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/entities/Vendedor.java|Modificado|Mapear FK do agrupamento; remover ticket persistido; preservar fórmula com BigDecimal explícito.|
|src/main/java/br/com/totvs/advisormind/entities/VendedorRepo.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/exception/ApiErrorHandler.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/exception/ApiException.java|Preservado|Compatível, reaproveitado sem alteração.|
|src/main/java/br/com/totvs/advisormind/main/TesteConexao.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/TesteRegrasNegocio.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/vendedor/TesteAtualizarVendedor.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/vendedor/TesteBuscarVendedorPorId.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/vendedor/TesteDeletarVendedor.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/vendedor/TesteInserirVendedor.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/main/vendedor/TesteSelecionarVendedor.java|Removido|Dependia do contrato híbrido anterior; substituído por artefato relacional equivalente. Original preservado no pacote anterior.|
|src/main/java/br/com/totvs/advisormind/repository/ReuniaoRepository.java|Modificado|Trocar memória/arquivo por persistência JDBC no esquema oficial.|
|src/main/java/br/com/totvs/advisormind/service/AnaliseService.java|Modificado|Integrar provedor, metadados, limites e persistência transacional; prévia separada.|
|src/main/java/br/com/totvs/advisormind/service/ClienteService.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/service/ConversationalAnalyticsService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/service/OpenAIClient.java|Modificado|Exigir modelo e uso de tokens válidos; preservar HTTP/timeout e tratar resposta vazia.|
|src/main/java/br/com/totvs/advisormind/service/RelatorioService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/service/ReuniaoRepoService.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/service/ReuniaoService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/service/SmartBriefingService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/service/ValidationService.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/service/VendedorRepoService.java|Criado|Necessário à integração relacional ou evidência/documentação desta entrega.|
|src/main/java/br/com/totvs/advisormind/service/VendedorService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/service/VoCTrackerService.java|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/java/br/com/totvs/advisormind/util/JsonUtil.java|Modificado|Remover segunda fonte em arquivo; gerar apenas índice derivado de IDs.|
|src/main/resources/application-local.properties|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/resources/application-oracle.properties|Preservado|Compatível, reaproveitado sem alteração.|
|src/main/resources/application.properties|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/main/resources/schema-local.sql|Modificado|Alinhar contrato e persistência ao SQL fornecido.|
|src/test/java/br/com/totvs/advisormind/ApiIntegrationTest.java|Removido|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/DomainRulesTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/NoKeyApiTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/OpenAIClientTest.java|Modificado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/OracleReadOnlyTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/OracleTransactionalProbeTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/RelationalApiIntegrationTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/RepositoryAndRulesTest.java|Removido|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/java/br/com/totvs/advisormind/SqlScriptTest.java|Criado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/resources/application-test.properties|Modificado|Cobrir modelo/relacionamentos novos, erros, rollback e IA; probes Oracle são opt-in.|
|src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker|Preservado|Compatível, reaproveitado sem alteração.|
