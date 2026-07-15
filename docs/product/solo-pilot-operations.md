# Operação factível para uma equipe de uma pessoa

Data-base: 2026-07-15

## Premissa honesta

O projeto possui um único responsável humano. O Codex auxilia durante sessões ativas de desenvolvimento, mas não monitora produção, não recebe incidentes e não funciona como plantão. Portanto, o piloto não promete suporte 24/7 nem permanece aberto sem supervisão.

## Modelo de sessão assistida

- sistema fechado por padrão;
- quests reais só podem ser publicadas em uma `PilotSession` agendada e explicitamente aberta pelo responsável;
- uma zona pequena, participantes convidados e inicialmente no máximo uma quest ativa simultânea;
- abertura somente quando o responsável estiver disponível no painel/admin e com telefone funcional;
- encerramento bloqueia novas publicações/aceites, preservando o acompanhamento das quests já ativas;
- fora da sessão, desenvolvimento usa pessoas, GPS, pagamento e navegação simulados.

Não existe SLA comercial. O app informa “piloto assistido”, janela da sessão e atendimento em melhor esforço. Emergência física deve acionar serviços públicos competentes; o Minimapa não é serviço de emergência.

## Checklist mínimo de abertura

1. confirmar kill switches, backup e acesso administrativo com MFA;
2. confirmar participantes, identidade manual e contatos protegidos;
3. confirmar zona, clima/condições locais e modalidade permitida;
4. manter pagamento real desligado até PSP/compliance aprovados;
5. testar publicação, aceite, chat, cancelamento, PIN e fallback de navegação;
6. abrir `PilotSession` com horário de encerramento automático;
7. acompanhar a única quest ativa pela timeline;
8. fechar a sessão e registrar resultado, bugs, incidentes e aprendizados.

## Resposta de uma página

| Evento | Ação imediata | Depois |
| --- | --- | --- |
| risco físico/acidente | orientar acionamento de emergência; interromper quest; preservar privacidade | abrir caso e congelar expansão |
| item divergente/proibido | não coletar/entregar; afastar-se se necessário; cancelar por segurança | bloquear categoria/conta para revisão |
| item perdido/danificado | abrir disputa; preservar termos e evidência proporcional | decidir manualmente, sem Karma automático |
| no-show | aguardar tolerância configurada e tentar contato protegido | classificar motivo e decidir caso |
| fraude/pagamento | congelar payout quando possível; bloquear ação financeira | reconciliar PSP e evidência |
| conta tomada | revogar sessões, bloquear payout/endereço e iniciar recuperação | revisar logs e restaurar com step-up |
| app/mapa indisponível | usar fallback externo ou cancelar sem culpa | registrar bug e manter feature fechada |
| pedido de autoridade | não improvisar entrega de dados; preservar pedido e identidade do solicitante | buscar orientação jurídica antes de responder, salvo emergência legal clara |

## Critérios de parada

Fechar imediatamente novas quests quando houver incidente físico grave, item proibido, falha de localização/identidade/pagamento, perda de acesso administrativo, mais de um caso sem capacidade de resposta ou qualquer dúvida jurídica material. Reabrir somente após registrar causa, correção/mitigação e decisão explícita.

## Evolução

Somente ampliar simultaneidade, zona ou horário após várias sessões sem incidentes graves, métricas aceitáveis e capacidade real de suporte. Antes de beta contínuo, será necessária equipe/fornecedor de operação e suporte compatível; automação não substitui responsável humano em decisões materiais.
