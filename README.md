# Minimapa

Aplicativo mobile-first de quests e navegação curva a curva com identidade medieval/fantasy. Usuários publicam quests, motoristas as aceitam e o modo de navegação remove publicidade e distrações durante o deslocamento.

O modelo de negócio não desconta porcentagem do ganho do executor. O pagador pode cobrir custos operacionais discriminados sem margem; o lucro planejado vem de publicidade local, cosméticos/microtransações e, posteriormente, lojas de comerciantes.

## Estado atual

- Aplicativo Android nativo em Kotlin e Jetpack Compose.
- Showcase local funcional: portal de entrada, mapa-fantasia simulado, pins de quests e menus clicáveis, sem depender de serviços pagos.
- Portal conectado ao Supabase Auth local com login, cadastro, confirmação por email capturado localmente e recuperação anti-enumeração; Google permanece mock, sem credenciais ou cobrança.
- Backend local com Supabase CLI, PostGIS, migration do núcleo de quests, RLS e testes pgTAP cruzados.
- CI para build, testes unitários, testes instrumentados de interface e lint do Android.
- Planejamento vivo e decisões em [`TASKS.md`](TASKS.md).
- Arquitetura do motor extensível em [`docs/architecture/quest-engine.md`](docs/architecture/quest-engine.md).
- Governança do catálogo em [`docs/architecture/community-governance.md`](docs/architecture/community-governance.md).
- RPG geolocalizado e dungeons em [`docs/architecture/location-rpg.md`](docs/architecture/location-rpg.md).
- Referências externas de preço em [`docs/architecture/external-price-references.md`](docs/architecture/external-price-references.md).
- Confiança e verificação em [`docs/architecture/trust-and-verification.md`](docs/architecture/trust-and-verification.md).
- Entrada, Supabase Auth e shell do mapa-fantasia em [`docs/architecture/authentication-entry-and-map-shell.md`](docs/architecture/authentication-entry-and-map-shell.md).
- Garagem, frota e organizações em [`docs/architecture/transport-and-organizations.md`](docs/architecture/transport-and-organizations.md).
- Decisão Mapbox e contrato de navegação em [`docs/architecture/navigation-provider-decision.md`](docs/architecture/navigation-provider-decision.md).
- Revisão de produto e recorte do MVP em [`docs/product/product-scope-review.md`](docs/product/product-scope-review.md).
- Piloto de Rio Claro, modalidades e liquidez em [`docs/product/rio-claro-pilot.md`](docs/product/rio-claro-pilot.md).
- Limites e conteúdo permitido da entrega em [`docs/product/pilot-delivery-policy.md`](docs/product/pilot-delivery-policy.md).
- Operação solo do piloto em [`docs/product/solo-pilot-operations.md`](docs/product/solo-pilot-operations.md).
- Pagamentos, cancelamentos e suporte em [`docs/architecture/payments-and-remedies.md`](docs/architecture/payments-and-remedies.md).
- Karma, casos e evidências em [`docs/architecture/karma-cases-and-evidence.md`](docs/architecture/karma-cases-and-evidence.md).
- Auditoria interna, bugs e sugestões em [`docs/architecture/operations-audit-feedback.md`](docs/architecture/operations-audit-feedback.md).
- Auditoria jurídica/antifraude do desenho em [`docs/security/public-risk-audit.md`](docs/security/public-risk-audit.md).
- Política de custo zero em [`docs/development/cost-policy.md`](docs/development/cost-policy.md).
- Mapbox Navigation SDK v3 escolhido como provider primário; desenvolvimento começa com provider simulado e custo bloqueado.

## Estrutura

```text
apps/android/       Aplicativo Android nativo
  core/config/      Configuração segura, SimulationMode e CostGuard
  core/domain/      Lifecycle universal, eventos e tipos estáveis de quest
  core/policy/      PolicyGate global e autorização de transições
  core/quest-contract/ Contratos versionados, capabilities, schemas e registry
  testing/          Simuladores e suíte de conformidade de módulos
supabase/           Configuração local e futuras migrations
.github/workflows/  Integração contínua
TASKS.md            Backlog, critérios e decisões
```

## Pré-requisitos

- JDK 17
- Android Studio e Android SDK 36
- Node.js 20 ou mais recente
- Docker Desktop para executar o Supabase local

## Começando

Instale as ferramentas do workspace:

```bash
npm install
```

Inicie o backend local (requer Docker em execução):

```bash
npm run supabase:start
```

No Windows, valide e gere o APK Android:

```powershell
cd apps/android
.\gradlew.bat test lintDebug assembleDebug
```

### Testar no emulador do Windows

Na primeira vez, instale a imagem Android 16/API 36 e crie o celular virtual:

```powershell
npm run android:setup-emulator
```

Depois, um único comando inicia e acorda o emulador quando necessário, compila, instala e abre o Minimapa:

```powershell
npm run android:run
```

Para executar testes e lint antes de abrir o aplicativo, use:

```powershell
npm run test:open
```

Esse é o comando padrão usado quando o responsável pede para abrir um teste: ele acorda e desbloqueia o AVD, injeta somente a configuração pública do Supabase local, executa testes unitários e instrumentados de interface, lint e build, instala o APK e deixa o aplicativo aberto para inspeção manual. Os testes dos fluxos alterados devem ser atualizados em todo round de desenvolvimento.

Na demonstração atual, toque em **TOQUE PARA ENTRAR** para abrir o login. **ENTRAR COM CONTA DE TESTE** abre o mapa sem representar autenticação real. O mapa usa dados locais simulados: os três pins abrem detalhes de quest, o botão `!` lateral abre as ações e os demais atalhos abrem personagem e configurações. O login por email já usa o Supabase local; mapa externo entra nos incrementos seguintes.

Emails de confirmação e recuperação não são enviados para a internet. Com o Supabase local iniciado, eles aparecem no Mailpit em `http://127.0.0.1:54324`.

O dispositivo criado se chama `medium_phone` e usa uma imagem x86_64 com Google Play, adequada aos futuros testes de mapas e localização.

No macOS ou Linux:

```bash
cd apps/android
./gradlew test lintDebug assembleDebug
```

Também é possível abrir `apps/android` diretamente no Android Studio. O arquivo `local.properties` é local e não deve ser versionado.

## Segredos e serviços externos

Nenhuma chave de mapa, token de serviço ou credencial deve entrar no Git. O repositório contém somente configuração local segura. Conta/tokens Mapbox e credenciais cloud serão criados apenas quando o provider simulado passar e houver aprovação explícita dos gates de custo.

Durante o desenvolvimento, integrações externas usam mocks ou sandboxes garantidamente gratuitas. Billing, cartões, trials com conversão automática e chamadas faturáveis permanecem desabilitados por padrão; consulte a política de custo antes de integrar qualquer fornecedor.

## Licença

Ainda não foi definida uma licença de código aberto. Tornar o repositório público não concede automaticamente permissão de uso, modificação ou redistribuição.
