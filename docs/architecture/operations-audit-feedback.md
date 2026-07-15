# Operação, auditoria e feedback

Data-base: 2026-07-14

## Auditoria interna

O audit log é append-only e separado de observabilidade técnica. Cada evento contém identificador, instante, ator/sessão, sujeito, ação, reason code, policy/versão, correlação e resumo redigido da mudança. Payloads completos, documentos e coordenadas exatas não são copiados para o log.

Eventos obrigatórios incluem:

- login de risco, verificação e mudança de identidade/veículo;
- leitura administrativa de documento, evidência ou localização;
- publicação, proposta, acordo e transição de quest;
- decisão de policy, moderação, disputa e recurso;
- criação/alteração de pagamento, recebedor, refund, payout e reconciliação;
- mudança de configuração, role, feature flag, zona e kill switch;
- concessão, reversão e ajuste manual de Karma/XP/Gold.

Controles:

- RBAC de mínimo privilégio, MFA/step-up e sessões administrativas curtas;
- motivo obrigatório e dupla aprovação para ações financeiras ou de alto impacto;
- partição/arquivamento, retenção por classe e legal hold;
- hash/assinatura ou armazenamento WORM quando a escala justificar;
- alertas para consulta em massa, exportação e comportamento administrativo anômalo;
- acesso do usuário ao histórico relevante sem expor antifraude ou terceiros.

## Bugs e sugestões

`FeedbackItem` é um domínio próprio com tipos `BUG`, `SUGGESTION`, `ACCESSIBILITY`, `SAFETY` e `LEGAL`. Denúncia contra usuário, disputa financeira e vulnerabilidade de segurança usam canais próprios e privados, embora possam ser vinculadas.

Campos mínimos:

- relato, passos e expectativa;
- app/build, SO, modelo e estado de conectividade;
- screenshot/anexo somente com consentimento e redaction;
- logs diagnósticos opt-in, sem tokens, chat, documento ou localização exata por padrão;
- severidade, impacto, duplicata, owner, estado, resolução e release vinculada;
- consentimento para contato e notificações de andamento.

Fluxo administrativo:

`NEW -> TRIAGED -> PLANNED | IN_PROGRESS | NEEDS_INFO -> RESOLVED | DECLINED -> VERIFIED`

Admins veem filas, busca, deduplicação, labels, métricas, comentários internos, histórico e vínculo com tarefas/releases. Usuários recebem protocolo e podem complementar o relato. Sugestões públicas futuras precisam de moderação; dados privados nunca aparecem num board comunitário.

## Arquitetura de integração

- `FeedbackRepository` mantém o registro canônico;
- `IssueTrackerProvider` permite sincronizar GitHub/Jira/Linear depois, sem exigir ferramenta paga;
- `SupportCaseProvider` integra atendimento, inicialmente com spike local do Chatwoot;
- webhooks entram por inbox idempotente e uma outbox publica mudanças;
- anexos são verificados, privados, expiram e têm acesso auditado.

## Métricas operacionais

- tempo de triagem e resolução por severidade;
- backlog, idade e reabertura;
- duplicatas e incidência por versão/dispositivo;
- bugs por 100 quests concluídas;
- volume de segurança/legal e tempo até contenção;
- sugestões promovidas a experimento/release;
- satisfação pós-atendimento, sem contaminar Karma da quest.
