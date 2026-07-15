# Inventário de integrações e unidades de custo

Este inventário é o registro técnico mínimo antes da criação de qualquer adaptador externo. Valores comerciais, franquias e termos mudam; por isso, devem ser confirmados na documentação oficial e anexados ao gate de aprovação imediatamente antes de cada ativação. Até lá, todos os fornecedores permanecem em `mock` e `ALLOW_BILLABLE_REQUESTS=false`.

| Serviço | Contrato interno | Unidade que pode gerar cobrança | Desenvolvimento atual | Hard cap antes de produção | Fallback seguro |
| --- | --- | --- | --- | --- | --- |
| Autenticação | `AuthGateway` (planejado) | usuário ativo mensal, usuário social/terceiro e MFA avançado conforme plano | Supabase local, Mailpit e provider Google mockado | nenhum projeto cloud/SMTP até orçamento, alertas e política de excedente aprovados | login local de teste; produção bloqueia com indisponibilidade explícita |
| Mapas e busca | `MapProvider` (planejado) | carregamento de mapa, usuário ativo, busca/geocoding | fixtures locais | limite de chamadas no backend e orçamento do fornecedor verificados | mapa simplificado/cache ou indisponibilidade explícita |
| Rota e navegação | `NavigationProvider` | rota, viagem ou usuário ativo conforme produto | `SimulatedNavigationProvider` | `CostGuard`, quota própria por ambiente e kill switch | rota simulada em teste; app externo ou pausa segura em operação |
| Identidade | `IdentityVerificationProvider` | sessão/verificação e eventual revisão humana | `SimulatedIdentityVerificationProvider` | limite diário/mensal e nenhuma repetição sem idempotência | fila `REVIEW_REQUIRED`, sem liberar execução |
| Pagamento | `PaymentProvider` | transação, método, payout, refund e chargeback | `SimulatedPaymentProvider` | somente test mode; limites e alertas antes de transação real | bloquear checkout e preservar acordo, nunca fingir pagamento |
| Notificação | `NotificationProvider` | mensagem, email, SMS ou push conforme canal | `SimulatedNotificationProvider` | quota por canal e bloqueio de SMS/email pagos | caixa local/in-app e retry controlado |
| Suporte | `SupportProvider` | agente, ticket, automação ou uso mensal | `SimulatedSupportProvider` | plano sem conversão automática e limite de seats | painel/admin e caso local |
| Assinatura eletrônica | `SignatureProvider` (planejado) | envelope, assinatura ou verificação | sem adaptador; somente desenho | quota mensal e expiração sem renovação automática | aceite interno quando juridicamente suficiente ou bloqueio |

## Regras de implementação

- Todo contrato recebe chave de idempotência quando a operação produz efeito externo.
- Falha externa retorna `Success`, `RetryableFailure` ou `PermanentFailure`; exceções e tipos do fornecedor não atravessam a porta.
- Retries têm limite, backoff e telemetria. Cobrança não pode ser repetida por retry.
- Chaves de produção são proibidas em builds locais, previews e CI.
- O adaptador chama `CostGuard` antes de rede. O simulador não possui cliente HTTP nem SDK de fornecedor.
- O principal da quest, o custo operacional e o total cobrado permanecem campos separados em todo o fluxo financeiro.

## Checklist por fornecedor

Antes de sair de `mock`, criar uma entrada datada com: página oficial de preços, unidade exata, moeda/impostos, franquia, política de excedente, hard cap real, alertas, retenção de dados, ambiente sandbox, credencial restrita, responsável, orçamento aprovado, teste de desligamento e fallback exercitado.
