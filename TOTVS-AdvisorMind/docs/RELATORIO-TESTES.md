# Relatório de testes - FINAL 3.0.0

> Este relatório registra a entrega anterior. A validação da revisão atual com JDBC direto está em [ALINHAMENTO-JDBC.md](ALINHAMENTO-JDBC.md).

Data: 19/10/2026, America/Sao_Paulo. Ambiente Linux, Azul Zulu JDK21.0.8, Maven3.9.11, Spring Boot3.5.7. Sem credenciais Oracle/OpenAI reais.

## Comandos executados
- `mvn -o -f final-project/TOTVS-AdvisorMind/pom.xml -DskipTests package`: compilação inicial passou; não alegada como teste.
- `mvn -o -f final-project/TOTVS-AdvisorMind/pom.xml clean verify`: execução final, código de saída0, BUILD SUCCESS. `-o` porque dependências já estavam no cache local; para primeiro uso, executar sem -o.
- `JAVA_HOME=<JDK21> python final-project/TOTVS-AdvisorMind/scripts/smoke_http.py`: JAR real, porta18080, H2 em arquivo temporário e três inicializações; 52 verificações passaram. Quatro chamadas ao provedor HTTP controlado (três persistências e uma prévia).

## Resultados finais
| Classe | Testes | Passaram | Falhas/erros | Ignorados |
|---|---:|---:|---:|---:|
|OpenAIClientTest|8|8|0/0|0|
|OracleTransactionalProbeTest|1|0|0/0|1|
|SqlScriptTest|1|1|0/0|0|
|DomainRulesTest|3|3|0/0|0|
|RelationalApiIntegrationTest|21|21|0/0|0|
|OracleReadOnlyTest|1|0|0/0|1|
|NoKeyApiTest|1|1|0/0|0|
| TOTAL |36|34|0/0|2|

## Cobertura verificada
CRUD dos cinco recursos editáveis, leitura/exclusão de análises e criação por IA; identidades geradas; mapeamento das seis tabelas; contadores e CLOB derivado; mais de4000 caracteres no CLOB; mudanças de repositório; unicidades; FKs e exclusões com dependentes; data por segundo, formato estrito e rejeição de fuso/fração; dados e limites de caracteres/bytes; transcrição4000 aceita/4001 rejeitada; tipos, status e IDs inválidos; erroSQL sem vazamento; rollback de reunião e vendedor quando atualização de cache/contador falha; regras legadas; comissão não persistida; ausência de chave em todos módulos; transporte HTTP IA, timeout e erros; tokens/modelo válidos; resultado250 aceito/>250 rejeitado sem truncar; preview completo não salvo; duplicidade não chama novamente provedor; mutação de conteúdo já analisado é bloqueada.

52 chamadas HTTP ao JAR verificaram CRUD, dados inválidos, falhas esperadas, três análises com metadados controlados, consulta de todas as seis entidades após duas reinicializações, integridade de exclusão, sincronização de contadores e remoção ordenada. Teste usa diretório temporário e não altera os dados do usuário.

## Ocorrência corrigida
A primeira execução encontrou uma falha: Jackson aceitava Z em LocalDateTime e descartava o offset. Adicionado formato estrito sem fuso/frações no DTO; todos os testes foram reexecutados e passaram. FalhasSQL e respostas409/503 nos testes são cenários esperados, não defeitos escondidos.

## Não executado
- OracleReadOnlyTest e OracleTransactionalProbeTest: dois testes opt-in ignorados sem flags/acesso autorizado. O primeiro inspeciona colunas por SELECT; o segundo testa os seis INSERTs/IDs, DATE,CLOB, constraints, update/delete e rollback em transação isolada. Não há DDL ou limpeza automática de Oracle. Ainda é preciso executar e revisar ambiente real antes de certificar Oracle.
- OpenAI real: nenhuma chamada paga; os modelos/tokens registrados nas evidências são do provedor local controlado, não uso real.
- Interface/prints: Front-End não enviado.
- Modelos Data Science: não enviados.

Evidências completas em docs/evidencias: maven-verify.txt, resumosSurefire, ambiente.json e http-smoke.json. Valores do DML original Gemini/tokens não são usados como prova de execução do provedor.
