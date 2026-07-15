# Revisão de produto e recorte de escopo

Data da revisão: 2026-07-14

## Parecer executivo

A visão é diferenciada e coerente: quests geram atividade econômica e social; o minimapa é a interface persistente; fantasia, progressão e mundo geográfico criam retenção; publicidade e lojas monetizam sem retirar percentual do executor.

O risco principal é tentar validar simultaneamente quatro produtos:

1. marketplace local de transporte e serviços;
2. navegação curva a curva própria;
3. mídia local e marketplace de lojas;
4. RPG geolocalizado com economia e governança.

O norte deve permanecer documentado, mas a implementação precisa validar uma cunha estreita. Sem liquidez e confiança no marketplace, AR, dungeons, Gold, Conselho e lojas não possuem base econômica.

## Recomendação para o primeiro piloto

### Cunha sugerida

Uma única cidade, Android, adultos verificados e **entrega urbana de pequenos itens permitidos e de baixo valor**, sem passageiros, dinheiro em espécie, produtos regulados, alimento com cadeia especial, animais, chaves, documentos sensíveis ou bens de alto risco. O enquadramento municipal, fiscal e de transporte precisa ser validado antes de confirmar essa escolha.

Por que começar aqui:

- valida publicação, descoberta, aceite/contraproposta, deslocamento, navegação, tracking, chegada e conclusão;
- preserva o diferencial navigation-first;
- evita inicialmente o risco corporal e regulatório adicional de transportar passageiros;
- evita a taxonomia, diagnóstico, credenciais e disputa de escopo de dezenas de serviços domésticos;
- permite operação manual em raio pequeno e conjunto fechado de participantes.

Se a análise local tornar esse recorte desproporcionalmente oneroso, a alternativa é um único serviço presencial de baixo risco e escopo padronizado, mantendo navegação até o local, mas sem ativar um catálogo genérico.

### MVP operacional

- Android apenas;
- uma cidade e raio operacional explícito;
- uma modalidade e uma definição de quest;
- solicitante e executor maiores de 18 anos e verificados no piloto;
- oferta do autor, aceite integral ou contraproposta privada;
- endereço aproximado antes da atribuição e exato somente quando necessário;
- mural, mapa, rota, navegação 2D, voz, tracking e estados essenciais;
- confirmação de coleta/início e entrega/conclusão com política de evidência;
- chat mínimo, bloqueio, denúncia, contato de emergência e suporte humano;
- cancelamento, no-show, disputa e compensação definidos;
- avaliação bilateral com contestação;
- pagamentos fora da plataforma apenas se o risco e a comunicação forem explicitamente aceitos no piloto; nenhum fundo custodiado pelo Minimapa;
- métricas e revisão semanal da operação.

## O que está faltando e tem alto retorno

### P0 — antes de construir o fluxo completo

1. **Escolha de cidade e modalidade:** hoje várias decisões críticas permanecem abertas. Sem elas não há regra jurídica, mapa de oferta, custo, seguro ou operação concreta.
2. **Service blueprint operacional:** definir quem atende fraude, acidente, item perdido, no-show, disputa, conta tomada e pedido policial, inclusive fora do horário.
3. **Matriz categoria × território × risco:** uma categoria só pode ser publicada, atribuída e concluída se todos os gates aplicáveis estiverem ativos e vigentes.
4. **Modelo de pagamento do piloto:** decidir se é fora da plataforma ou por PSP licenciado em sandbox/produção autorizada. “Depois vemos” deixa disputa e fraude sem dono.
5. **Política de endereço e encontro:** aproximado na descoberta, revelação progressiva, pontos seguros e prazo de retenção.
6. **Regras de cancelamento/no-show:** separar desistência legítima, risco, falha de veículo, atraso, item divergente e abuso recorrente. Cancelar por segurança não pode prejudicar karma.
7. **Lista de itens/serviços proibidos e restritos:** com fluxo de moderação e retirada emergencial.
8. **Termos acordados e evidência:** o que prova coleta, escopo, entrega e aceite sem transformar GPS ou foto em verdade absoluta.
9. **Seguro e resposta a incidentes físicos:** determinar cobertura mínima e responsabilidade por modalidade antes do piloto.
10. **Hipótese de liquidez:** número mínimo de executores verificados por zona/horário e tempo máximo aceitável até primeira proposta.

### P1 — para o beta fechado

- fila operacional de revisão manual e console mínimo de suporte;
- recuperação segura de conta e step-up para endereço, payout e documentos;
- notificações com fallback e proteção contra conteúdo sensível na tela bloqueada;
- acessibilidade, modo de baixa conectividade e canal alternativo de emergência;
- recibo/snapshot do acordo e histórico exportável;
- política de indisponibilidade do SDK de navegação;
- painel semanal de funil, segurança, fraude e qualidade;
- pesquisa curta após publicação abandonada, aceite, cancelamento e conclusão;
- ferramentas de supply activation: convites controlados, agenda e zonas — sem punição por recusar quests.

## O que podar ou postergar

| Item | Recomendação | Motivo |
| --- | --- | --- |
| AR de navegação | manter contratos, postergar renderer | alto risco de distração e pouco valor antes da navegação 2D funcionar |
| Dungeons/party | manter arquitetura, nenhuma produção antes do core | abre segurança física, menores, moderação e anti-spoofing |
| Gold/IAP/equipamentos | postergar integralmente | adiciona política de lojas, fraude e economia antes de retenção comprovada |
| Conselho com ativação automática | substituir por nomeação + gate humano | votação não avalia legalidade, seguro ou risco |
| Marketplace de lojas/pagamento/entrega | postergar | é outro produto com KYC, estoque, consumidor, fiscal e chargeback |
| iOS e web consumidor | postergar | Android já é suficiente para validar o fluxo; web pode surgir primeiro só para operação |
| Catálogo amplo de serviços | postergar | destrói foco, liquidez e capacidade de suporte |
| Importadores de referências de preço | usar cadastro manual no piloto | automação não é necessária antes de volume e categoria definirem a fonte útil |
| Biometria/assinatura pagas | manter adaptadores/mock, usar revisão manual no piloto | custo e abandono sem demanda comprovada |
| Ads self-service | postergar; no máximo piloto manual | inventário sem audiência não tem valor; aumenta moderação e privacidade |
| Gamificação complexa | avatar simples e XP opcional apenas após fluxo confiável | incentivos prematuros ampliam farming e distraem da utilidade |
| Engine genérica completa | implementar contrato + módulo real + fixture | provar fronteira sem construir renderer universal ou módulos vazios |

## Sequência recomendada

### Gate A — decisão de operação

Cidade, modalidade, público, território, proibições, pagamento, seguro e parecer jurídico. Nenhuma feature de crescimento antes disso.

### Gate B — walking skeleton local

Publicar → descobrir → aceitar/contra-ofertar → navegar simulado → iniciar → concluir → avaliar, com um módulo e dados locais.

### Gate C — piloto fechado assistido

20–50 solicitantes, oferta recrutada manualmente, área pequena, verificação manual, suporte em horário restrito e zero dinheiro custodiado pelo Minimapa.

### Gate D — beta medido

Somente após provar segurança, taxa de match, tempo até proposta, conclusão e suporte. Ativar PSP ou fornecedor externo apenas com autorização de custo e compliance.

### Gate E — expansão

Segunda modalidade prova o `QuestModuleContract`; depois entram publicidade manual e progressão leve. RPG, governança e lojas ficam condicionados a métricas próprias e nova auditoria.

## Métricas e critérios de parada

Além do funil atual, medir:

- quests sem nenhuma proposta por zona/horário;
- concentração de oferta e repetição de pares;
- cancelamentos por segurança, item/escopo divergente e preço;
- denúncias, incidentes físicos e contas suspensas por 100 quests;
- tempo de resposta e resolução do suporte;
- contrapropostas aceitas e diferença para oferta original;
- falsos positivos/negativos de verificação e antifraude;
- quilômetros vazios do executor e duração total, não só rota da quest;
- retenção separada de solicitante e executor;
- custo operacional manual por quest concluída.

Parar expansão quando houver incidente grave sem runbook, mais disputas que a operação consegue tratar, liquidez insuficiente recorrente, custo variável sem caminho de receita ou exigência regulatória ainda não atendida.

## Decisão recomendada

Preservar toda a visão como roadmap, mas congelar o MVP em `QuestCore + MovementModule + uma definição + Android + uma cidade + adultos verificados + suporte manual`. O próximo trabalho de produto não é adicionar features: é fechar as decisões de Fase 0 e escrever o service blueprint do piloto.
