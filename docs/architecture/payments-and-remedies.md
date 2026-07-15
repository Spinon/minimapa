# Pagamentos, cancelamentos e remédios

Data-base: 2026-07-15

## Princípios

- pagamento ocorre dentro da experiência do Minimapa;
- um PSP com produto de marketplace recebe, tokeniza, verifica o recebedor e faz repasse/reembolso/chargeback;
- o Minimapa não mantém saldo sacável, não recebe dinheiro em conta própria para repassar e não converte Gold;
- não há take rate sobre a remuneração do executor;
- o solicitante paga exatamente o valor acordado da quest e o executor não paga tarifa do Minimapa;
- o Minimapa absorve tarifas de processamento, payout e disputa do PSP como custo da plataforma;
- estado informado pelo cliente nunca confirma pagamento: somente API server-side e webhook verificado;
- toda integração começa em mock ou sandbox sem cobrança.

“Sem garantia de entrega” significa que o piloto não vende cobertura adicional, indenização própria ou seguro. Isso não elimina cancelamento, reembolso por serviço não prestado, direitos consumeristas nem responsabilidade por falha própria da plataforma. `Minimap Plus` só poderá anunciar seguro após contratação com seguradora/parceiro habilitado, termos claros, cobertura, exclusões, sinistro e regulação aprovados; o Minimapa não se autossegura.

## Provedor primário e fluxo financeiro

O provedor primário escolhido para o piloto é **Stripe Connect**, usando Accounts v2 e uma configuração de contas conectadas em que a plataforma é pagadora das tarifas. O fluxo inicial usa **destination charges**, sem `application_fee`, e transfere ao beneficiário o valor integral acordado. Pix é o primeiro meio planejado; cartões permanecem atrás de feature flag até o fluxo de fraude/chargeback estar validado.

Esta decisão é de arquitetura e sandbox. Conta, credencial, billing e produção não serão ativados sem autorização de custo e validação jurídica/contratual. `MarketplacePaymentProvider` preserva a possibilidade de trocar de PSP.

Mercado Pago Split 1:1 não é o primário porque sua documentação atual desconta a tarifa do Mercado Pago do valor do vendedor, contrariando a regra econômica do Minimapa. Pagar.me permanece candidato de contingência mediante proposta comercial compatível.

Não cobrar solicitante/executor significa:

- preço da quest não recebe adicional ou “taxa de serviço” do Minimapa;
- payout não desconta comissão nem tarifa operacional do Minimapa;
- taxas externas não são escondidas dentro da sugestão de preço;
- custos são registrados em `platform_payment_costs` e subsidiados pela receita/capital da plataforma;
- tributos/retenções legalmente obrigatórios não são tarifa do Minimapa e precisam de tratamento próprio;
- refund ou reversão do principal de uma quest inválida não é taxa: é desfazer o pagamento correspondente, sempre por policy/caso.

Esse subsídio precisa de orçamento e limite. Se a receita futura de publicidade, lojas e microtransações não cobrir o custo, o sistema pausa novos pagamentos; não transfere silenciosamente o custo às partes.

## Contrato neutro de provedor

`MarketplacePaymentProvider` expõe:

- onboarding/KYC de conta recebedora;
- criação e confirmação de pagamento;
- autorização/captura quando suportada;
- consulta de saldo transacional, repasse e falha;
- reembolso vinculado ao instrumento original;
- disputa/chargeback;
- webhook assinado e idempotente;
- reconciliação e relatório.

Na implementação Stripe, usar a versão de API vigente aprovada no início do desenvolvimento (referência atual `2026-02-25.clover`), Accounts v2 e propriedades explícitas de controller/responsabilidade. Não usar tipos legados de conta nem a Charges API. Checkout/PaymentIntent e a superfície mobile serão confirmados no spike oficial antes de escrever o adapter definitivo.

## Estado financeiro

```text
REQUIRES_PAYMENT -> PROCESSING -> PAID -> PAYOUT_PENDING -> PAID_OUT
                         |          |             |
                         v          v             v
                       FAILED   REFUND_PENDING  PAYOUT_FAILED
                                    |
                                    v
                                 REFUNDED

PAID/PAYOUT_* -> DISPUTED -> WON | LOST | PARTIALLY_REFUNDED
```

O pagamento nasce após um `AgreedTermsSnapshot`. O repasse fica pendente até conclusão válida e fim da janela operacional aplicável. Uma disputa aberta congela o repasse quando o PSP permitir. O ledger local é um espelho imutável e reconciliável do PSP, nunca a fonte dos fundos.

Em execução empresarial, contratado, despachante, funcionário executor, meio de transporte e beneficiário do payout são campos distintos. O recebedor vem do acordo e de uma conta aprovada pelo PSP; não é inferido pela pessoa que dirigiu, pelo proprietário do veículo ou por quem operou o painel. Trocar funcionário/asset não troca silenciosamente o beneficiário.

Requisitos técnicos:

- idempotency key por comando e deduplicação de webhook;
- assinatura, timestamp, proteção contra replay e fetch server-to-server do estado crítico;
- valores inteiros em centavos e moeda explícita;
- relação imutável entre quest, acordo, pagamento, recebedor e refund;
- alteração de conta de repasse com MFA, cooldown e alerta fora de banda;
- jobs de reconciliação e fila de exceções;
- logs sem PAN, CVV, token reutilizável ou documento bruto.
- `application_fee=0` e teste contratual garantindo que nenhuma tarifa Minimapa seja criada;
- ledger separado de custos absorvidos pela plataforma, sem descontá-los do payout;
- hard cap financeiro que bloqueia produção quando o subsídio autorizado acabar.

## Cancelamento, no-show, disputa e evidência

Karma não movimenta dinheiro sozinho. A ordem é:

1. registrar o fato e abrir `OperationalCase` quando necessário;
2. coletar evidência proporcional e manifestação das duas partes;
3. aplicar policy de cancelamento/reembolso e decisão revisável;
4. executar refund/payout no PSP;
5. somente então derivar consequências de Karma de um resultado confirmado.

Cada cancelamento registra ator, estado, antecedência, reason code e versão dos termos. Classes iniciais:

- `NO_FAULT`: acordo bilateral, condição externa ou indisponibilidade da plataforma;
- `SAFETY`: risco pessoal, item proibido, divergência grave ou ponto inseguro;
- `REQUESTER_ATTRIBUTABLE`: ausência, endereço/escopo/item divergente ou mudança unilateral;
- `EXECUTOR_ATTRIBUTABLE`: ausência, abandono ou incapacidade previsível;
- `EXCUSED`: emergência, acidente, falha material documentada;
- `ABUSE_SUSPECTED`: padrão que requer revisão, nunca culpa automática.

No-show exige janela de tolerância versionada, check-in, tentativas de contato protegido e sinais múltiplos. Geofence ou GPS isolado não provam presença. Cancelamento por segurança, falha do app, GPS degradado, item/escopo divergente ou credencial invalidada não gera punição automática.

Uma disputa possui SLA, estado, motivo, valor, evidências, contramanifestação, decisão fundamentada e recurso humano. Evidências variam por modalidade: PIN/QR de coleta e entrega, timestamps, acordo/chat, fotos consentidas e minimizadas, coerência de rota e confirmação bilateral. Foto, avaliação ou GPS nunca são verdade absoluta.

## Atendimento e automação

`support_cases` e `operational_cases` são a fonte de verdade no Minimapa. Um `SupportCaseProvider` sincroniza canais externos. Para desenvolvimento sem custo, o primeiro spike será Chatwoot self-hosted local, que oferece API e webhooks; a ativação hospedada fica condicionada a orçamento e operação.

O painel administrativo precisa oferecer:

- fila por segurança, pagamento, entrega, conta, privacidade, bug e dúvida;
- SLA, prioridade, responsável, macros e checklist versionado;
- visão da timeline redigida da quest e acesso separado a evidência sensível;
- suspensão temporária, hold, refund e escalonamento com dupla aprovação quando material;
- resposta, histórico exportável, recurso e pesquisa de resolução;
- integração futura com Consumidor.gov.br após CNPJ, SAC acessível e adesão aprovados.

Automação classifica e sugere; não encerra incidente grave, decide disputa material ou bloqueia renda sem revisão disponível. A LGPD assegura revisão e informação sobre decisões exclusivamente automatizadas que afetem interesses.

## Fontes oficiais/técnicas consultadas

- Stripe Connect: https://docs.stripe.com/connect?locale=pt-BR
- Connect Accounts v2: https://docs.stripe.com/connect/accounts-v2
- tipos de cobrança Connect: https://docs.stripe.com/connect/charges?locale=pt-BR
- Pix com Connect: https://docs.stripe.com/payments/pix
- Mercado Pago Split 1:1: https://www.mercadopago.com.br/developers/pt/docs/split-payments/split-1-1/integration-configuration/integrate-marketplace
- Consumidor.gov.br: https://www.consumidor.gov.br/pages/conteudo/publico/1
- LGPD, art. 20: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm
- Chatwoot: https://www.chatwoot.com/help-center
