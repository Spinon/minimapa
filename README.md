# Minimapa

Aplicativo mobile-first de quests e navegação curva a curva com identidade medieval/fantasy. Usuários publicam quests, motoristas as aceitam e o modo de navegação remove publicidade e distrações durante o deslocamento.

O modelo de negócio não desconta uma porcentagem do ganho do motorista. A monetização planejada vem de publicidade local claramente identificada, cosméticos e, posteriormente, lojas de comerciantes.

## Estado atual

- Aplicativo Android nativo em Kotlin e Jetpack Compose.
- Backend local preparado com Supabase CLI, Postgres e fluxo de migrations.
- CI para build, testes unitários e lint do Android.
- Planejamento vivo e decisões em [`TASKS.md`](TASKS.md).
- Arquitetura do motor extensível em [`docs/architecture/quest-engine.md`](docs/architecture/quest-engine.md).
- Governança do catálogo em [`docs/architecture/community-governance.md`](docs/architecture/community-governance.md).
- Escolha entre Google Navigation SDK e Mapbox Navigation SDK reservada para um spike comparativo.

## Estrutura

```text
apps/android/       Aplicativo Android nativo
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

Depois, um único comando inicia o emulador quando necessário, compila, instala e abre o Minimapa:

```powershell
npm run android:run
```

O dispositivo criado se chama `medium_phone` e usa uma imagem x86_64 com Google Play, adequada aos futuros testes de mapas e localização.

No macOS ou Linux:

```bash
cd apps/android
./gradlew test lintDebug assembleDebug
```

Também é possível abrir `apps/android` diretamente no Android Studio. O arquivo `local.properties` é local e não deve ser versionado.

## Segredos e serviços externos

Nenhuma chave de mapa, token de serviço ou credencial deve entrar no Git. O repositório contém somente configuração local segura. Projetos cloud, chaves restritas de Android e credenciais de Google/Mapbox serão criados quando o provedor de navegação for escolhido.

## Licença

Ainda não foi definida uma licença de código aberto. Tornar o repositório público não concede automaticamente permissão de uso, modificação ou redistribuição.
