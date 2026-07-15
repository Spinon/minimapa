# Auditoria pública de riscos jurídicos e fraude

Data-base: 2026-07-14

## Escopo e ressalva

Esta é uma auditoria preventiva do desenho do produto, não um parecer jurídico. Legislação municipal, contratos, tributos, seguros e enquadramento de cada modalidade exigem validação profissional na cidade piloto. O objetivo é revelar abusos previsíveis e incorporar controles sem quebrar o `QuestCore` ou a modularização.

## Parecer executivo

O desenho possui boas bases: verificação independente de credenciais, RLS planejada, aceite atômico, eventos idempotentes, ledgers separados, propostas privadas, navegação sem publicidade e contratos de módulo versionados.

Ainda existem cinco blockers para beta público:

1. categoria e território ainda não determinam gates legais obrigatórios;
2. a ativação automática pelo Conselho pode contornar revisão jurídica;
3. endereço/localização e publicação por não verificados ainda permitem engenharia social e emboscada;
4. pagamento, responsabilidade consumerista e seguro não têm dono operacional definido;
5. menores, RPG geolocalizado e recompensas aleatórias ficaram materialmente mais regulados pelo ECA Digital.

## Solução transversal sem refatorar a engine

Adicionar um `PolicyGate` universal às transições sensíveis:

`publish → reveal_precise_location → apply → accept → assign → start → complete → reward → pay`

O core envia fatos tipados e recebe `ALLOW`, `REQUIRE_REVIEW` ou `DENY`, com códigos explicáveis. Políticas são módulos independentes:

- `CategoryPolicyRegistry`: permitido, restrito, regulado ou proibido;
- `JurisdictionPolicy`: país/UF/município, vigência e obrigação local;
- `IdentityAndAgePolicy`: nível de verificação, maioridade e responsável;
- `CredentialAndVehiclePolicy`: credencial, ART/RRT, CNH/EAR, veículo, registro e validade;
- `LocationDisclosurePolicy`: precisão conforme relação, finalidade e janela;
- `PaymentRiskPolicy`: PSP, KYC, limites, payout, refund e anomalia;
- `EvidencePolicy`: desafios, confirmações e disputa por modalidade;
- `ContentSafetyPolicy`: itens/serviços proibidos, links, anexos e notice/action;
- `ConsumerRemedyPolicy`: informação, atendimento, cancelamento e reembolso;
- `IncentiveIntegrityPolicy`: XP, Gold, ratings, preço e anticolusão.

Cada `QuestModuleContract` declara fatos e policies exigidas, mas não pode desabilitar policies globais. A decisão e a versão das políticas entram no audit log e no snapshot da quest.

## Achados prioritários

### `AUD-01` — ativação comunitária de categoria regulada ou ilegal — crítico

**Brecha:** o Conselho pode publicar automaticamente um vencedor compatível com schema. Schema técnico não comprova legalidade, seguro, credencial, fiscalização local ou segurança.

**Abuso:** uma votação coordenada ativa transporte irregular, serviço elétrico perigoso, atividade de saúde, transporte de produto restrito ou categoria criada para fraude.

**Correção modular:** o Conselho apenas nomeia `CANDIDATE`. `CategoryPolicyRegistry + JurisdictionPolicy` exigem um `ComplianceApproval` assinado e versionado para `ACTIVE_BETA`. Sem ele, a definição continua invisível/inexecutável. Rollback e kill switch independem de nova votação.

### `AUD-02` — emboscada, stalking e exposição de residência — crítico

**Brecha:** contas não verificadas podem publicar; quests, parties e tracking dependem de localização. Um badge não evita endereço-isca, doxxing, coleta de rotina ou encontro físico malicioso.

**Correção modular:** no piloto, ambas as partes devem ser adultas e verificadas. Em desenho futuro, não verificado pode salvar/submeter para moderação, mas descoberta mostra somente célula/área aproximada. Endereço exato é criptografado e liberado por `LocationDisclosurePolicy` somente após atribuição, finalidade ativa e janela curta. Pontos seguros, bloqueio, contato protegido e botão de incidente complementam o gate.

### `AUD-03` — transporte de passageiros fora da regulação local — alto

**Base:** a Lei 13.640/2018 atribui regulamentação/fiscalização aos municípios e DF e prevê CNH com EAR, requisitos de veículo/CRLV, antecedentes, INSS e seguros no regime aplicável. Operar sem requisitos locais pode caracterizar transporte ilegal.

**Correção modular:** `PassengerTransportPolicy` por município, com feature flag fechada por padrão; validação de CNH/EAR, CRLV, antecedentes, veículo, seguro e cadastro local com expiração. Não ativar passageiros no primeiro piloto.

Fonte: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13640.htm

### `AUD-04` — transporte de cargas sem RNTRC/CIOT/piso/documentos quando aplicáveis — alto

**Base:** a ANTT informa que transporte rodoviário remunerado de cargas depende de RNTRC; regras de CIOT foram ampliadas em 2026, enquanto piso mínimo e vale-pedágio dependem das características da operação. Entrega urbana não deve ser presumida isenta sem análise.

**Correção modular:** classificador de operação (`urban/local`, carga fracionada/lotação, veículo, território, contratante, tipo de carga) produz obrigações; `CargoTransportPolicy` bloqueia sem RNTRC/CIOT/PEF/piso/seguro/documentos quando incidentes. A plataforma não calcula enquadramento apenas por “tipo da quest”.

Fontes: https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/requisitos e https://www.gov.br/antt/pt-br/assuntos/cargas/ciot-para-todos-1

### `AUD-05` — serviço regulado tratado como habilidade comum — alto

**Brecha:** XP ou diploma genérico pode parecer autorização para atividade que exige habilitação, responsabilidade técnica ou segurança ocupacional.

**Base:** contratos de atividades técnicas abrangidas pelo Sistema Confea/Crea podem exigir ART; serviços em eletricidade possuem requisitos de segurança da NR-10.

**Correção modular:** `CredentialAndSafetyPolicy` diferencia skill, treinamento, registro profissional, licença e documento de responsabilidade técnica. Categorias reguladas não entram apenas por XP. Requisitos obrigatórios são definidos pelo registry, não pelo autor da quest.

Fontes: https://www.confea.org.br/servicos-prestados/anotacao-de-responsabilidade-tecnica-art e https://www.gov.br/trabalho-e-emprego/pt-br/acesso-a-informacao/participacao-social/conselhos-e-orgaos-colegiados/comissao-tripartite-partitaria-permanente/normas-regulamentadora/normas-regulamentadoras-vigentes/norma-regulamentadora-no-10-nr-10

### `AUD-06` — responsabilidade consumerista subestimada — alto

**Brecha:** chamar o produto de mero intermediador ou não cobrar take rate não elimina deveres. Oferta, publicidade, seleção de fornecedores, pagamento, suporte e confiança na marca podem gerar responsabilidade conforme o caso.

**Base:** CDC protege informação, segurança e adequação; o Decreto 7.962/2013 exige informação clara, atendimento eletrônico e meios para cancelamento. A Senacon recomenda seleção/cadastro de fornecedores e medidas preventivas contra produtos ilegais/falsificados.

**Correção modular:** identificação clara de cada fornecedor, termos e responsabilidades antes do aceite, canal de atendimento, protocolo, cancelamento, disputa, recall/retirada, moderação e preservação de evidência. `ConsumerRemedyPolicy` gera prazos e ações por tipo de contratação.

Fontes: https://www.planalto.gov.br/ccivil_03/leis/l8078compilado.htm, https://www.planalto.gov.br/ccivil_03/_ato2011-2014/2013/decreto/d7962.htm e https://www.gov.br/mj/pt-br/assuntos/noticias/senacon-fiscaliza-pirataria-e-praticas-abusivas-em-plataformas-de-venda-digitais

### `AUD-07` — excesso de CPF, documento, biometria e localização — alto

**Brecha:** verificação forte pode se transformar em banco central de documentos e biometria. Biometria é dado sensível; rotas e endereços revelam rotina, saúde, religião ou relações mesmo quando a coordenada isolada não é classificada como sensível.

**Base:** a LGPD exige finalidade, necessidade, transparência, segurança, prevenção e prestação de contas; segurança deve existir desde a concepção. A ANPD recomenda RIPD suficientemente detalhado para tratamentos de risco e mantém biometria como tema regulatório/fiscalizatório.

**Correção modular:** guardar preferencialmente resultado/claim e identificador do fornecedor, não selfie/documento bruto; separar vault, chaves, acesso e retenção; RIPD antes do piloto; mapa de bases legais e operadores; direitos do titular; resposta a incidente; telemetria sem coordenada precisa por padrão.

Fontes: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm e https://www.gov.br/anpd/pt-br/canais_atendimento/agente-de-tratamento/relatorio-de-impacto-a-protecao-de-dados-pessoais-ripd

### `AUD-08` — RPG geolocalizado, party, publicidade e loot boxes acessíveis a menores — alto

**Base:** a Lei 15.211/2025 (ECA Digital) criou deveres para produtos de acesso provável por crianças/adolescentes, incluindo aferição de idade, supervisão, jogos e publicidade, e veda loot boxes em jogos direcionados ou de acesso provável por esse público.

**Correção modular:** piloto 18+. Antes de admitir menores, criar experiência separada child-safe: age assurance proporcional, responsável/supervisão, ausência de marketplace profissional, endereço/party com estranhos e publicidade comportamental; nenhuma loot box. Drops conquistados devem ser determinísticos ou não pagos e ainda passar por revisão específica.

Fonte: https://www.gov.br/anpd/pt-br/assuntos/eca-digital

### `AUD-09` — vínculo e controle algorítmico — alto

**Brecha:** preço unilateral, punição por recusa, metas, bloqueio automático, prioridade comprável, direção detalhada do trabalho e dependência podem contribuir para alegações de subordinação. O fato de não haver take rate não resolve a natureza da relação.

**Base:** o Tema 1.291 do STF sobre vínculo entre plataformas e trabalhadores continua sem tese final; o julgamento foi retirado de pauta em junho de 2026 e o cenário permanece dinâmico.

**Correção sem mudar o core:** preservar autonomia real: autor define oferta, executor pode recusar/contra-ofertar, nenhuma punição por taxa de aceite, agenda voluntária, critérios explicáveis, recurso humano para bloqueio, ausência de exclusividade e documentação de decisões. Fazer revisão trabalhista antes de matching automático ou incentivos de produtividade.

Fontes: https://portal.stf.jus.br/jurisprudenciarepercussao/verandamentoprocesso.asp?classeprocesso=RE&incidente=6679823&numeroprocesso=1446336&numerotema=1291 e https://noticias.stf.jus.br/postsnoticias/a-pedido-da-dpu-e-do-mpt-stf-retira-de-pauta-julgamento-sobre-relacoes-de-trabalho-em-plataformas-digitais/

### `AUD-10` — custódia financeira, lavagem e fraude de pagamento — alto

**Brecha:** quests circulares, cartões roubados, payout rápido, refund para destino diferente, conta laranja e conversão entre Gold/loja/quest podem lavar recursos ou transferir perda à plataforma.

**Base:** gestão de conta e movimentação de recursos são atividades reguladas no ecossistema de instituições/arranjos de pagamento.

**Correção modular:** Minimapa não custodia saldo. PSP de marketplace licenciado recebe, tokeniza, faz KYC, split/payout e chargeback; payout com hold por risco; beneficiário e reembolso vinculados à transação original; limites de velocidade/valor; detecção de pares circulares; Gold isolado e sem cashout/transferência/bens físicos.

Fonte: https://www.bcb.gov.br/estabilidadefinanceira/instituicaopagamento

### `AUD-11` — fake quest, falsa conclusão e farming — alto

**Brecha:** solicitante e executor coludem para gerar XP profissional, Gold, reputação, preço histórico e eventual payout sem serviço real. GPS sozinho é falsificável.

**Correção modular:** `EvidencePolicy` por modalidade combina desafio de início, coerência temporal, confirmação bilateral, evidência proporcional, janela de disputa e sinais antifraude. Recompensas ficam pendentes; repetição de pares, ciclos, device/account graph e velocidade impossível reduzem confiança. Reversões usam eventos compensatórios. Nunca exigir foto invasiva quando outro sinal basta.

### `AUD-12` — conteúdo criminoso, proibido ou golpe — alto

**Brecha:** quests, chat, loja e anúncios podem recrutar crime, transportar item ilícito, aplicar phishing, vender produto falsificado ou publicar conteúdo que gere dano grave.

**Base:** o STF definiu deveres estruturais/de cuidado para plataformas em hipóteses relevantes de conteúdo ilícito; o CDC/Senacon também reforçam prevenção no comércio eletrônico.

**Correção modular:** taxonomia proibida/restrita, moderação por risco, safe-link, limite de anexos, notice/action, canal a autoridades, retenção legalmente adequada e kill switch. Conteúdo grave não espera apenas rating comunitário. Módulos novos herdam `ContentSafetyPolicy` obrigatoriamente.

Fonte: https://noticias.stf.jus.br/postsnoticias/plataformas-terao-60-dias-para-implementar-medidas-estruturais-decide-stf/

### `AUD-13` — conta tomada e engenharia social — alto

**Brecha:** uma sessão roubada expõe endereço, permite aceitar quest, trocar payout, obter documento ou mandar falsa instrução de emergência.

**Correção modular:** MFA/step-up para ações sensíveis, device/session inventory, revogação, alertas fora de banda, cooldown para alteração de payout, recuperação sem downgrade, push sem endereço na lockscreen e equipe incapaz de solicitar senha/código/pagamento.

### `AUD-14` — preço, propostas e referências manipuladas — médio

**Brecha:** pares artificiais deslocam mediana; fonte externa vencida vira preço enganoso; proponente faz bait-and-switch; visualização pública de bids vira leilão reverso.

**Correção modular:** propostas privadas, snapshot/aditivo, amostra mínima, contribuição limitada por grafo/par, outliers, fraude/disputa excluídas, fonte/versionamento visíveis, piso legal separado de sugestão, contestação e auditoria. Não punir oferta fora da faixa.

### `AUD-15` — reputação discriminatória ou retaliatória — médio

**Brecha:** requisitos livres podem discriminar; avaliação negativa pode retaliar denúncia ou recusa de mudança de escopo; karma opaco pode retirar renda sem recurso.

**Correção modular:** requisitos apenas de catálogo revisado; atributos protegidos proibidos; ratings bilaterais revelados após ambos avaliarem ou expirar janela; detecção de retaliação; decisões explicáveis; contestação e intervenção humana antes de sanção material.

### `AUD-16` — publicidade baseada em localização e ad fraud — médio

**Brecha:** anunciantes inferem visitas ou grupos sensíveis; executores simulam presença/clique; publicidade parece alerta/quest.

**Correção modular:** segmentação contextual coarse, limiar de agregação, nenhuma trilha individual exportável, visita tratada como estimativa, fraude de impressão/clique, rótulo patrocinado e bloqueio estrutural em navegação/dungeon ativa.

### `AUD-17` — captura do Conselho e sybil — médio

**Brecha:** contas múltiplas, compra de votos, guildas coordenadas e fornecedores interessados controlam roadmap ou skills.

**Correção modular:** elegibilidade/idade de conta, proof-of-personhood proporcional, voto secreto, quórum, concentração e anomalias, conflito de interesse, auditoria pública agregada. Resultado nomeia candidato; nunca concede ativação operacional.

### `AUD-18` — seguro, tributo, nota e registro tratados como detalhe — alto

**Brecha:** uma quest é executável tecnicamente, mas falta seguro, licença, documento fiscal ou cadastro local. Depois do incidente, o histórico prova que a plataforma conhecia e intermediava a atividade.

**Correção modular:** `JurisdictionPolicy` possui obrigações versionadas e owner operacional; `ComplianceEvidence` expira; recibo/nota e responsabilidades entram no acordo; nenhuma cidade/categoria abre sem checklist jurídico, fiscal e de seguro assinado.

## Controles de lançamento obrigatórios

- piloto 18+ e verificado nas duas pontas;
- cidade/categoria fechadas por feature flag e policy versionada;
- endereço exato somente após atribuição;
- lista proibida/restrita e moderação humana;
- nenhuma custódia de dinheiro ou conversão de Gold;
- suporte e incidente em horário do piloto;
- logs com redaction e auditoria de admin;
- kill switch por categoria, região, usuário, provider e feature;
- RIPD de identidade/localização;
- parecer local para transporte, seguro, fiscal e consumidor;
- threat modeling e teste de abuso antes de cada nova modalidade.

## Ordem de correção

1. `PolicyGate`, registry de categoria/jurisdição e kill switches.
2. maioridade/verificação no piloto e revelação progressiva de localização.
3. modalidade/cidade + parecer legal/seguro/pagamento.
4. prohibited-content, evidência, disputa e suporte.
5. antifraude de identidade, assignment, conclusão, reward e payout.
6. somente depois: anúncios, Conselho, RPG, Gold e marketplace.

## Resultado

A modularização atual é compatível com as correções. A regra a acrescentar é: **módulos estendem comportamento, mas policies globais limitam comportamento**. Nenhum schema comunitário, configuração de admin ou módulo novo pode remover um gate de segurança, jurídico, privacidade, idade ou pagamento.
