# TASKS — marketplace de quests

Última atualização: 2026-07-14

Este arquivo é a fonte de verdade do planejamento. Ele deve ser atualizado em toda sessão de desenvolvimento que altere escopo, arquitetura, status ou decisões.

### Regra operacional de custo

Nenhuma tarefa autoriza gasto. Todo serviço externo começa em `mock` ou sandbox/demo com cobrança tecnicamente impossível, usando `ALLOW_BILLABLE_REQUESTS=false`. Não cadastrar cartão, ativar billing, aceitar trial com conversão automática ou consumir cota que possa gerar excedente sem autorização explícita registrada. A implementação deve deixar portas/adaptadores, configuração e testes prontos para ativação futura. Detalhes em `docs/development/cost-policy.md`.

## Como manter este arquivo

- `[ ]` pendente
- `[~]` em andamento
- `[x]` concluído e verificado
- `[!]` bloqueado — registrar o motivo ao lado
- Uma tarefa só é concluída quando seus critérios de aceite e verificações relevantes passaram.
- Novas decisões entram no log no final do arquivo; não apagar decisões antigas, marcar como substituídas.

## 1. Visão do produto

Criar um marketplace mobile/web inspirado no fluxo do Uber, com identidade medieval/fantasy:

1. Um usuário cria e publica uma **quest** com origem, destino, horário, descrição e recompensa/preço.
2. Motoristas elegíveis visualizam quests próximas em um **mural da guilda**.
3. Um motorista aceita a quest; a aceitação precisa ser atômica para impedir dois aceites.
4. As partes acompanham status e localização em tempo real.
5. A quest é iniciada com confirmação segura, concluída e avaliada.

A fantasia deve dar personalidade ao produto sem esconder informações de segurança. Endereço, preço, identidade, veículo, status da corrida e ações de emergência continuam explícitos e convencionais.

### Tese central: o minimapa é o produto

O produto é **mobile-first e navigation-first**. As quests organizam o motivo da jornada, mas o diferencial é manter o usuário dentro de uma interface de minimapa própria durante o deslocamento. A primeira versão entrega navegação curva a curva embutida em 2D; a arquitetura deve permitir que o mesmo estado de navegação alimente, no futuro, uma visualização em realidade aumentada.

### Modos da experiência

O aplicativo possui interfaces de mapa com objetivos e densidades diferentes:

1. **Mapa-mundo / exploração:** busca de conteúdo, lojas, lugares, quests, avatares, pins patrocinados, filtros e planejamento. É a superfície social, comercial e de descoberta.
2. **Modo corrida / quest ativa:** curva a curva, rota, posição, próxima manobra, faixas, voz, velocidade, ETA e status essencial da quest. Lojas, publicidade, recompensas e conteúdo de exploração ficam ocultos.
3. **Modo dungeon:** experiência lúdica para usuários parados ou a pé, com lobby, party e encontro. Fica indisponível durante uma quest ativa e nunca aparece sobre a navegação veicular.

Fluxo principal:

`exploração → detalhe da quest → aceite/confirmação → modo corrida → chegada/conclusão → exploração`

A mudança de modo precisa ser explícita, rápida e reversível quando seguro. O motor de mapa pode ser compartilhado, mas cada modo tem seu próprio conjunto de camadas, controles, câmera e regras de interação.

### Hipótese do MVP

Começar em uma única cidade/região e com **um só tipo de quest ponto A → ponto B**. Antes da implementação, escolher se o primeiro caso será transporte de passageiros, entrega de itens ou pequenos serviços. Misturar os três no primeiro MVP aumenta regras, telas, operação e risco jurídico.

A revisão de produto recomenda como cunha, condicionada à validação local, entrega urbana de pequenos itens permitidos e de baixo valor: Android, Rio Claro e zonas graduais, adultos verificados, sem passageiros, dinheiro em espécie, produtos regulados ou bens de alto risco. A visão completa permanece no roadmap, mas AR, RPG, Conselho, Gold, lojas, IAP e expansão de categorias não entram antes de liquidez, confiança e operação do núcleo. Detalhes em `docs/product/product-scope-review.md`.

O território do piloto foi definido em Rio Claro/SP, com área técnica máxima de 50 km e abertura gradual por zonas menores. A legislação é resolvida por origem, destino, rota e vigência porque o raio cruza municípios. O desenho operacional está em `docs/product/rio-claro-pilot.md`.

### Quest como plataforma extensível

O MVP valida deslocamento, mas `Quest` é um contrato genérico de trabalho e colaboração. O núcleo contém publicação, requisitos, matching, atribuição, execução, recompensa, eventos e avaliação. Dados específicos entram por módulos tipados, como `MovementQuest` e `ServiceQuest`, sem presumir que toda quest possui motorista, veículo, origem e destino.

Extensibilidade é uma invariável: uma nova definição de serviço entra por dados/schema sem alteração da engine; uma modalidade nova implementa um `QuestModuleContract` versionado sem editar regras internas do `QuestCore`. Capabilities compostas substituem condicionais globais por tipo.

Uma quest de serviço pode exigir nível mínimo, karma contextual, skills e credenciais verificadas. A elegibilidade é calculada no servidor por regras tipadas, versionadas e explicáveis. Skills autodeclaradas, reputação derivada de avaliações e diplomas/certificados verificados possuem graus de confiança diferentes e não podem ser tratados como equivalentes.

Cada skill possui XP e proficiência próprios. Concluir uma quest concede XP somente às skills canônicas mapeadas e versionadas na definição daquele serviço. O perfil materializa apenas skills nas quais o jogador já recebeu XP ou possui comprovação/credencial, evitando milhares de trilhas vazias.

O **Conselho do Reino** permite propor e votar mensalmente no próximo serviço. O vencedor que couber no workflow genérico gera automaticamente uma definição candidata orientada a schema, ainda não executável. Toda categoria aguarda gate de compliance proporcional, com revisão especializada quando regulada ou de alto risco. Votação nunca injeta código, cria skills livres diretamente ou autoriza produção.

Somente identidades verificadas podem candidatar-se, aceitar, executar ou concluir quests. Contas não verificadas podem publicar quests de baixo risco com o aviso **Identidade do solicitante não verificada**, mas a atribuição e a execução permanecem bloqueadas até o solicitante também concluir a verificação. Verificação de identidade, credencial profissional, karma e MFA são sinais independentes.

No primeiro piloto fechado, aplica-se política mais restritiva: somente maiores de 18 anos com identidade verificada publicam ou executam. A publicação limitada por não verificados permanece como capacidade futura, desabilitada por feature flag até a operação comprovar segurança.

O detalhamento está em `docs/architecture/quest-engine.md` e `docs/architecture/community-governance.md`.

Cancelamento, no-show, disputa e evidência são tratados por casos operacionais. Karma é uma consequência contextual de resultados confirmados, nunca juiz automático nem comando de payout/refund. Recusar ou ignorar uma quest não gera penalidade. Detalhes em `docs/architecture/karma-cases-and-evidence.md`.

### Gate público de política

Módulos estendem comportamento, mas policies globais limitam comportamento. Um `PolicyGate` server-side reavalia publicação, revelação de localização, candidatura, aceite, atribuição, início, conclusão, recompensa e pagamento. Categoria, jurisdição, idade, identidade, credencial, veículo, localização, conteúdo, evidência, consumidor e pagamento retornam `ALLOW`, `REQUIRE_REVIEW` ou `DENY` com versão e motivo auditáveis.

O Conselho do Reino pode nomear a próxima definição candidata, mas não ativá-la em produção. `ACTIVE_BETA` exige aprovação jurídica/segurança humana e versionada; nenhuma votação, schema ou admin genérico remove policy global. A auditoria completa está em `docs/security/public-risk-audit.md`.

Para quests de transporte, a criação apresenta uma **faixa de valor sugerido** baseada em quests comparáveis concluídas nos 30 dias anteriores. A referência usa mediana robusta de valor líquido por quilômetro, segmentação geográfica/operacional, tamanho mínimo de amostra e confiança explícita. O autor pode editar o valor; a plataforma não impõe tarifa nem recebe percentual.

Para prestação de serviços, o cold start usa pedido de orçamento: escopo estruturado, orçamento opcional do autor e propostas com mão de obra, prazo, deslocamento e materiais separados. Não existe “valor-base da plataforma” antes de haver amostra suficiente. Futuras faixas históricas comparam somente a mesma definição/versionamento de serviço, região e escopo compatíveis.

O autor publica o que precisa e quanto pretende pagar. Uma pessoa ou empresa elegível pode aceitar os termos como publicados ou enviar contraproposta privada com preço, prazo, materiais e eventual ajuste explícito de escopo. O autor escolhe; o aceite atômico congela um snapshot do acordo e encerra as demais propostas. Referências de preço apenas informam a negociação.

Fontes oficiais ou profissionais podem complementar o cold start quando forem aplicáveis: custos SINAPI, pisos regulatórios de frete, tarifas municipais e honorários indicativos, por exemplo. Cada referência permanece separada do histórico do Minimapa e exibe publicador, território, competência, metodologia e natureza. Sem fonte aplicável nem amostra interna, não há sugestão. Detalhes em `docs/architecture/external-price-references.md`.

### RPG geolocalizado

O avatar também será um personagem jogável. Dungeons versionadas podem aparecer em pontos seguros do mapa de exploração; o usuário se aproxima a pé, entra em uma party e participa de encontros cooperativos. As recompensas possíveis são XP de jogo, Gold, cosméticos e equipamentos com efeito exclusivamente lúdico.

Progressão profissional e progressão de jogo são domínios distintos, ligados por uma ponte de mão única: **Quests → RPG**. Uma quest real concluída e validada pode conceder XP do avatar, Gold, cosméticos ou progresso lúdico; dungeon não comprova encanamento, não concede karma, credencial, elegibilidade, prioridade ou vantagem econômica em quests reais. Durante `ActiveQuestMode`, dungeons, convites e recompensas ficam desmontados e toda interação é bloqueada. O desenho completo está em `docs/architecture/location-rpg.md`.

### Métricas iniciais

- Percentual de quests publicadas que recebem aceite.
- Tempo mediano até o primeiro aceite.
- Percentual de quests aceitas que são concluídas.
- Cancelamentos por usuário e por motorista.
- Usuários e motoristas ativos por semana.
- Custo de mapas/rotas por quest concluída.
- Receita publicitária por região e por mil impressões qualificadas.
- Conversão de ponto patrocinado para abertura de detalhe, rota ou visita, sem inferir visita quando a precisão não permitir.
- Retenção semanal por nível e progressão cosmética.
- Percentual de receita que não depende de qualquer desconto sobre a remuneração do motorista.

### Modelo de negócio e princípios econômicos

- **Sem take rate do motorista:** o Minimapa não recebe percentual sobre o valor da quest. Custos de processamento eventualmente cobrados por terceiros devem ser transparentes e separados da receita do Minimapa.
- **Pagamento in-app sem custódia:** um PSP de marketplace processa checkout, KYC, repasse, refund e chargeback; o Minimapa mantém somente ledger reconciliável e não converte Gold. Detalhes em `docs/architecture/payments-and-remedies.md`.
- **Receita principal:** empresas pagam por presença destacada, campanhas locais e pontos de interesse patrocinados.
- **Receita digital:** cosméticos opcionais para avatar e personalização do minimapa, sem vantagem funcional sobre outros usuários.
- **Marketplace futuro:** empresas abrem “lojas” temáticas, vendem bens/serviços reais e podem contratar entrega por quests. A receita pode vir do lojista, publicidade, software e processamento — não do ganho do motorista.
- **Progressão para todos:** XP e níveis são conquistados por participação segura e útil; dinheiro não compra nível, prioridade de aceite ou reputação.

### Camadas de evolução

1. **Núcleo confiável:** navegação, quests, segurança e operação.
2. **Identidade RPG:** avatar, XP, níveis, títulos e recompensas cosméticas.
3. **Publicidade local:** empresas e lugares patrocinados no modo de exploração, busca, mural e chegada.
4. **Economia digital:** loja de cosméticos e moeda virtual fechada.
5. **Marketplace:** lojas de comerciantes, catálogo, pedidos, pagamento, entrega e pós-venda.

### Regras da experiência patrocinada

- Todo conteúdo pago aparece identificado como **Patrocinado**.
- Publicidade não imita quest, alerta de trânsito, perigo, botão do sistema ou instrução de navegação.
- Durante navegação ativa, não exibir anúncios, animações ou pins patrocinados que concorram com manobra, faixa, velocidade, posição ou ETA.
- Priorizar contexto geográfico e intenção atual, evitando segmentação por dados sensíveis ou histórico preciso desnecessário.
- Limitar frequência e densidade para o mapa continuar sendo uma ferramenta, não um mural publicitário.
- Métricas de campanha devem usar agregação e limiares de privacidade; nunca vender trilhas individuais de localização.
- Ao entrar no modo corrida, todas as camadas comerciais e de descoberta são removidas da árvore de renderização, não apenas escondidas visualmente por outro painel.

### Progressões e economias separadas

1. **XP/nível global:** ganho por participação segura; não transferível, não comprável e sem valor monetário.
2. **XP profissional:** ganho por quests reais e credenciais verificadas, separado do RPG.
3. **XP de jogo:** ganho em dungeons e eventos, usado apenas pelo avatar jogável.
4. **Gold:** moeda virtual fechada para cosméticos e itens digitais permitidos. Não pode ser sacada, transferida ou usada para pagar produtos físicos.
5. **Chaves de Aventura:** franquia diária/semanal/mensal não transferível para entradas com recompensa; não é moeda e, inicialmente, não pode ser comprada.
6. **Dinheiro real:** pagamentos de publicidade e marketplace ficam em ledgers e provedores próprios, separados de XP e Gold.

Gold não deve esconder o preço real. Uma compra futura deve mostrar quantidade, preço em moeda local, equivalência dos itens, saldo, histórico e política de reembolso. Para lojistas, preferir uma **Licença de Guilda** ou plano comercial com preço transparente em um portal B2B, em vez de misturar capital da loja com Gold.

## 2. Escopo funcional do MVP

### Usuário que publica a quest

- Cadastro/login e perfil.
- Definição de origem e destino por busca, mapa ou localização atual.
- Prévia de rota, distância e duração estimadas.
- Categoria, observações, horário e recompensa/preço.
- Publicação, acompanhamento e cancelamento conforme as regras.
- Visualização do motorista, veículo, ETA e posição durante a execução.
- Histórico, recibo simples e avaliação.

### Motorista

- Cadastro, perfil, documentos e veículo.
- Estado online/offline.
- Mural/lista de quests próximas com distância até a origem e recompensa.
- Detalhe e aceite de uma quest ainda disponível.
- Rota até a origem e depois até o destino.
- Mudanças de status: a caminho, chegou, iniciada e concluída.
- Histórico e resumo de ganhos.

### Operação/administração

- Busca de usuários, motoristas e quests.
- Aprovação/bloqueio de motorista.
- Visualização da linha do tempo de eventos de uma quest.
- Cancelamento administrativo e registro de motivo.
- Tratamento básico de denúncias/disputas.
- Cadastro manual de anunciante, campanha, local patrocinado, período e orçamento.
- Auditoria de impressões/cliques agregados e identificação visual de conteúdo patrocinado.
- Fila robusta de bugs, sugestões, acessibilidade, segurança e legal, com protocolo, triagem, histórico e retorno ao autor.
- Audit log administrativo redigido e append-only. Detalhes em `docs/architecture/operations-audit-feedback.md`.

### Fora do primeiro MVP

- Realidade aumentada em produção; o MVP prepara o contrato e valida um spike técnico separado.
- Preço dinâmico e leilão público/reverso; contraproposta privada faz parte do fluxo principal.
- Quests com múltiplas paradas.
- Grupos, guildas sociais, ranking complexo e itens colecionáveis.
- Dungeons geolocalizadas em produção, parties públicas e recompensas aleatórias; primeiro haverá apenas uma vertical slice local simulada.
- Divisão de tarifa, assinatura, carteira e programa de indicação.
- Expansão para várias cidades antes de validar a operação local.
- Marketplace completo com catálogo, estoque, split/payout, entrega, reembolso e disputa.
- Compra de Gold com dinheiro real; o beta pode validar progressão e cosméticos apenas com recompensas conquistadas.

## 3. Arquitetura proposta

### Aplicações

- **Primeiro cliente:** Android nativo com Kotlin + Jetpack Compose, onde navegação, sensores, voz, background e ARCore têm acesso direto às APIs da plataforma.
- **Segundo cliente:** iOS nativo com Swift + SwiftUI depois de validar o núcleo no Android.
- **Web:** experiência complementar para publicar/acompanhar quests e operar o sistema; não será a plataforma de referência para navegação contínua.
- **Compartilhamento:** contratos de API, máquina de estados e modelo de navegação independentes da interface. Avaliar Kotlin Multiplatform apenas depois do spike, sem colocar a navegação nativa atrás de uma abstração frágil.
- **Cross-platform:** Google oferece plugins React Native/Flutter, mas estão em versão 0.x e sem SLA para o código do wrapper; Mapbox não fornece wrappers diretos oficiais. Por isso, frameworks híbridos não serão a fundação inicial sem um spike aprovado.
- **Linguagens:** Kotlin/Swift no mobile e TypeScript nas funções e no web.

### Backend

- **Supabase Auth:** login e sessões.
- **Postgres + PostGIS:** dados transacionais e consultas de quests/motoristas por distância.
- **Supabase Realtime:** mudanças de status, aceite e localização ao vivo.
- **Edge Functions:** operações privilegiadas, webhooks, notificações e integrações de pagamento/mapa que não podem expor segredos.
- **Storage:** documentos de motorista e imagens, sempre com políticas de acesso.
- **RLS em todas as tabelas expostas:** usuário vê o próprio dado; motorista vê apenas dados necessários; admin usa autorização armazenada em `app_metadata`, nunca em metadata editável pelo usuário.

### Serviços complementares

- Push: FCM no Android e APNs no iOS, com disparo pelo backend.
- Erros/telemetria: Sentry ou equivalente.
- Produto: analytics com eventos mínimos e sem enviar localização precisa desnecessariamente.
- Digital: Apple In-App Purchase e Google Play Billing para Gold/cosméticos comprados dentro do app, conforme as políticas vigentes.
- Marketplace físico: provedor de pagamentos com suporte a marketplace, split/payout, estorno e KYC; não usar billing das lojas para bens e serviços físicos.
- Anunciantes/lojistas: portal web B2B separado para campanhas, faturamento, catálogo e operação. Validar as regras de links/compra externa antes de apontar o app consumidor para esse portal.
- Durante o protótipo, usar sandbox e não movimentar dinheiro real.

### Entidades principais

- `profiles`
- `identity_verifications`
- `identity_verification_events`
- `verification_provider_sessions`
- `quest_types`
- `quest_module_versions`
- `quest_capability_definitions`
- `driver_profiles`
- `vehicles`
- `driver_documents`
- `quests`
- `quest_requirements`
- `quest_applications`
- `quest_offers`
- `quest_offer_versions`
- `quest_offer_events`
- `agreed_terms_snapshots`
- `quest_assignments`
- `quest_events`
- `eligibility_evaluations`
- `quest_movement_details`
- `quest_service_details`
- `price_suggestion_policies`
- `price_suggestion_snapshots`
- `transport_price_observations`
- `external_price_sources`
- `external_price_source_versions`
- `external_price_references`
- `external_price_category_mappings`
- `driver_locations`
- `navigation_sessions`
- `navigation_events`
- `messages`
- `ratings`
- `skills`
- `player_skills`
- `skill_xp_ledger`
- `skill_proficiency_levels`
- `credentials`
- `credential_verifications`
- `reputation_events`
- `reputation_scores`
- `service_categories`
- `service_definitions`
- `service_definition_versions`
- `service_skill_rewards`
- `service_proposals`
- `council_cycles`
- `council_votes`
- `council_results`
- `avatars`
- `cosmetic_items`
- `avatar_inventory`
- `xp_ledger`
- `virtual_wallets`
- `virtual_currency_ledger`
- `game_skills`
- `player_game_progress`
- `player_loadouts`
- `dungeon_definitions`
- `dungeon_locations`
- `dungeon_instances`
- `dungeon_parties`
- `dungeon_party_members`
- `dungeon_runs`
- `dungeon_events`
- `adventure_allowance_ledger`
- `game_reward_ledger`
- `gold_ledger`
- `quest_game_reward_policies`
- `advertisers`
- `ad_campaigns`
- `sponsored_places`
- `ad_events_aggregated`
- `merchants`
- `shops`
- `products`
- `orders`
- `order_items`
- `payments`
- `payouts`
- `fulfillments`
- `reports`

Os ledgers de XP global, XP profissional, XP de jogo, Gold, Chaves de Aventura e dinheiro real devem ser separados. Saldos não podem ser atualizados diretamente pelo cliente; toda concessão, compra, consumo, reembolso ou ajuste administrativo gera uma entrada imutável e idempotente.

### Máquina de estados da quest

Núcleo universal: `draft → published → matching → assigned → active → completed`.

Saídas excepcionais: `cancelled`, `expired` e `disputed`. Subestados pertencem ao módulo: deslocamento pode usar `driver_en_route` e `driver_arrived`; serviço pode usar `scheduled`, `diagnosing` e `awaiting_owner_approval`. Toda transição deve ser validada no servidor e registrada em `quest_events`.

### Núcleo de navegação preparado para AR

O SDK escolhido fica atrás de um adaptador nativo que publica um modelo próprio, sem copiar tipos do fornecedor para o domínio:

`Navigation SDK → NavigationSession → NavigationFrame → renderizadores`

Cada `NavigationFrame` deve poder conter:

- posição bruta e posição ajustada à via, com precisão;
- heading/orientação, velocidade e timestamp;
- geometria/corredor da rota e progresso;
- próxima manobra, via, distância e instrução de faixa;
- ETA, distância restante e estado de tráfego quando disponível;
- estado `enroute`, `rerouting`, `off_route`, `arrived` ou `degraded`;
- nível de confiança/qualidade do posicionamento.

Renderizadores iniciais: minimapa 2D, banner de manobra, áudio e HUD compacto. Renderizador futuro: câmera com ARCore Geospatial/ARKit, ancorando indicações no espaço real. O backend recebe somente telemetria necessária e amostrada; o fluxo de alta frequência permanece no aparelho.

### Estado da experiência de mapa

Um controlador de alto nível deve coordenar os modos do mapa sem misturar suas responsabilidades:

- `ExploreMode`: conteúdo, quests, lojas, campanhas, dungeons, busca e câmera livre.
- `RoutePreviewMode`: origem/destino, alternativas, distância e confirmação, ainda sem orientação ativa.
- `ActiveQuestMode`: `NavigationSession` ativa, câmera de seguimento, HUD mínimo e nenhuma camada comercial.
- `ArrivalMode`: confirmação de chegada/conclusão; só então retorna ao mapa-mundo.
- `DungeonApproachMode`: aproximação segura a pé, sem interação quando houver velocidade veicular ou quest ativa.
- `DungeonMode`: lobby, party e encontro lúdico; isolado da progressão profissional e da navegação.

As camadas do `ExploreMode` e de dungeon não recebem eventos durante `ActiveQuestMode`. Isso reduz distração, uso de rede/GPU e risco de um anúncio, convite ou encontro aparecer por engano sobre uma manobra.

## 4. Estratégia de mapas e navegação

Mapa, busca de endereço, cálculo de rota e navegação curva a curva são produtos distintos. Neste projeto, **os quatro fazem parte do MVP**. Google Maps, Waze ou Apple Maps permanecem apenas como fallback.

### Recomendação inicial

1. Criar uma camada `MapProvider`/`RoutingProvider` para evitar acoplamento do domínio a um fornecedor.
2. Fazer um spike Android nativo comparando **Google Navigation SDK** e **Mapbox Navigation SDK v3** com uma rota real na região piloto.
3. Comparar precisão no Brasil, rerota, voz, background, instruções de faixa, liberdade visual, acesso ao feed curva a curva, custo e termos de uso.
4. Escolher o fornecedor antes de construir as telas definitivas do minimapa.
5. Implementar navegação embutida como caminho principal e deep link externo somente como contingência.
6. Construir desde o início o `NavigationFrame` independente de fornecedor e um spike ARCore que consuma dados simulados desse contrato.

### Comparação validada em 2026-07-14

| Opção | Franquia gratuita relevante | Depois da franquia | Observações |
| --- | --- | --- | --- |
| Google Maps Platform | Maps SDK mobile sem cobrança por uso; web Dynamic Maps 10 mil cargas/mês; Routes Essentials 10 mil chamadas/mês; Navigation SDK 1 mil solicitações/mês | Dynamic Maps: US$ 7/1.000; Routes: US$ 5/1.000; Navigation: US$ 25/1.000 no primeiro nível pago | Forte candidato: possui experiência pronta e feed curva a curva customizável com manobras, faixas, tempo e distância. Combina naturalmente com ARCore Geospatial. Billing precisa ser configurado. |
| Mapbox | Mapas mobile: 25 mil MAU/mês; web: 50 mil cargas/mês; Directions: 100 mil chamadas/mês; geocodificação temporária: 100 mil/mês | Maps mobile: US$ 4/1.000 MAU; web: US$ 5/1.000 cargas; Directions: US$ 2/1.000; geocoding temporário: US$ 0,75/1.000 no primeiro nível pago | Forte candidato para identidade visual: Navigation v3 expõe progresso, manobras, voz, rerota, tráfego e offline. No preço medido oferece atualmente 100 MAU e 1.000 viagens grátis; depois parte de US$ 0,30/MAU e US$ 0,08/viagem. |
| MapLibre + OSM + openrouteservice/HeiGIT | Bibliotecas e dados abertos; plano Standard do HeiGIT: 2.000 rotas/dia e 3.000 geocodificações/dia | Pode exigir provedor de tiles/serviço comercial ou infraestrutura própria | Menor lock-in, mas “open source” não torna hospedagem gratuita. Não usar os servidores públicos do OSM como infraestrutura de produção. |

### Alertas sobre a opção aberta

- Os tiles públicos de `tile.openstreetmap.org` não têm SLA e o acesso pode ser bloqueado; para produto comercial, contratar tiles OSM de um provedor ou hospedar os próprios.
- O Nominatim público limita o uso agregado do app a 1 requisição/segundo e proíbe autocomplete. Não é apropriado como busca de endereços de um app em crescimento.
- O openrouteservice gratuito é ótimo para desenvolvimento/beta, mas exige atribuição, possui cotas e não substitui automaticamente uma experiência de navegação curva a curva com tráfego em tempo real.
- Se adotarmos openrouteservice, integrar pelo novo host `api.heigit.org`; o host legado `api.openrouteservice.org` está programado para ser desligado em 2026-08-24.
- Custos e franquias mudam. Revisar os links oficiais antes de ativar produção.

### Fontes oficiais

- Google Maps Platform: https://developers.google.com/maps/billing-and-pricing/pricing
- Mapbox: https://www.mapbox.com/pricing
- HeiGIT/openrouteservice: https://account.heigit.org/info/plans
- Migração da API HeiGIT: https://ask.openrouteservice.org/t/deprecating-api-openrouteservice-org-in-favour-of-api-heigit-org/7912
- OpenStreetMap Tile Policy: https://operations.osmfoundation.org/policies/tiles/
- Nominatim Usage Policy: https://operations.osmfoundation.org/policies/nominatim/
- Google Custom Navigation: https://developers.google.com/maps/documentation/navigation/android-sdk/intro-custom-nav
- Google turn-by-turn feed: https://developers.google.com/maps/documentation/navigation/android-sdk/tbt-feed
- Mapbox Navigation SDK: https://docs.mapbox.com/android/navigation/guides/
- ARCore Geospatial: https://developers.google.com/ar/develop/geospatial
- ARKit Geotracking: https://developer.apple.com/documentation/arkit/tracking-geographic-locations-in-ar
- Google Navigation UI policies: https://developers.google.com/maps/documentation/navigation/android-sdk/policies
- Apple App Review Guidelines: https://developer.apple.com/app-store/review/guidelines/
- Google Play Payments Policy: https://support.google.com/googleplay/android-developer/answer/9858738
- Google Play ads declaration: https://support.google.com/googleplay/android-developer/answer/9859455

## 5. Backlog por fase

### Fase 0 — descoberta e decisões (estimativa: 3–5 dias)

- [x] `PLN-001` Criar plano inicial e arquivo vivo de tarefas.
- [x] `MAP-001` Comparar opções atuais de mapas, rotas, geocoding e navegação.
- [ ] `PRD-001` Escolher o primeiro tipo de quest: passageiro, item ou serviço.
- [x] `PRD-002` Definir cidade/região piloto, público e hipótese de valor: Rio Claro/SP, área técnica máxima de 50 km, Android, 18+ verificado e navigation-first.
- [ ] `PRD-003` Definir regras de preço/recompensa, cancelamento e no-show.
- [ ] `PRD-004` Validar requisitos jurídicos, seguro, verificação de motoristas, LGPD e termos locais.
- [ ] `PRD-005` Desenhar jornadas e critérios de sucesso do beta.
- [x] `ADR-001` Aprovar Android nativo como primeira plataforma e definir a sequência iOS/web.
- [ ] `ADR-002` Escolher o Navigation SDK após o spike e definir limites de gasto.
- [x] `ADR-003` Decidir se pagamento real entra no beta fechado: checkout dentro do app via PSP de marketplace, condicionado a compliance, sandbox e autorização explícita de custo.
- [ ] `PRD-006` Definir se a navegação/AR futura é para motorista, passageiro, pedestre ou dispositivo montado; isso altera requisitos de segurança.
- [ ] `PRD-007` Definir a taxonomia inicial de tipos de quest e quais capacidades pertencem ao núcleo ou a módulos.
- [ ] `PRD-008` Definir matriz de risco que limita publicação, visibilidade e verificação adicional por categoria de quest.
- [ ] `PRD-009` Definir quais categorias/valores exigem aceite simples, assinatura avançada ou assinatura qualificada após validação jurídica.
- [ ] `PRD-010` Aprovar ou substituir a modalidade recomendada: entrega de pequenos itens permitidos/baixo valor em zonas do piloto já definido.
- [ ] `PIL-001` Escrever service blueprint do piloto com responsáveis, horário e runbooks de acidente, fraude, disputa, item perdido, no-show e pedido de autoridade.
- [ ] `PIL-002` Definir meta de oferta por zona/horário, tamanho da coorte, suporte e critérios de pausa/encerramento.
- [ ] `PIL-003` Definir centro/polígono da área máxima de 50 km, municípios cruzados, zonas iniciais menores e áreas excluídas; não abrir zona sem oferta e suporte mínimos.
- [ ] `LEG-001` Obter validação jurídica local para modalidade, município, seguro, consumidor, fiscal, trabalho e documentos antes de feature flag pública.
- [ ] `LEG-002` Confirmar diretamente com Mobilidade de Rio Claro e municípios alcançados os cadastros/regras vigentes; não confiar apenas em notícia ou decreto histórico.
- [ ] `RSK-001` Criar matriz versionada categoria × território × risco × obrigação e owner de compliance.
- [ ] `RSK-002` Definir listas de itens/serviços proibidos, restritos e regulados com fluxo de notice/action e retirada emergencial.
- [ ] `RSK-003` Definir revelação progressiva de localização e prazo de retenção por estado/relacionamento.
- [ ] `RSK-004` Implementar resolução de jurisdição por origem, destino, rota, categoria e data de vigência, sem tratar “Rio Claro + 50 km” como uma única legislação local.
- [x] `BUS-001` Registrar que o Minimapa não recebe percentual do valor pago ao motorista.
- [x] `BUS-002` Definir pagamento da quest dentro do app como repasse transparente por PSP, sem custódia e sem receita de take rate.
- [ ] `BUS-003` Decidir quem absorve e como exibe tarifa do PSP, refund, chargeback, saldo negativo e eventual custo de payout sem reduzir remuneração de forma oculta.
- [ ] `INS-001` Redigir posicionamento do piloto sem cobertura adicional, preservando direitos legais e resposta a incidentes; desenhar Minimap Plus somente com seguradora/parceiro habilitado.
- [ ] `ADS-001` Definir inventário inicial: busca, mapa de exploração, mural, pré-rota e chegada; excluir navegação ativa.
- [ ] `ADS-002` Definir modelo comercial do piloto: venda direta/faturada, CPM, período fixo ou destaque regional.
- [x] `ECO-001` Aprovar separação entre XP global, XP profissional, XP de jogo, Gold, Chaves de Aventura e dinheiro real.
- [ ] `POL-001` Validar App Store/Google Play para publicidade, moeda virtual, cosméticos e marketplace físico.
- [ ] `CST-001` Implementar `CostGuard`, modos `mock/sandbox/production` e bloqueio quando `ALLOW_BILLABLE_REQUESTS=false`.
- [ ] `CST-002` Criar mocks e contract tests para cada integração externa antes de qualquer credencial real.
- [ ] `CST-003` Inventariar chamadas externas, unidade de cobrança, franquia, hard cap e fallback por fornecedor.
- [ ] `CST-004` Garantir que CI, previews e desenvolvimento local nunca recebam chaves de produção.
- [ ] `CST-005` Criar checklist de aprovação explícita para billing, orçamento, alertas e desligamento.
- [ ] `MKT-001` Definir o recorte futuro de lojas: tipos de produto, logística, responsabilidade e modelo de receita do lojista.

### Fase 1 — conceito visual e fundação (estimativa: 1 semana)

- [ ] `DSN-001` Criar conceito visual completo para os fluxos principais, mobile e web.
- [ ] `DSN-002` Aprovar tokens: cores, tipografia, espaçamento, ícones e tom fantasy.
- [ ] `DSN-003` Definir componentes e estados de acessibilidade/contraste.
- [ ] `DSN-004` Projetar estados do minimapa: exploração, rota, manobra, rerota, GPS degradado e chegada.
- [ ] `DSN-007` Projetar shells distintos para mapa-mundo e modo corrida, incluindo transição, retorno e estados interrompidos.
- [~] `DEV-001` Inicializar repositório, convenções, lint, format, testes e CI. Repositório, lint, testes e CI prontos; formatter dedicado ainda pendente.
- [~] `DEV-002` Inicializar aplicativo Android nativo com Kotlin, Jetpack Compose e módulos por domínio. App-base compilando; modularização por domínio será feita junto aos primeiros módulos reais.
- [~] `DEV-003` Configurar ambientes local, staging e produção sem versionar segredos. Ambiente local e regras de exclusão prontos; staging e produção pendentes.
- [ ] `DEV-004` Criar navegação, tema e componentes básicos conforme o conceito aprovado.
- [ ] `DEV-005` Definir contratos de API compartilháveis e fronteira do futuro cliente iOS/web.
- [x] `DEV-006` Configurar emulador Android local e fluxo de um comando para compilar, instalar e abrir o aplicativo.
- [ ] `DOM-001` Extrair contratos de domínio do motor de quests para módulos independentes de UI e navegação.
- [ ] `DOM-002` Implementar registro de tipos/módulos de quest sem permitir regras arbitrárias executadas pelo cliente.
- [ ] `DOM-003` Definir e versionar `QuestModuleContract`, papéis universais, schemas, lifecycle, eventos e pontos de extensão.
- [ ] `DOM-004` Modelar capabilities compostas (`LOCATION`, `ROUTE`, `QUOTE`, `SCHEDULING`, `MATERIALS`, `EVIDENCE` etc.) sem `if/else` global por modalidade.
- [ ] `DOM-005` Criar registry por injeção de dependência; `QuestCore` não pode importar módulo concreto.
- [ ] `DOM-006` Criar suíte de conformidade reutilizável para autorização, lifecycle, concorrência, idempotência, privacidade e compatibilidade.
- [ ] `DOM-007` Provar extensibilidade com uma definição de serviço apenas por dados e um módulo-fixture sem alterar o core.
- [ ] `POL-002` Definir contrato universal `PolicyGate` com decisões `ALLOW`, `REQUIRE_REVIEW`, `DENY`, códigos explicáveis e versão auditada.
- [ ] `POL-003` Impedir que módulo, schema, votação ou configuração administrativa desabilite policy global obrigatória.

### Fase 2 — backend, autenticação e perfis (estimativa: 1–2 semanas)

- [x] `DB-001` Inicializar Supabase local e fluxo de migrations.
- [ ] `DB-002` Criar schema inicial e extensão PostGIS.
- [ ] `DB-003` Modelar núcleo universal de quests e tabelas tipadas dos módulos de deslocamento e serviço.
- [ ] `RUL-001` Implementar requisitos tipados, versionamento e avaliação de elegibilidade exclusivamente no servidor.
- [ ] `SKL-001` Modelar skills, proficiência, origem da comprovação e vínculo com jogadores.
- [ ] `SKL-002` Implementar ledger idempotente de XP por skill acionado pela conclusão válida da quest.
- [ ] `SKL-003` Exibir somente skills com XP, credencial ou comprovação e separar experiência prática de verificação.
- [ ] `SKL-004` Mapear cada versão de serviço para skills canônicas, pesos de XP, limites e regras de confirmação/reversão.
- [ ] `SKL-005` Implementar limites e detecção antifarming para quests combinadas, repetição artificial e concessão de XP manipulada.
- [ ] `CRD-001` Modelar credenciais, emissores, validade, evidências privadas e fluxo de verificação.
- [ ] `REP-001` Modelar karma/reputação contextual por categoria e papel a partir de eventos auditáveis.
- [ ] `REP-002` Implementar ledger idempotente de Karma derivado somente de resultados confirmados, com eventos compensatórios, versões e projeções por dimensão.
- [ ] `REP-003` Implementar ratings bilaterais cegos, confiança por amostra/recência, limite de influência por dupla e detecção de retaliação/conluio.
- [ ] `REP-004` Garantir que recusa, quest ignorada, preço fora da sugestão, denúncia e cancelamento por segurança não reduzam Karma automaticamente.
- [ ] `CAS-001` Modelar `OperationalCase`, evidências, contramanifestação, decisão, medida cautelar, SLA e recurso humano.
- [ ] `SEC-001` Ativar RLS e políticas mínimas para todas as tabelas expostas.
- [ ] `AUTH-001` Implementar cadastro, login, recuperação e encerramento de sessão.
- [ ] `AUTH-002` Implementar perfis de usuário e motorista sem confiar em roles editáveis pelo cliente.
- [ ] `AGE-001` Restringir o piloto a maiores de 18 anos verificados e manter experiência de menores totalmente desabilitada.
- [ ] `SEC-003` Implementar kill switch por categoria, território, provider, feature e usuário sem depender de release do app.
- [ ] `SEC-004` Auditar acesso administrativo, mudança de policy, documento, localização, payout, reward e moderação com step-up.
- [ ] `VER-001` Implementar máquina de estados de verificação, validade, reverificação, suspensão e recurso.
- [ ] `VER-002` Criar adaptador de provedor para documento, dados cadastrais e prova de vida sem acoplar o domínio ao fornecedor.
- [ ] `VER-003` Implementar publicação limitada e badge acessível de identidade não verificada sem usar rótulo “inseguro”.
- [ ] `VER-004` Bloquear no servidor candidatura, aceite, atribuição, execução e conclusão para executor não verificado.
- [ ] `VER-005` Bloquear atribuição/execução enquanto o solicitante da quest não estiver verificado.
- [ ] `VER-006` Exigir MFA/sessão `aal2` em ações sensíveis definidas pela matriz de risco.
- [ ] `VER-007` Implementar webhooks assinados, idempotência, replay protection e revisão manual inconclusiva.
- [ ] `VER-008` Fazer spike comparativo entre Datavalid V4/BioConnect e uma solução orquestrada como idwall ou Unico IDCloud.
- [ ] `VER-009` Medir falso aceite/rejeição, abandono, acessibilidade, fallback, latência, cobertura, retenção e custo na região piloto.
- [ ] `SIG-001` Fazer spike de assinatura eletrônica via Clicksign API 3.0 e Autentique para fluxos de maior risco.
- [ ] `DRV-001` Implementar veículo, documentos e estado de aprovação.
- [ ] `DRV-002` Modelar CNH com categoria, validade, EAR quando aplicável e compatibilidade com veículo/modalidade, sem expor número ou imagem ao público.
- [ ] `DRV-003` Projetar identificação pós-atribuição com foto verificada, nome necessário, veículo, cor e placa, além de fluxo de divergência/denúncia.
- [ ] `TST-001` Testar RLS com usuários distintos e tentativas de acesso indevido.
- [ ] `TST-011` Tentar contornar gates de verificação via cliente, JWT desatualizado, chamada direta e replay de webhook.
- [ ] `TST-010` Testar concessão única, pendência, confirmação e compensação de XP por skill sob retries e disputas.

### Fase 3 — spikes de navegação, mapas e criação de quest (estimativa: 2–3 semanas)

- [ ] `NAV-001` Fazer spike Android do Google Navigation SDK com experiência pronta e feed curva a curva customizado.
- [ ] `NAV-002` Fazer spike Android do Mapbox Navigation SDK v3 com `RouteProgress`, manobras, voz e rerota.
- [ ] `NAV-003` Testar ambos em rotas reais da região piloto e registrar precisão, latência, cobertura, bateria e custo projetado.
- [ ] `NAV-004` Registrar ADR escolhendo o SDK e documentando como trocar de fornecedor.
- [ ] `MAP-002` Implementar o mapa Android com o SDK de navegação escolhido.
- [ ] `MAP-003` Implementar busca/autocomplete e geocodificação reversa.
- [ ] `MAP-004` Implementar cálculo e desenho de rota com distância/ETA.
- [ ] `MAP-005` Criar adaptadores de provedor e telemetria de consumo/custo.
- [ ] `MAP-006` Implementar `ExploreMode` com busca, quests, lojas/pontos, filtros, clustering e camadas patrocinadas.
- [ ] `MAP-007` Implementar `RoutePreviewMode` sem iniciar cobrança/sessão de navegação prematuramente.
- [ ] `GEO-002` Implementar `QuestDiscoveryArea` como círculo/célula aproximada com exclamação fantasy e centro estável dissociado do endereço exato.
- [ ] `GEO-003` Garantir server-side que zoom, filtros e consultas repetidas não refinam a área; liberar endereço exato somente após atribuição e gates válidos.
- [ ] `QST-001` Implementar criação, validação, prévia e publicação de quest.
- [ ] `QST-002` Implementar histórico e detalhe para quem publicou.
- [x] `PRC-001` Aprovar faixa sugerida e editável para transporte com base robusta em quests comparáveis concluídas nos últimos 30 dias.
- [ ] `PRC-002` Modelar observações e snapshots auditáveis com versão, filtros, amostra, percentis, custos e confiança.
- [ ] `PRC-003` Implementar agregação local por região, modalidade, veículo, faixa de distância e período; sem amostra mínima, omitir a sugestão em vez de inventar fallback.
- [ ] `PRC-004` Exibir faixa, decomposição, tamanho da amostra, confiança e aviso de que a sugestão não garante aceite.
- [ ] `PRC-005` Excluir gorjeta, pedágio, reembolso, disputa, fraude e outliers; limitar influência repetida da mesma conta ou dupla.
- [ ] `PRC-006` Testar recálculo, pouca amostra, privacidade, manipulação, rotas curtas/longas e escolha de valor fora da faixa.
- [x] `PRC-008` Aprovar `QUOTE_AND_SCHEDULE` como cold start de serviços, sem valor-base da plataforma.
- [ ] `PRC-009` Implementar proposta de serviço com mão de obra, prazo, deslocamento, materiais e validade discriminados.
- [ ] `PRC-010` Calcular faixa histórica somente após amostra mínima da mesma definição/versionamento, região e escopo de serviço.
- [ ] `PRC-011` Exibir “ainda sem dados comparáveis” e permitir orçamento opcional ou “a combinar” durante o cold start.
- [x] `PRC-012` Catalogar SINAPI, ANTT, tarifas municipais e tabelas profissionais como fontes candidatas, sem tratá-las como equivalentes.
- [ ] `SEC-002` Manter chaves secretas de mapas/rotas no servidor quando exigido e restringir chaves públicas por app/domínio.

### Fase 4 — mural e aceite (estimativa: 1–2 semanas)

- [ ] `GEO-001` Consultar quests abertas por raio usando índice espacial.
- [ ] `BRD-001` Implementar mural/lista/mapa de quests para motoristas.
- [ ] `BRD-002` Implementar filtros essenciais e detalhe da quest.
- [ ] `BRD-003` Implementar estratégia `PULL_BOARD`: mural opt-in e notificações por filtros, sem despacho individual, taxa de aceite ou penalidade por recusa.
- [ ] `QST-003` Implementar aceite atômico no banco (`open → accepted`).
- [ ] `QST-004` Tratar corrida de dois motoristas aceitando a mesma quest.
- [ ] `QST-007` Implementar aceite da oferta publicada ou contraproposta privada e versionada por pessoa/empresa elegível.
- [ ] `QST-008` Modelar preço `FIXED_TOTAL`, `LABOR_ONLY`, `ESTIMATE_WITH_CEILING` e `DIAGNOSIS_REQUIRED`, separando materiais.
- [ ] `QST-009` Aceitar proposta em transação atômica, criar `AgreedTermsSnapshot` e expirar/rejeitar as propostas concorrentes.
- [ ] `QST-010` Implementar revisão, retirada, rejeição, expiração e aditivo bilateral sem apagar versões anteriores.
- [ ] `QST-011` Garantir que candidatos nunca vejam valores/termos das propostas concorrentes.
- [ ] `TST-012` Testar aceite concorrente, proposta vencida, perda de elegibilidade, revisão e tentativa de alterar termos após atribuição.
- [ ] `NTF-001` Enviar notificações de publicação, aceite e cancelamento.
- [ ] `TST-002` Criar teste de concorrência para aceite único.

### Fase 5 — minimapa, navegação e execução em tempo real (estimativa: 3–4 semanas)

- [ ] `LOC-001` Solicitar permissões de localização com explicação e fallback.
- [ ] `LOC-002` Atualizar localização do motorista somente durante janelas necessárias.
- [ ] `LOC-003` Exibir posição e ETA para o usuário em tempo real.
- [ ] `NAV-005` Implementar `NavigationSession` e adaptador do SDK escolhido.
- [ ] `NAV-006` Implementar `NavigationFrame` com posição, heading, precisão, progresso, manobra, faixa, ETA e qualidade.
- [ ] `NAV-007` Construir o minimapa 2D rotacionado, linha de rota, próxima manobra e HUD de missão.
- [ ] `NAV-008` Implementar instruções de voz, rerota, desvio, chegada e modo de GPS degradado.
- [ ] `NAV-009` Implementar ciclo background/foreground e retomada segura da sessão.
- [ ] `NAV-012` Implementar transição atômica de aceite/confirmação para `ActiveQuestMode`.
- [ ] `NAV-013` Desmontar camadas do mapa-mundo ao iniciar navegação e restaurá-las somente após chegada, cancelamento ou saída confirmada.
- [ ] `NAV-014` Implementar retorno seguro ao mapa-mundo preservando filtros, câmera e contexto quando apropriado.
- [ ] `QST-005` Implementar transições de chegada, início e conclusão validadas no servidor.
- [ ] `QST-006` Adicionar PIN ou confirmação equivalente para iniciar a quest.
- [ ] `NAV-010` Abrir Google Maps/Waze/Apple Maps somente como fallback de contingência.
- [ ] `MSG-001` Implementar contato seguro/chat mínimo sem expor telefone diretamente.
- [ ] `RES-001` Tratar perda de rede, retomada e eventos duplicados.

### Fase 6 — confiança e operação do piloto (estimativa: 2–3 semanas)

- [ ] `ADM-001` Criar painel de operação protegido.
- [ ] `ADM-002` Implementar aprovação/bloqueio de motorista e auditoria.
- [ ] `ADM-003` Implementar audit log append-only com RBAC, step-up, reason codes, correlação, redaction, retenção e dupla aprovação para ações materiais.
- [ ] `ADM-004` Criar módulo administrativo de bugs/sugestões com triagem, deduplicação, severidade, owner, histórico, release e notificação ao autor.
- [ ] `RAT-001` Implementar avaliações bilaterais e denúncia.
- [ ] `PRV-001` Definir retenção e exclusão de localização/documentos conforme LGPD.
- [ ] `PRV-002` Definir minimização, retenção, criptografia/tokenização e acesso auditado para documento e biometria.
- [ ] `PRV-003` Elaborar RIPD de identidade, biometria, localização, profiling/antifraude e fornecedores antes do piloto.
- [ ] `SAF-001` Implementar contato de emergência e compartilhamento da quest, se aplicável ao tipo escolhido.
- [ ] `SAF-002` Implementar revelação progressiva de endereço e localização exata somente por finalidade/relação/janela.
- [ ] `SAF-003` Implementar conteúdo proibido/restrito, moderação, bloqueio, denúncia, notice/action e retirada emergencial.
- [ ] `SAF-004` Implementar política de evidência, disputa e conclusão sem tratar GPS/foto como prova absoluta.
- [ ] `SUP-001` Implementar console e fila mínima de suporte com SLA do piloto, reason codes e preservação de evidência.
- [ ] `SUP-002` Criar `SupportCaseProvider` e testar Chatwoot self-hosted local com API/webhooks, mantendo `support_cases` como registro canônico.
- [ ] `SUP-003` Automatizar protocolo, classificação, SLA, macros, checklist e escalonamento; decisão material e incidente grave mantêm revisão humana.
- [ ] `SUP-004` Preparar adesão futura ao Consumidor.gov.br após CNPJ, SAC acessível, termos e operação diária estarem prontos.
- [ ] `FBK-001` Criar canal in-app para bug, sugestão, acessibilidade, segurança e legal com anexos consentidos/redigidos e logs opt-in.
- [ ] `PAY-001` Implementar `MarketplacePaymentProvider`, ledger espelho, inbox de webhooks idempotente, reconciliação e mocks sem chamadas faturáveis.
- [ ] `PAY-002` Fazer spike sandbox de Stripe Connect, Mercado Pago Split e Pagar.me Marketplace comparando KYC, checkout in-app, repasse, refund, chargeback, Pix, custo e responsabilidades.
- [ ] `PAY-003` Implementar estados de pagamento/payout/refund/disputa e congelar repasse durante caso quando suportado, sem usar Karma como ordem financeira.

### Fase 7 — qualidade e beta (estimativa: 1–2 semanas)

- [ ] `QA-001` Testes unitários da máquina de estados e cálculo de valores.
- [ ] `QA-002` Testes integrados de RLS, aceite, notificações e webhooks.
- [ ] `QA-003` E2E do fluxo publicar → aceitar → iniciar → concluir.
- [ ] `QA-004` Verificar a plataforma mobile escolhida para o beta, responsividade e acessibilidade; aplicar o mesmo gate quando iOS/web entrarem no escopo.
- [ ] `QA-005` Testar localização em foreground/background, bateria e permissões negadas.
- [ ] `QA-006` Testar navegação simulada e em rotas reais: manobras, rerota, túneis/áreas densas, áudio e chegada.
- [ ] `QA-007` Validar legibilidade do minimapa e distração visual com protocolo de segurança.
- [ ] `QA-008` Verificar que anúncios nunca aparecem na navegação ativa e são sempre identificados como patrocinados.
- [ ] `QA-009` Testar idempotência, abuso e consistência do XP/inventário.
- [ ] `OBS-001` Configurar alertas, logs com redaction e métricas do funil.
- [ ] `OPS-001` Configurar limites/alertas de gasto dos provedores externos.
- [ ] `OPS-002` Medir custo projetado por usuário e quest usando contadores locais antes de habilitar faturamento.
- [ ] `BET-001` Rodar beta fechado em uma região com suporte manual.
- [ ] `BET-002` Revisar métricas e decidir avançar, ajustar ou reduzir o escopo.

### Fase 8 — pós-validação

- [ ] `AR-001` Fazer spike de ARCore Geospatial com uma manobra simulada derivada de `NavigationFrame`.
- [ ] `AR-002` Transformar corredor/manobras da rota em pontos geoespaciais seguros para renderização.
- [ ] `AR-003` Implementar renderizador experimental com ARCore Geospatial e fallback quando VPS/precisão não estiver disponível.
- [ ] `AR-004` Avaliar ARKit/ARCore no iOS e diferenças de cobertura geográfica.
- [ ] `AR-005` Medir precisão, deriva, oclusão, bateria, temperatura e segurança antes de qualquer beta público.
- [ ] `NAV-011` Avaliar navegação offline e pacotes regionais conforme o SDK escolhido.
- [ ] `MCH-001` Avaliar matching/recomendação automática sem remover o mural de quests.
- [ ] `MCH-002` Ampliar estratégias configuráveis após validar aceite direto, contraproposta privada e orçamento/agendamento do fluxo principal.
- [ ] `PRC-007` Avaliar componente de tempo, sazonalidade e eventual preço dinâmico somente após medir o estimador transparente do MVP.
- [ ] `SCL-001` Testar carga, particionamento/retenção de localizações e expansão regional.
- [ ] `GAM-005` Avaliar progressão social, temporadas e novas categorias cosméticas sem prejudicar confiança.
- [ ] `DSN-005` Projetar avatar, níveis, loadout cosmético e recompensas sem pay-to-win.
- [ ] `GAM-001` Definir fontes de XP, curvas de nível, limites diários e controles antiabuso.
- [ ] `GAM-002` Implementar avatar, inventário e loadout cosmético.
- [ ] `GAM-003` Implementar ledger imutável de XP e concessão idempotente de recompensas.
- [ ] `GAM-004` Entregar cosméticos conquistáveis, sem compra com dinheiro real.
- [ ] `DSN-006` Projetar pins/locais patrocinados com identificação inequívoca e baixa densidade.
- [ ] `ADS-003` Implementar anunciantes, campanhas e locais patrocinados administrados manualmente.
- [ ] `ADS-004` Renderizar conteúdo patrocinado apenas em superfícies permitidas, com rótulo e limite de densidade.
- [ ] `ADS-005` Registrar impressões/interações agregadas, frequência e orçamento sem trilha individual vendável.
- [ ] `ADS-006` Criar relatório simples para validar valor com os primeiros anunciantes locais.
- [ ] `PRC-013` Implementar registro versionado de fonte, vigência, território, licença/termos, checksum, unidade, fórmula e natureza jurídica.
- [ ] `PRC-014` Criar importador offline do SINAPI por fixtures e mapeamento revisado para serviços canônicos de construção/reparo.
- [ ] `PRC-015` Cadastrar manualmente referências vigentes da cidade piloto e validar enquadramento jurídico antes de aplicar qualquer piso.
- [ ] `PRC-016` Exibir histórico interno e referência externa em blocos separados, com metodologia e divergência visíveis.
- [ ] `PRC-017` Automatizar atualização somente para fonte com download/API estável e termos compatíveis; não raspar concorrentes ou calculadoras fechadas.
- [ ] `GOV-001` Implementar propostas, consolidação de duplicatas e ciclos mensais do Conselho do Reino.
- [ ] `GOV-002` Implementar votação com elegibilidade, voto único, quórum, auditoria e controles antissybil.
- [ ] `GOV-003` Criar catálogo e renderer de serviços orientados a blocos de schema aprovados.
- [ ] `GOV-004` Gerar automaticamente apenas `ServiceDefinitionCandidate`; `ACTIVE_BETA` exige `ComplianceApproval` humano, assinado e versionado.
- [ ] `GOV-005` Submeter todo vencedor a gates proporcionais; serviços regulados/alto risco exigem revisão especializada antes de permitir quests.
- [ ] `GOV-006` Preservar versões antigas, permitir suspensão/rollback e auditar ajustes manuais posteriores.

### Fase 9 — RPG geolocalizado e dungeons

- [ ] `RPG-001` Implementar domínios e ledgers separados para nível global, progressão profissional e progressão de jogo.
- [ ] `RPG-002` Criar schemas versionados para definição, localização, instância e ciclo de vida de dungeon.
- [ ] `RPG-003` Implementar `DungeonApproachMode` e `DungeonMode`, desmontados e bloqueados durante `ActiveQuestMode`.
- [ ] `RPG-004` Implementar party pública, privada e por convite, com liderança, limite, saída, bloqueio e denúncia.
- [ ] `RPG-005` Criar motor autoritativo de encontros por eventos e blocos de conteúdo aprovados, sem código arbitrário.
- [ ] `RPG-006` Implementar Chaves de Aventura com concessões, consumo, estorno, cooldown e caps diários/semanais/mensais.
- [ ] `RPG-007` Implementar recompensas idempotentes de XP de jogo, Gold e itens por tabelas versionadas.
- [ ] `RPG-008` Implementar skills, equipamentos e loadout de jogo sem efeito em proficiência, karma, credencial ou matching profissional.
- [ ] `RPG-009` Validar vertical slice local: dungeon mockada, GPS do emulador, party-bot, um encontro e ledger local, sem API paga.
- [ ] `RPG-010` Implementar gates de movimento seguro, precisão, quest ativa, cooldown, spoofing e multiaccount proporcionais ao risco.
- [ ] `RPG-011` Definir critérios operacionais para POIs seguros, acessíveis, permitidos, moderáveis e removíveis emergencialmente.
- [ ] `RPG-012` Testar renderer AR opcional consumindo o mesmo estado da dungeon, com fallback 2D e uso somente parado/a pé.
- [ ] `RPG-013` Definir transparência de drops, proteção de menores e revisão jurídica/políticas antes de qualquer recompensa aleatória paga.
- [ ] `RPG-014` Implementar ponte idempotente `QuestCompleted → QuestGameRewardPolicy → GameRewardLedger`, sem dependência inversa na elegibilidade profissional.

Critérios da vertical slice: entrar em `ActiveQuestMode` ou simular velocidade veicular encerra/bloqueia interação; repetição de um evento não duplica recompensa; concluir uma quest pode conceder recompensa de RPG uma única vez; nenhum estado do RPG altera uma regra de elegibilidade profissional; todos os serviços permanecem locais/mock e sem billing.

### Fase 10 — economia digital e marketplace de lojas

- [ ] `ECO-002` Definir pacotes, sinks, não expiração e política de reembolso de Gold.
- [ ] `ECO-003` Implementar wallet/ledger de Gold sem transferência ou saque entre usuários.
- [ ] `IAP-001` Integrar Apple In-App Purchase e Google Play Billing para Gold/cosméticos digitais.
- [ ] `IAP-002` Implementar validação server-side, restore, reembolso e prevenção de replay de recibos.
- [ ] `MER-001` Criar portal web B2B para onboarding, Licença de Guilda, campanhas e gestão da loja.
- [ ] `MER-002` Implementar loja, catálogo, variações, estoque, disponibilidade e moderação.
- [ ] `MKT-PAY-001` Estender o provedor aprovado para pagamentos de lojas com KYC, split/payout, estorno e conciliação multi-lojista.
- [ ] `MKT-PAY-002` Implementar pagamentos físicos das lojas em sandbox e webhooks idempotentes.
- [ ] `MKT-PAY-003` Implementar recibo, cancelamento, reembolso, disputa e reconciliação específicos de pedidos de loja.
- [ ] `ORD-001` Implementar pedido, confirmação do lojista, separação e status de fulfillment.
- [ ] `DLV-001` Converter pedido pronto em quest de entrega sem descontar comissão do motorista.
- [ ] `DLV-002` Confirmar coleta/entrega, prova mínima e resolução de falhas.
- [ ] `MOD-001` Implementar moderação de lojas, produtos, avaliações e conteúdo gerado por usuários.

## 6. Critérios de pronto para o beta

- Um usuário consegue publicar uma quest válida em menos de dois minutos.
- Somente um motorista consegue aceitar a mesma quest, inclusive sob concorrência.
- Nenhum usuário não verificado consegue ser atribuído, iniciar ou concluir uma quest por qualquer cliente ou chamada direta.
- Uma quest de solicitante não verificado fica claramente marcada e não avança para atribuição/execução antes da verificação.
- No piloto fechado, somente maiores de 18 anos com identidade verificada conseguem publicar ou executar; a exceção futura para publicação não verificada permanece desabilitada.
- Cidade, modalidade, seguro, pagamento e obrigações locais possuem aprovação registrada antes da abertura do piloto.
- `PolicyGate` é aplicado server-side em todas as transições sensíveis e nenhum módulo/configuração consegue remover policy global.
- Endereço exato não aparece na descoberta e só é liberado após relação, finalidade e janela autorizadas.
- Existe lista de itens/serviços proibidos/restritos, notice/action, suporte humano, kill switch e runbook para incidente grave.
- O pagamento da quest ocorre dentro do app por PSP licenciado; o Minimapa não custodia dinheiro, e produção exige integração, compliance e custo explicitamente aprovados.
- Karma nunca é a única base para refund, payout, culpa ou sanção material; existe caso, evidência proporcional, decisão explicável e recurso.
- Ignorar ou recusar uma quest não altera Karma, visibilidade futura ou acesso ao mural.
- A descoberta envia apenas área aproximada estável; zoom e consultas repetidas não revelam o endereço exato.
- CNH/categoria/EAR e veículo são validados quando aplicáveis, mas número e documento bruto não aparecem ao público.
- Existe canal in-app de bugs/sugestões e audit log administrativo com acesso, redaction e ações materiais rastreáveis.
- Build, CI e testes locais realizam zero chamadas faturáveis e funcionam apenas com serviços locais, mocks ou sandboxes garantidamente gratuitas.
- Origem, destino, preço, motorista e veículo permanecem claros em todas as telas críticas.
- A sugestão de transporte é editável, auditável, mostra faixa/confiança e não mistura custos reembolsáveis com o valor líquido por quilômetro.
- Serviços sem amostra usam orçamento/propostas e nunca recebem um valor-base fabricado; materiais e deslocamento aparecem separados da mão de obra.
- Autor pode publicar sua oferta; executor elegível pode aceitá-la ou fazer contraproposta privada, e o aceite congela termos sem permitir duas atribuições.
- Adicionar uma definição de serviço exige apenas dados/schema; adicionar uma modalidade exige novo módulo que passe na suíte de conformidade, sem editar o `QuestCore`.
- Toda referência externa mostra fonte, competência, território e natureza; piso legal, custo de referência, tarifa e histórico interno nunca são fundidos em uma média opaca.
- Localização só é coletada com consentimento e durante a finalidade declarada.
- RLS impede acesso cruzado a dados privados e documentos.
- O fluxo principal funciona após perda temporária de rede sem duplicar cobrança ou eventos.
- O minimapa fornece navegação embutida com manobra, voz, rerota, ETA e chegada sem depender de outro aplicativo.
- O app entra em modo degradado seguro quando localização ou heading não têm precisão suficiente.
- Gastos de mapas/rotas têm limite e alerta configurados.
- O fluxo publicar → aceitar → navegar → iniciar → concluir passa na plataforma mobile do beta; clientes iOS/web posteriores devem cumprir o mesmo gate.
- Existe operação manual para bloqueio, disputa e suporte durante o beta.
- Nenhuma receita do Minimapa é calculada como percentual da remuneração do motorista.
- Conteúdo patrocinado é identificado, limitado e ausente durante navegação ativa.
- A transição mapa-mundo → modo corrida remove camadas de lojas, busca, quests não relacionadas e publicidade; a conclusão restaura o contexto de exploração.
- XP, níveis e cosméticos conquistados são consistentes, auditáveis e não compram vantagem operacional.
- A separação de domínio impede que XP, skills, Gold ou equipamentos de dungeon alterem proficiência, credenciais, karma ou elegibilidade profissional.
- `ActiveQuestMode` não renderiza nem processa dungeons, convites, recompensas de jogo ou outras interações lúdicas.
- Publicidade permanece desabilitada até o núcleo provar liquidez, segurança e operação; um piloto manual precede qualquer compra self-service.

## 7. Riscos a acompanhar

- Regulação, seguro e responsabilidade mudam bastante entre passageiro, entrega e serviço.
- Localização em background exige justificativa para App Store/Play Store e pode consumir bateria.
- GPS pode ser impreciso ou fraudado; não usar uma coordenada isolada como prova absoluta.
- Aceite, pagamento e webhooks precisam ser idempotentes e transacionais.
- O visual fantasy não pode reduzir acessibilidade ou clareza de segurança.
- Cotas gratuitas podem mudar ou ser insuficientes quando o produto crescer.
- Web móvel não oferece a mesma confiabilidade de localização em background que apps nativos.
- AR geoespacial depende de compatibilidade do aparelho, cobertura VPS/geotracking, câmera, sensores e condições do ambiente.
- Uma interface AR usada por condutores pode aumentar distração e risco; o caso de uso e o suporte físico precisam ser validados antes de produção.
- O feed customizado do Google e wrappers cross-platform ainda possuem superfícies preview/0.x; os spikes devem testar estabilidade e plano de migração.
- Google Navigation impõe regras de segurança para overlays; publicidade durante navegação pode violar política e aumentar distração.
- Gold usado para ocultar preço real pode ser considerado experiência enganosa. Preço, equivalência, saldo e reembolso devem ser transparentes.
- Bens digitais, cosméticos e funcionalidades desbloqueadas no app normalmente exigem Apple IAP/Google Play Billing; bens e serviços físicos seguem outro fluxo de pagamento.
- Misturar Gold com saldo de lojista, repasse ou remuneração de motorista cria risco contábil, fraude e possível enquadramento financeiro.
- Marketplace traz KYC, chargeback, fraude, moderação, tributação, defesa do consumidor, logística e responsabilidade por produtos.
- Publicidade baseada em localização precisa pode revelar padrões sensíveis; preferir contexto em tempo real e métricas agregadas.
- O app deve declarar publicidade às lojas e distinguir claramente posicionamento pago de resultado orgânico.
- Dungeons podem incentivar distração ao dirigir, travessias inseguras ou invasão de propriedade; interação exige gate de movimento e POIs previamente aprovados.
- Party e encontro presencial ampliam riscos de assédio, exposição de localização e segurança de menores; aplicar privacidade por padrão, bloqueio, denúncia e regras etárias.
- GPS spoofing, multiaccount e farming podem desequilibrar Gold e recompensas; usar sinais proporcionais sem transformar localização em vigilância permanente.
- Drops aleatórios, energia comprável e itens com valor percebido podem acionar regras de loot box, jogos de azar ou proteção do consumidor; não monetizar sem revisão específica.
- Uma média simples de preço por quilômetro pode reforçar distorções, manipulação ou remuneração inadequada; usar segmentos comparáveis, estatística robusta, amostra mínima e monitoramento de impacto.
- Medianas de serviços pouco comparáveis podem induzir preço inadequado; não misturar categorias, versões, escopos ou materiais e ocultar a faixa quando faltar amostra.
- Fonte externa pode estar vencida, fora do território, ter licença incompatível ou representar custo em vez de preço final; versionar, revisar e rotular antes de exibir.
- Contrapropostas públicas podem provocar corrida ao menor preço e conluio; manter propostas privadas, limitar spam e auditar comportamento coordenado.
- Mudanças informais de escopo ou materiais após o aceite favorecem conflito e bait-and-switch; exigir snapshot e aditivo bilateral.
- Campos ou condicionais específicos de modalidade vazando para o core criam refactor futuro; aplicar dependency inversion, capabilities e testes de fronteira entre módulos.
- Votação comunitária não comprova legalidade; ativação automática sem aprovação humana pode abrir categoria proibida, regulada ou sem seguro.
- A relação entre plataformas e trabalhadores permanece juridicamente dinâmica; preço/controle unilateral, punição por recusa e bloqueio automático aumentam risco trabalhista.
- Ausência de take rate não elimina responsabilidade consumerista, dever de informação, suporte, cancelamento, moderação ou segurança.
- Custodiar fundos, permitir cashout/transferência de Gold ou misturar payout/refund pode criar risco regulatório financeiro e lavagem.
- O ECA Digital amplia obrigações para produtos de acesso provável por menores e veda loot boxes no escopo legal; piloto será 18+ e RPG infantil exige revisão própria.
- Conta tomada pode combinar fraude, endereço e risco físico; recuperação, MFA/step-up e cooldown de payout são controles de segurança pessoal.

## 8. Log de decisões

| Data | ID | Status | Decisão |
| --- | --- | --- | --- |
| 2026-07-14 | `DEC-001` | substituída | Usar Expo/React Native/Expo Router como cliente universal, com mapas específicos por plataforma. Substituída após navegação embutida e AR se tornarem requisitos centrais. |
| 2026-07-14 | `DEC-002` | decidida | Usar Supabase/Postgres/PostGIS/Realtime com RLS por padrão. |
| 2026-07-14 | `DEC-003` | substituída | Usar Mapbox diretamente no protótipo. Substituída por spike comparativo Google Navigation vs Mapbox Navigation. |
| 2026-07-14 | `DEC-004` | substituída | No MVP, abrir navegação externa e adiar Navigation SDK embutido. |
| 2026-07-14 | `DEC-005` | decidida | Navegação curva a curva embutida é parte fundamental do MVP; navegação externa é apenas fallback. |
| 2026-07-14 | `DEC-006` | decidida | O produto é mobile-first e o núcleo de navegação deve alimentar um futuro renderizador em realidade aumentada. |
| 2026-07-14 | `DEC-007` | decidida | Começar por Android nativo e levar o núcleo validado ao iOS antes de expandir a navegação para outras superfícies. |
| 2026-07-14 | `DEC-008` | decidida | O Minimapa não recebe percentual da remuneração dos motoristas. |
| 2026-07-14 | `DEC-009` | decidida | Publicidade local é a fonte de receita principal planejada. |
| 2026-07-14 | `DEC-010` | decidida | Todos os usuários participam de progressão com nível, avatar e recompensas estéticas. |
| 2026-07-14 | `DEC-011` | decidida | Lojas de empresas, pedidos, pagamento e entrega formam uma camada posterior de marketplace. |
| 2026-07-14 | `DEC-012` | substituída | Separar XP, gemas de cosméticos e dinheiro real. Ampliada e renomeada por `DEC-021` após a inclusão do RPG geolocalizado. |
| 2026-07-14 | `DEC-013` | decidida | Exibir publicidade em exploração/busca/mural/chegada, nunca durante navegação ativa. |
| 2026-07-14 | `DEC-014` | decidida | Separar o mapa em `ExploreMode` rico em conteúdo e `ActiveQuestMode` limpo para curva a curva. |
| 2026-07-14 | `DEC-015` | decidida | Tratar Quest como núcleo universal extensível por módulos tipados, requisitos versionados e estratégias configuráveis de atribuição. |
| 2026-07-14 | `DEC-016` | decidida | Separar level global, XP de skill, proficiência, karma contextual e verificação por credencial. |
| 2026-07-14 | `DEC-017` | substituída | Usar o Conselho do Reino para escolher mensalmente serviços e ativar automaticamente vencedores compatíveis. A ativação automática foi substituída pelo gate humano de `DEC-031`. |
| 2026-07-14 | `DEC-018` | decidida | Exigir identidade verificada para executar quests; permitir publicação limitada por não verificados, bloqueando atribuição até a verificação de ambas as partes. |
| 2026-07-14 | `DEC-019` | proposta | Selecionar verificação após spike entre Datavalid e um orquestrador privado; manter assinatura eletrônica como controle separado e baseado em risco. |
| 2026-07-14 | `DEC-020` | decidida | Manter custo adicional zero durante desenvolvimento; preparar integrações por adaptadores e mocks, sem billing ou produção até autorização explícita. |
| 2026-07-14 | `DEC-021` | decidida | Adotar Gold como moeda virtual fechada e separar nível global, XP profissional, XP de jogo, Chaves de Aventura, Gold e dinheiro real em domínios e ledgers independentes. |
| 2026-07-14 | `DEC-022` | decidida | Criar dungeons geolocalizadas, party e avatar jogável como camada pós-validação, bloqueada durante navegação ativa e sem efeitos do RPG sobre confiança ou qualificação profissional. |
| 2026-07-14 | `DEC-023` | decidida | Tratar Chaves de Aventura como franquia limitada de participação, não como moeda; no desenho inicial são não transferíveis e não compráveis. |
| 2026-07-14 | `DEC-024` | decidida | Permitir apenas a integração unidirecional `Quests → RPG`: quests validadas podem conceder progresso lúdico, mas nenhum estado ou recompensa do RPG melhora elegibilidade ou progressão profissional. |
| 2026-07-14 | `DEC-025` | decidida | Sugerir uma faixa editável para quests de transporte usando mediana robusta do valor por quilômetro de quests comparáveis concluídas nos 30 dias anteriores, com custos separados, amostra mínima, confiança e snapshot auditável. |
| 2026-07-14 | `DEC-026` | decidida | Não inventar valor-base no cold start: transporte sem amostra omite sugestão; serviços começam com pedido de orçamento e só exibem faixa histórica após amostra comparável suficiente. |
| 2026-07-14 | `DEC-027` | decidida | Permitir fontes externas oficiais/profissionais como referências versionadas e identificadas, separando piso legal, custo, honorário indicativo, tarifa regulada e histórico do Minimapa. |
| 2026-07-14 | `DEC-028` | decidida | Adotar oferta do autor com dois caminhos: aceite integral ou contraproposta privada de pessoa/empresa elegível; o acordo aceito é atômico, versionado e imutável salvo aditivo bilateral. |
| 2026-07-14 | `DEC-029` | decidida | Tornar extensibilidade uma invariável: definições entram por schema; modalidades implementam `QuestModuleContract` versionado por capabilities, sem dependência do core em módulos concretos. |
| 2026-07-14 | `DEC-030` | parcialmente decidida | Piloto Android, 18+ verificado e Rio Claro/SP com área técnica máxima de 50 km; entrega de itens pequenos/permitidos permanece a modalidade recomendada, pendente de validação jurídica local. |
| 2026-07-14 | `DEC-031` | decidida | Conselho nomeia definição candidata; nenhuma categoria entra em `ACTIVE_BETA` sem `ComplianceApproval` humano, versionado e auditável. |
| 2026-07-14 | `DEC-032` | decidida | Restringir o piloto a maiores de 18 anos verificados nas duas pontas e revelar localização exata apenas progressivamente após atribuição. |
| 2026-07-14 | `DEC-033` | substituída | Não custodiar dinheiro no piloto e adiar pagamento interno. Substituída por `DEC-036`: checkout in-app via PSP sem custódia pelo Minimapa. |
| 2026-07-14 | `DEC-034` | decidida | Policies globais de categoria, jurisdição, idade, identidade, localização, conteúdo, consumidor e pagamento limitam todos os módulos e transições sensíveis. |
| 2026-07-14 | `DEC-035` | decidida | Usar Rio Claro/SP como base do piloto, com área técnica máxima de 50 km, resolução jurídica por rota e abertura gradual por zonas menores. |
| 2026-07-14 | `DEC-036` | decidida | Processar pagamento dentro do app por PSP de marketplace, sem custódia, take rate ou conversão de Gold; tarifa e responsabilidades serão transparentes e ainda precisam de seleção contratual. |
| 2026-07-14 | `DEC-037` | decidida | Não oferecer seguro/garantia adicional no piloto; manter resposta a incidentes e preservar direitos legais. Minimap Plus com seguro exige parceiro habilitado e novo gate jurídico. |
| 2026-07-14 | `DEC-038` | decidida | Karma deriva de casos e resultados confirmados; não decide sozinho disputa, refund, payout ou culpa e sempre admite explicação/recurso quando material. |
| 2026-07-14 | `DEC-039` | decidida | Adotar mural `PULL_BOARD`: nenhuma designação individual, taxa de aceite ou punição por ignorar/recusar quest; sem interesse, o solicitante revisa os termos. |
| 2026-07-14 | `DEC-040` | decidida | Exibir quests públicas como área aproximada/círculo com ícone de exclamação; endereço exato só após atribuição e gates válidos, sem refinamento por zoom/consultas repetidas. |
| 2026-07-14 | `DEC-041` | decidida | Identificação veicular inclui identidade/foto verificadas, categoria e validade da CNH, EAR quando aplicável e dados necessários do veículo, sem exposição do documento bruto. |
| 2026-07-14 | `DEC-042` | decidida | Implementar audit log interno e módulo robusto de bugs/sugestões no painel admin, com adapters opcionais para suporte e issue tracker. |

## 9. Histórico de atualização

- 2026-07-14 — criação do plano inicial, backlog, arquitetura proposta e comparação de mapas/navegação.
- 2026-07-14 — navegação embutida promovida a núcleo do MVP; arquitetura revisada para native-first e preparada para AR por meio de `NavigationFrame`.
- 2026-07-14 — modelo de receita revisado: sem take rate de motoristas, publicidade local como monetização, gamificação universal e marketplace de lojas como camada futura.
- 2026-07-14 — experiência dividida em mapa-mundo para descoberta/comércio e modo corrida dedicado à navegação da quest.
- 2026-07-14 — fundação executável criada: Android nativo com Kotlin/Compose, Supabase local com migration-base, CI, documentação e validação por build, testes e lint.
- 2026-07-14 — emulador Android 16/API 36 configurado e aplicativo instalado/aberto com sucesso no aparelho virtual `medium_phone`.
- 2026-07-14 — arquitetura ampliada para quests genéricas de deslocamento e serviços, com skills, credenciais, karma contextual e elegibilidade auditável.
- 2026-07-14 — adicionados XP/proficiência por skill e governança mensal do catálogo pelo Conselho do Reino, com publicação automática segura e versionada.
- 2026-07-14 — definida verificação forte e revogável: não verificados podem publicar com alcance limitado, mas somente partes verificadas avançam para atribuição e execução.
- 2026-07-14 — mapeados candidatos brasileiros para CPF, biometria/liveness e assinatura eletrônica, mantendo seleção final condicionada a spike técnico, jurídico e de custo.
- 2026-07-14 — estabelecida política de custo zero: ambiente local/mock por padrão, demos sem cobrança e proibição de billing sem autorização explícita.
- 2026-07-14 — planejado RPG geolocalizado com avatar jogável, dungeons, parties, Gold e Chaves de Aventura, mantendo progressão profissional isolada e interação bloqueada durante navegação.
- 2026-07-14 — definida ponte unidirecional: quests concluídas podem melhorar o RPG; RPG nunca melhora requisitos, reputação ou progressão das quests.
- 2026-07-14 — aprovado estimador transparente de valor para transporte, baseado em faixa robusta dos últimos 30 dias e sempre editável pelo autor.
- 2026-07-14 — definido cold start de preços sem referência artificial: serviços usam propostas e medianas surgem apenas após amostra comparável suficiente.
- 2026-07-14 — catalogadas fontes externas de preço e definido pipeline versionado, auditável e sem scraping para complementar o cold start.
- 2026-07-14 — contraproposta privada promovida ao fluxo principal: autor publica objetivo/oferta e aceita atomicamente termos versionados de pessoa ou empresa elegível.
- 2026-07-14 — formalizado contrato de módulos e suíte de conformidade para expandir modalidades sem refactor da engine de quests.
- 2026-07-14 — revisão de produto recomendou cunha estreita e postergou AR, RPG, Gold, Conselho ativo, lojas, ads e automação de preços até validação do núcleo.
- 2026-07-14 — auditoria pública mapeou riscos jurídicos/fraude e propôs `PolicyGate`, compliance humano, piloto 18+ verificado, localização progressiva e zero custódia financeira.
- 2026-07-14 — definido piloto em Rio Claro com raio máximo de 50 km e zonas graduais; documentadas modalidades, liquidez, mural sem punição por recusa e descoberta por área aproximada.
- 2026-07-14 — aprovado pagamento in-app via PSP sem custódia/take rate, arquitetura robusta de casos/Karma/evidência, identificação por CNH/veículo, auditoria e canal administrativo de bugs/sugestões.
