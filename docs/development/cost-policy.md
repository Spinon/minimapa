# Política de custo zero durante o desenvolvimento

## Regra principal

Enquanto não houver autorização explícita do responsável pelo projeto, o desenvolvimento do Minimapa não pode gerar custo adicional. Integrações pagas ficam arquitetadas, simuladas e documentadas, mas não são ativadas em produção nem contra endpoints faturáveis.

O padrão obrigatório é:

```text
APP_ENV=local
INTEGRATION_MODE=mock
ALLOW_BILLABLE_REQUESTS=false
```

`ALLOW_BILLABLE_REQUESTS=false` funciona como disjuntor. Adaptadores reais devem recusar inicialização ou chamadas potencialmente cobradas quando essa flag estiver desligada.

## Permitido sem nova aprovação

- Supabase local em Docker, banco local e Mailpit/Inbucket local;
- Android Emulator, testes unitários, fixtures e dados sintéticos;
- bibliotecas open source respeitando licenças e políticas de uso;
- mocks, simuladores e gravações sanitizadas sem dados pessoais reais;
- sandbox/demo que não exija cartão, não converta automaticamente em plano pago e possua garantia técnica de cobrança zero;
- free tier somente após confirmar limites, ausência de cobrança excedente automática e mecanismo de bloqueio antes da cota.

## Proibido sem autorização explícita

- cadastrar cartão, ativar billing ou contratar plano/pacote/crédito;
- criar recurso cloud que possa acumular cobrança;
- aceitar trial que se converta automaticamente em assinatura;
- consumir endpoint cobrado de mapas, rotas, navegação, CPF, biometria, assinatura, SMS, email ou IA;
- efetuar transação financeira, mesmo de valor baixo, fora de sandbox/test mode;
- publicar aplicativo ou serviço em infraestrutura paga;
- usar chaves de produção na máquina local ou na CI.

## Arquitetura das integrações

Cada fornecedor implementa um contrato interno com três modos:

1. `mock`: determinístico, local e padrão da CI;
2. `sandbox` ou `demo`: teste manual contra ambiente garantidamente gratuito;
3. `production`: desabilitado e sem credenciais até aprovação.

O domínio não conhece SDKs nem tipos do fornecedor. Feature flags controlam a ativação e um `CostGuard` central valida modo, orçamento e permissão antes de qualquer chamada externa.

## Estratégia por área

| Área | Desenvolvimento sem custo | Preparação para o futuro |
| --- | --- | --- |
| Supabase | stack local em Docker | migrations e configuração portáveis para cloud |
| Mapas | mapa/mock e fixtures; SDK apenas em demo gratuita segura | `MapProvider` intercambiável |
| Rotas/navegação | rotas gravadas e `NavigationFrame` simulado | adaptadores Google/Mapbox atrás de flags |
| CPF/biometria | estados, webhooks e resultados mockados; sandbox do fornecedor | `IdentityVerificationProvider` |
| Assinatura | envelopes e callbacks falsos ou sandbox | `SignatureProvider` |
| Pagamentos | test mode/sandbox e webhooks locais | `PaymentProvider` e ledger independente |
| Email/SMS | Mailpit local e notificações fake | `NotificationProvider` |
| Publicidade | campanhas e métricas sintéticas | adaptador de entrega e faturamento |

Biometria, documentos, CPF ou dados financeiros reais não entram em fixtures, screenshots ou CI. Demos com dados pessoais exigem consentimento do próprio testador e confirmação da política de retenção do fornecedor.

## Gate para ativação futura

Antes de habilitar qualquer serviço faturável, registrar no `TASKS.md`:

- autorização explícita e responsável;
- preço, franquia, moeda, impostos e condição de renovação;
- limite mensal e custo máximo por usuário/quest;
- alertas em 50%, 75%, 90% e 100%;
- bloqueio automático no limite, sem cobrança excedente;
- ambiente, chave restrita e rotação de segredo;
- telemetria de chamadas e atribuição de custo;
- plano de desligamento e fallback;
- revisão de termos, privacidade e retenção.

Uma franquia “gratuita” não é autorização para cobrança. Se o fornecedor não oferecer hard cap ou sandbox realmente isolado, a integração permanece somente em mock.
