# Decisão do provedor de navegação

Data: 2026-07-15

## Decisão

Adotar **Mapbox Navigation SDK v3 para Android** como implementação primária de `NavigationProvider`, preservando Google Maps/Waze como fallback externo e um provider simulado para desenvolvimento.

Nenhuma conta paga, token ou billing é autorizada por esta decisão. A primeira implementação usa `SimulatedNavigationProvider`; o SDK real só recebe credenciais após aprovação do `CostGuard` e confirmação de que a conta não pode gerar cobrança não autorizada.

## Motivos

- expõe `RouteProgress`, manobras, voz, rerota, alternativas, tráfego e controle de câmera;
- permite identidade visual própria para o minimapa, sem depender da UI pronta do fornecedor;
- possui predictive caching e regiões offline, úteis para conexão degradada;
- integra naturalmente com Kotlin/Android e o contrato `NavigationFrame` já planejado;
- preço medido atual é adequado a um piloto pequeno: até 100 MAU e 1.000 viagens mensais na franquia publicada;
- não impede uma futura troca: domínio, quests e renderizadores dependem dos contratos internos, não de classes Mapbox.

O Google Navigation SDK continua tecnicamente forte, mas exige billing e cobra por destino após a franquia de 1.000. Suas políticas também impõem restrições adicionais para produtos semelhantes ao Google Maps, embedded devices e navegação de veículos pesados. Para um minimapa customizado com evolução para AR, a flexibilidade do Mapbox é mais alinhada neste momento.

## Restrições arquiteturais

- classes Mapbox ficam somente em `:navigation:mapbox`;
- `:core:navigation` define `NavigationProvider`, `NavigationSession`, `NavigationFrame`, `RoutePlan` e eventos;
- `:navigation:simulation` reproduz rota, GPS degradado, rerota, pausa, background e chegada sem rede;
- UI consome `NavigationFrame`, nunca `RouteProgress` diretamente;
- telemetria registra MAU/trip/request e bloqueia nova sessão ao atingir hard cap;
- estilos, tiles, busca e geocoding seguem adapters próprios;
- AR consome o contrato interno; uso de dados/licença do fornecedor será revalidado antes de renderer público;
- navegação de van/caminhão não é autorizada pelo provider de carro comum.

## Gate de produção

Antes de ativar Mapbox real:

- conta organizacional e tokens com escopo mínimo;
- secret token apenas no Gradle/CI seguro, nunca no APK ou Git;
- public token restrito e rotacionável;
- attribution e termos revisados;
- orçamento, alertas, hard cap e kill switch;
- teste em Rio Claro de cobertura, faixas, voz, rerota, bateria e offline;
- fallback externo e modo degradado funcionando;
- revisão de preço e termos na data de ativação.

## Fontes oficiais consultadas

- Mapbox Navigation SDK Android: https://docs.mapbox.com/android/navigation/guides/
- preços do Navigation SDK: https://docs.mapbox.com/android/navigation/guides/pricing/
- navegação offline: https://docs.mapbox.com/android/navigation/guides/advanced/offline/
- Google Navigation SDK pricing: https://developers.google.com/maps/documentation/navigation/android-sdk/pricing
- políticas Google Navigation: https://developers.google.com/maps/documentation/navigation/android-sdk/policies
