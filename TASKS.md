# TASKS — marketplace de quests

Última atualização: 2026-07-14

Este arquivo é a fonte de verdade do planejamento. Ele deve ser atualizado em toda sessão de desenvolvimento que altere escopo, arquitetura, status ou decisões.

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

### Dois modos da experiência

O aplicativo possui duas interfaces de mapa, com objetivos e densidades diferentes:

1. **Mapa-mundo / exploração:** busca de conteúdo, lojas, lugares, quests, avatares, pins patrocinados, filtros e planejamento. É a superfície social, comercial e de descoberta.
2. **Modo corrida / quest ativa:** curva a curva, rota, posição, próxima manobra, faixas, voz, velocidade, ETA e status essencial da quest. Lojas, publicidade, recompensas e conteúdo de exploração ficam ocultos.

Fluxo principal:

`exploração → detalhe da quest → aceite/confirmação → modo corrida → chegada/conclusão → exploração`

A mudança de modo precisa ser explícita, rápida e reversível quando seguro. O motor de mapa pode ser compartilhado, mas cada modo tem seu próprio conjunto de camadas, controles, câmera e regras de interação.

### Hipótese do MVP

Começar em uma única cidade/região e com **um só tipo de quest ponto A → ponto B**. Antes da implementação, escolher se o primeiro caso será transporte de passageiros, entrega de itens ou pequenos serviços. Misturar os três no primeiro MVP aumenta regras, telas, operação e risco jurídico.

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

### Três economias separadas

1. **XP/nível:** ganho por atividade; não transferível, não comprável e sem valor monetário.
2. **Gemas:** moeda virtual fechada para cosméticos digitais. Pode ser comprada, mas não sacada, transferida entre usuários ou usada para pagar produtos físicos.
3. **Dinheiro real:** pagamentos de publicidade e marketplace ficam em ledgers e provedores próprios, separados de XP/gemas.

“Gemas” podem reforçar o tema, mas não devem esconder o preço real. A tela de compra deve mostrar quantidade, preço em moeda local, equivalência dos itens, saldo, histórico e política de reembolso. Para lojistas, preferir uma **Licença de Guilda** ou plano comercial com preço transparente em um portal B2B, em vez de misturar capital da loja com gemas de cosméticos.

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

### Fora do primeiro MVP

- Realidade aumentada em produção; o MVP prepara o contrato e valida um spike técnico separado.
- Preço dinâmico, leilão ou contraproposta.
- Quests com múltiplas paradas.
- Grupos, guildas sociais, ranking complexo e itens colecionáveis.
- Divisão de tarifa, assinatura, carteira e programa de indicação.
- Expansão para várias cidades antes de validar a operação local.
- Marketplace completo com catálogo, estoque, split/payout, entrega, reembolso e disputa.
- Compra de gemas com dinheiro real; o beta pode validar progressão e cosméticos apenas com recompensas conquistadas.

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
- Digital: Apple In-App Purchase e Google Play Billing para gemas/cosméticos comprados dentro do app, conforme as políticas vigentes.
- Marketplace físico: provedor de pagamentos com suporte a marketplace, split/payout, estorno e KYC; não usar billing das lojas para bens e serviços físicos.
- Anunciantes/lojistas: portal web B2B separado para campanhas, faturamento, catálogo e operação. Validar as regras de links/compra externa antes de apontar o app consumidor para esse portal.
- Durante o protótipo, usar sandbox e não movimentar dinheiro real.

### Entidades principais

- `profiles`
- `driver_profiles`
- `vehicles`
- `driver_documents`
- `quests`
- `quest_assignments`
- `quest_events`
- `driver_locations`
- `navigation_sessions`
- `navigation_events`
- `messages`
- `ratings`
- `avatars`
- `cosmetic_items`
- `avatar_inventory`
- `xp_ledger`
- `virtual_wallets`
- `virtual_currency_ledger`
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

Os ledgers de XP, gemas e dinheiro real devem ser separados. Saldos não podem ser atualizados diretamente pelo cliente; toda concessão, compra, consumo, reembolso ou ajuste administrativo gera uma entrada imutável e idempotente.

### Máquina de estados da quest

`draft → open → accepted → driver_en_route → driver_arrived → in_progress → completed`

Saídas excepcionais: `cancelled` e `disputed`. Toda transição deve ser validada no servidor e registrada em `quest_events`.

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

Um controlador de alto nível deve coordenar os dois produtos de mapa sem misturar suas responsabilidades:

- `ExploreMode`: conteúdo, quests, lojas, campanhas, busca e câmera livre.
- `RoutePreviewMode`: origem/destino, alternativas, distância e confirmação, ainda sem orientação ativa.
- `ActiveQuestMode`: `NavigationSession` ativa, câmera de seguimento, HUD mínimo e nenhuma camada comercial.
- `ArrivalMode`: confirmação de chegada/conclusão; só então retorna ao mapa-mundo.

As camadas do `ExploreMode` não recebem eventos durante `ActiveQuestMode`. Isso reduz distração, uso de rede/GPU e risco de um anúncio aparecer por engano sobre uma manobra.

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
- [ ] `PRD-002` Definir cidade/região piloto, público e hipótese de valor.
- [ ] `PRD-003` Definir regras de preço/recompensa, cancelamento e no-show.
- [ ] `PRD-004` Validar requisitos jurídicos, seguro, verificação de motoristas, LGPD e termos locais.
- [ ] `PRD-005` Desenhar jornadas e critérios de sucesso do beta.
- [x] `ADR-001` Aprovar Android nativo como primeira plataforma e definir a sequência iOS/web.
- [ ] `ADR-002` Escolher o Navigation SDK após o spike e definir limites de gasto.
- [ ] `ADR-003` Decidir se pagamento real entra no beta fechado.
- [ ] `PRD-006` Definir se a navegação/AR futura é para motorista, passageiro, pedestre ou dispositivo montado; isso altera requisitos de segurança.
- [x] `BUS-001` Registrar que o Minimapa não recebe percentual do valor pago ao motorista.
- [ ] `BUS-002` Definir se o pagamento da quest acontece fora da plataforma ou como repasse transparente sem receita de take rate.
- [ ] `ADS-001` Definir inventário inicial: busca, mapa de exploração, mural, pré-rota e chegada; excluir navegação ativa.
- [ ] `ADS-002` Definir modelo comercial do piloto: venda direta/faturada, CPM, período fixo ou destaque regional.
- [ ] `ECO-001` Aprovar separação entre XP, gemas e dinheiro real.
- [ ] `POL-001` Validar App Store/Google Play para publicidade, moeda virtual, cosméticos e marketplace físico.
- [ ] `MKT-001` Definir o recorte futuro de lojas: tipos de produto, logística, responsabilidade e modelo de receita do lojista.

### Fase 1 — conceito visual e fundação (estimativa: 1 semana)

- [ ] `DSN-001` Criar conceito visual completo para os fluxos principais, mobile e web.
- [ ] `DSN-002` Aprovar tokens: cores, tipografia, espaçamento, ícones e tom fantasy.
- [ ] `DSN-003` Definir componentes e estados de acessibilidade/contraste.
- [ ] `DSN-004` Projetar estados do minimapa: exploração, rota, manobra, rerota, GPS degradado e chegada.
- [ ] `DSN-005` Projetar avatar, níveis, loadout cosmético e recompensas sem pay-to-win.
- [ ] `DSN-006` Projetar pins/locais patrocinados com identificação inequívoca e baixa densidade.
- [ ] `DSN-007` Projetar shells distintos para mapa-mundo e modo corrida, incluindo transição, retorno e estados interrompidos.
- [~] `DEV-001` Inicializar repositório, convenções, lint, format, testes e CI. Repositório, lint, testes e CI prontos; formatter dedicado ainda pendente.
- [~] `DEV-002` Inicializar aplicativo Android nativo com Kotlin, Jetpack Compose e módulos por domínio. App-base compilando; modularização por domínio será feita junto aos primeiros módulos reais.
- [~] `DEV-003` Configurar ambientes local, staging e produção sem versionar segredos. Ambiente local e regras de exclusão prontos; staging e produção pendentes.
- [ ] `DEV-004` Criar navegação, tema e componentes básicos conforme o conceito aprovado.
- [ ] `DEV-005` Definir contratos de API compartilháveis e fronteira do futuro cliente iOS/web.
- [x] `DEV-006` Configurar emulador Android local e fluxo de um comando para compilar, instalar e abrir o aplicativo.

### Fase 2 — backend, autenticação e perfis (estimativa: 1–2 semanas)

- [x] `DB-001` Inicializar Supabase local e fluxo de migrations.
- [ ] `DB-002` Criar schema inicial e extensão PostGIS.
- [ ] `SEC-001` Ativar RLS e políticas mínimas para todas as tabelas expostas.
- [ ] `AUTH-001` Implementar cadastro, login, recuperação e encerramento de sessão.
- [ ] `AUTH-002` Implementar perfis de usuário e motorista sem confiar em roles editáveis pelo cliente.
- [ ] `DRV-001` Implementar veículo, documentos e estado de aprovação.
- [ ] `TST-001` Testar RLS com usuários distintos e tentativas de acesso indevido.

### Fase 3 — spikes de navegação, mapas e criação de quest (estimativa: 2–3 semanas)

- [ ] `NAV-001` Fazer spike Android do Google Navigation SDK com experiência pronta e feed curva a curva customizado.
- [ ] `NAV-002` Fazer spike Android do Mapbox Navigation SDK v3 com `RouteProgress`, manobras, voz e rerota.
- [ ] `NAV-003` Testar ambos em rotas reais da região piloto e registrar precisão, latência, cobertura, bateria e custo projetado.
- [ ] `NAV-004` Registrar ADR escolhendo o SDK e documentando como trocar de fornecedor.
- [ ] `AR-001` Fazer spike de ARCore Geospatial com uma manobra simulada derivada de `NavigationFrame`.
- [ ] `MAP-002` Implementar o mapa Android com o SDK de navegação escolhido.
- [ ] `MAP-003` Implementar busca/autocomplete e geocodificação reversa.
- [ ] `MAP-004` Implementar cálculo e desenho de rota com distância/ETA.
- [ ] `MAP-005` Criar adaptadores de provedor e telemetria de consumo/custo.
- [ ] `MAP-006` Implementar `ExploreMode` com busca, quests, lojas/pontos, filtros, clustering e camadas patrocinadas.
- [ ] `MAP-007` Implementar `RoutePreviewMode` sem iniciar cobrança/sessão de navegação prematuramente.
- [ ] `QST-001` Implementar criação, validação, prévia e publicação de quest.
- [ ] `QST-002` Implementar histórico e detalhe para quem publicou.
- [ ] `SEC-002` Manter chaves secretas de mapas/rotas no servidor quando exigido e restringir chaves públicas por app/domínio.

### Fase 4 — mural e aceite (estimativa: 1–2 semanas)

- [ ] `GEO-001` Consultar quests abertas por raio usando índice espacial.
- [ ] `BRD-001` Implementar mural/lista/mapa de quests para motoristas.
- [ ] `BRD-002` Implementar filtros essenciais e detalhe da quest.
- [ ] `QST-003` Implementar aceite atômico no banco (`open → accepted`).
- [ ] `QST-004` Tratar corrida de dois motoristas aceitando a mesma quest.
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

### Fase 6 — confiança, gamificação e piloto publicitário (estimativa: 2–3 semanas)

- [ ] `ADM-001` Criar painel de operação protegido.
- [ ] `ADM-002` Implementar aprovação/bloqueio de motorista e auditoria.
- [ ] `RAT-001` Implementar avaliações bilaterais e denúncia.
- [ ] `GAM-001` Definir fontes de XP, curvas de nível, limites diários e controles antiabuso.
- [ ] `GAM-002` Implementar avatar, inventário e loadout cosmético.
- [ ] `GAM-003` Implementar ledger imutável de XP e concessão idempotente de recompensas.
- [ ] `GAM-004` Entregar cosméticos conquistáveis no beta, sem compra com dinheiro real.
- [ ] `ADS-003` Implementar anunciantes, campanhas e locais patrocinados administrados manualmente.
- [ ] `ADS-004` Renderizar conteúdo patrocinado apenas em superfícies permitidas, com rótulo e limite de densidade.
- [ ] `ADS-005` Registrar impressões/interações agregadas, frequência e orçamento sem trilha individual vendável.
- [ ] `ADS-006` Criar relatório simples para validar valor com os primeiros anunciantes locais.
- [ ] `PRV-001` Definir retenção e exclusão de localização/documentos conforme LGPD.
- [ ] `SAF-001` Implementar contato de emergência e compartilhamento da quest, se aplicável ao tipo escolhido.

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
- [ ] `BET-001` Rodar beta fechado em uma região com suporte manual.
- [ ] `BET-002` Revisar métricas e decidir avançar, ajustar ou reduzir o escopo.

### Fase 8 — pós-validação

- [ ] `AR-002` Transformar corredor/manobras da rota em pontos geoespaciais seguros para renderização.
- [ ] `AR-003` Implementar renderizador experimental com ARCore Geospatial e fallback quando VPS/precisão não estiver disponível.
- [ ] `AR-004` Avaliar ARKit/ARCore no iOS e diferenças de cobertura geográfica.
- [ ] `AR-005` Medir precisão, deriva, oclusão, bateria, temperatura e segurança antes de qualquer beta público.
- [ ] `NAV-011` Avaliar navegação offline e pacotes regionais conforme o SDK escolhido.
- [ ] `MCH-001` Avaliar matching/recomendação automática sem remover o mural de quests.
- [ ] `PRC-001` Avaliar preço sugerido/dinâmico com regras transparentes.
- [ ] `SCL-001` Testar carga, particionamento/retenção de localizações e expansão regional.
- [ ] `GAM-005` Avaliar progressão social, temporadas e novas categorias cosméticas sem prejudicar confiança.

### Fase 9 — economia digital e marketplace de lojas

- [ ] `ECO-002` Definir nome, pacotes, sinks, não expiração e política de reembolso das gemas.
- [ ] `ECO-003` Implementar wallet/ledger de gemas sem transferência ou saque entre usuários.
- [ ] `IAP-001` Integrar Apple In-App Purchase e Google Play Billing para gemas/cosméticos digitais.
- [ ] `IAP-002` Implementar validação server-side, restore, reembolso e prevenção de replay de recibos.
- [ ] `MER-001` Criar portal web B2B para onboarding, Licença de Guilda, campanhas e gestão da loja.
- [ ] `MER-002` Implementar loja, catálogo, variações, estoque, disponibilidade e moderação.
- [ ] `PAY-001` Selecionar provedor de marketplace com KYC, split/payout, estorno e conciliação.
- [ ] `PAY-002` Implementar pagamentos físicos em sandbox e webhooks idempotentes.
- [ ] `PAY-003` Implementar recibo, cancelamento, reembolso, disputa e reconciliação.
- [ ] `ORD-001` Implementar pedido, confirmação do lojista, separação e status de fulfillment.
- [ ] `DLV-001` Converter pedido pronto em quest de entrega sem descontar comissão do motorista.
- [ ] `DLV-002` Confirmar coleta/entrega, prova mínima e resolução de falhas.
- [ ] `MOD-001` Implementar moderação de lojas, produtos, avaliações e conteúdo gerado por usuários.

## 6. Critérios de pronto para o beta

- Um usuário consegue publicar uma quest válida em menos de dois minutos.
- Somente um motorista consegue aceitar a mesma quest, inclusive sob concorrência.
- Origem, destino, preço, motorista e veículo permanecem claros em todas as telas críticas.
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
- Existe um piloto publicitário manual mensurável antes de construir compra self-service de campanhas.

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
- Gemas usadas para ocultar preço real podem ser consideradas experiência enganosa. Preço, equivalência, saldo e reembolso devem ser transparentes.
- Bens digitais, cosméticos e funcionalidades desbloqueadas no app normalmente exigem Apple IAP/Google Play Billing; bens e serviços físicos seguem outro fluxo de pagamento.
- Misturar gemas com saldo de lojista, repasse ou remuneração de motorista cria risco contábil, fraude e possível enquadramento financeiro.
- Marketplace traz KYC, chargeback, fraude, moderação, tributação, defesa do consumidor, logística e responsabilidade por produtos.
- Publicidade baseada em localização precisa pode revelar padrões sensíveis; preferir contexto em tempo real e métricas agregadas.
- O app deve declarar publicidade às lojas e distinguir claramente posicionamento pago de resultado orgânico.

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
| 2026-07-14 | `DEC-012` | proposta | Separar XP, gemas de cosméticos e dinheiro real em economias e ledgers independentes. |
| 2026-07-14 | `DEC-013` | decidida | Exibir publicidade em exploração/busca/mural/chegada, nunca durante navegação ativa. |
| 2026-07-14 | `DEC-014` | decidida | Separar o mapa em `ExploreMode` rico em conteúdo e `ActiveQuestMode` limpo para curva a curva. |

## 9. Histórico de atualização

- 2026-07-14 — criação do plano inicial, backlog, arquitetura proposta e comparação de mapas/navegação.
- 2026-07-14 — navegação embutida promovida a núcleo do MVP; arquitetura revisada para native-first e preparada para AR por meio de `NavigationFrame`.
- 2026-07-14 — modelo de receita revisado: sem take rate de motoristas, publicidade local como monetização, gamificação universal e marketplace de lojas como camada futura.
- 2026-07-14 — experiência dividida em mapa-mundo para descoberta/comércio e modo corrida dedicado à navegação da quest.
- 2026-07-14 — fundação executável criada: Android nativo com Kotlin/Compose, Supabase local com migration-base, CI, documentação e validação por build, testes e lint.
- 2026-07-14 — emulador Android 16/API 36 configurado e aplicativo instalado/aberto com sucesso no aparelho virtual `medium_phone`.
