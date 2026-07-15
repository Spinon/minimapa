# Karma, casos e evidências

Data-base: 2026-07-14

## Regra central

Karma é memória reputacional derivada de resultados auditáveis; não é juiz, prova nem saldo financeiro. Ratings opinativos, sinais automáticos, incidentes alegados e decisões confirmadas permanecem separados. Uma acusação não reduz score antes de apuração proporcional.

```text
eventos/evidências -> caso -> resultado revisável -> ledger de Karma -> score contextual
                                      |                    |
                                      v                    v
                              refund/payout policy   elegibilidade explicável
```

## Domínios

- `OperationalCase`: cancelamento, no-show, disputa, incidente, item perdido, fraude ou segurança.
- `EvidenceArtifact`: referência, origem, hash, retenção, acesso e cadeia de custódia; conteúdo sensível fica em vault separado.
- `CaseDecision`: fatos considerados, policy/versão, conclusão, responsável e recurso.
- `KarmaLedgerEntry`: evento imutável positivo, neutro ou negativo derivado de resultado confirmado.
- `KarmaProjection`: visão calculada por papel, modalidade, categoria e dimensão.
- `Rating`: opinião bilateral, revelada após ambos avaliarem ou a janela expirar.

## Dimensões contextuais

- confiabilidade/comparecimento;
- pontualidade;
- precisão do escopo ou conformidade do item;
- comunicação;
- cuidado e segurança;
- qualidade, somente quando a modalidade permitir avaliação responsável.

Solicitante e executor têm projeções separadas. Reputação em entrega não comprova encanamento; rating não substitui diploma, CNH ou outra credencial. Scores usam prior conservador, tamanho de amostra, confiança, recência e categoria. A interface mostra faixa/nível e motivos úteis, não uma falsa precisão de duas casas decimais.

## Robustez e justiça

- rating bilateral cego reduz retaliação;
- uma mesma dupla tem influência limitada;
- detecção de contas/dispositivos/rotas relacionados aponta conluio sem decidir culpa;
- eventos positivos e negativos são idempotentes e reversíveis por lançamento compensatório;
- políticas e pesos são versionados; recalcular não apaga a visão histórica usada numa decisão;
- conteúdo de denúncia não é exibido ao acusado quando isso comprometer segurança;
- sanções materiais têm motivo, duração, canal de recurso e revisão humana disponível;
- nenhuma taxa de aceite existe e quests ignoradas/recusadas não entram no Karma;
- atributos protegidos, preço fora da sugestão e recusa de aditivo não entram no score.

## Consequências graduais

Um resultado confirmado pode gerar, conforme gravidade e reincidência:

1. orientação ou aviso;
2. exigência de reverificação/treinamento;
3. limite temporário de valor, categoria ou simultaneidade;
4. cooldown;
5. revisão especializada;
6. suspensão proporcional;
7. banimento somente para fraude, risco grave ou repetição comprovada, com trilha e recurso quando cabível.

Casos graves podem exigir medida cautelar imediata para proteger usuários; ela é registrada como cautelar, tem prazo e revisão, e não equivale a decisão de mérito.

## Evidência por etapa de entrega

- **antes da coleta:** snapshot do item declarado, restrições, identidade/veículo e terms;
- **coleta:** PIN ou QR de uso único, horário, área aproximada e confirmação de conformidade;
- **trânsito:** telemetria mínima durante a finalidade ativa, sem exigir trilha contínua como prova exclusiva;
- **entrega:** PIN do destinatário ou alternativa acessível, confirmação e exceção documentada;
- **pós:** janela de disputa, anexos redigidos e manifestação bilateral.

Fotos não devem conter pessoas, documentos, interiores ou endereço quando outra evidência basta. Metadados são minimizados, malware é bloqueado e acesso administrativo é auditado.

## Antifraude

Sinais incluem repetição artificial de pares, quests circulares, velocidade impossível, GPS incompatível, device graph, chargeback, conta de repasse recém-alterada, muitas conclusões em janela curta e evidência reutilizada. Eles alimentam risco e revisão; não concedem ao cliente nem ao modelo automático poder de condenar.

XP, Gold e sugestão de preço ficam pendentes ou excluídos durante disputa/fraude. Uma reversão usa eventos compensatórios nos ledgers correspondentes. O RPG jamais aumenta Karma profissional.
