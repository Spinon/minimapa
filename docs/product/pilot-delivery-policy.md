# Política da modalidade oficial `LOCAL_DELIVERY`

Data-base: 2026-07-15

## Recorte oficial

A primeira modalidade do Minimapa é `LOCAL_DELIVERY`: entrega ponto a ponto de item pequeno, permitido, de baixo valor e declarado previamente, em zonas ativas do piloto de Rio Claro. Passageiros, serviços presenciais, múltiplas paradas e carga profissional permanecem desabilitados por feature flag.

O objetivo dos limites abaixo é reduzir risco no piloto; eles não certificam capacidade legal ou física. Sempre prevalece o menor limite entre:

1. policy do Minimapa;
2. fabricante/manual e capacidade do asset;
3. compartimento de carga aprovado;
4. CNH, documentos e autorização do executor;
5. legislação federal, estadual e municipal aplicável;
6. limite específico da quest.

## Limites iniciais por modo

| Modo | Peso | Volume | Maior dimensão | Valor declarado | Estado no piloto |
| --- | ---: | ---: | ---: | ---: | --- |
| `ON_FOOT` | 5 kg | 20 L | 50 cm | R$ 200 | ativo |
| `BICYCLE` | 8 kg | 40 L | 70 cm | R$ 300 | ativo após checklist do asset |
| `E_BIKE` | 10 kg | 50 L | 80 cm | R$ 400 | ativo após checklist do asset |
| `MOTORCYCLE` | 12 kg | 60 L | deve caber integralmente no compartimento aprovado | R$ 500 | condicionado a motofrete/documentos locais |
| `CAR` | 25 kg | 120 L | 100 cm | R$ 1.000 | ativo após checklist do veículo |
| `VAN` | — | — | — | — | desabilitado; revisão posterior |
| `TRUCK` | — | — | — | — | desabilitado; revisão posterior |

Peso e dimensões são do item/embalagem; não autorizam carga externa, banco ocupado, obstrução de visão, item solto ou alteração do veículo. Moto não usa sidecar/semirreboque no piloto. Uma policy versionada poderá reduzir limites por zona, clima, horário, executor, asset ou categoria.

## Conteúdo proibido no piloto

### Financeiro e identidade

- dinheiro em espécie, moeda estrangeira e criptoativos físicos/seed phrases;
- cheques, títulos, cartões, gift cards, vouchers e instrumentos ao portador;
- joias, metais/pedras preciosas e objetos usados primordialmente como reserva de valor;
- documentos pessoais, bancários, fiscais, judiciais, chaves criptográficas ou credenciais de acesso;
- chaves de imóvel/veículo desacompanhadas de fluxo futuro específico.

### Ilícito, perigoso ou regulado

- item roubado, falsificado, contrabandeado ou cuja posse/transporte seja ilícita;
- armas, munições, explosivos, fogos de artifício e componentes controlados;
- drogas ilícitas, entorpecentes, produtos controlados e medicamentos;
- combustível, inflamável, tóxico, corrosivo, pressurizado, radioativo ou material perigoso;
- material biológico, sangue, órgão, amostra clínica, resíduo médico ou contaminante;
- animais vivos, restos mortais ou material de origem animal sujeito a controle;
- tabaco, vape, bebida alcoólica e produto com venda/entrega condicionada à idade;
- alimento que exija refrigeração, aquecimento, controle sanitário especial ou confirmação de idade;
- qualquer item não declarado, embalagem violada, vazamento, odor/sinal de risco ou incompatível com a descrição.

### Alto risco operacional

- bem acima do limite de valor, peso, volume ou dimensão;
- eletrônico ou equipamento cuja fragilidade/valor não caiba no limite declarado;
- objeto único/insubstituível, obra de arte ou item sentimental de valor não mensurável;
- pessoa, animal ou transporte com custódia especial;
- conteúdo que exija licença, cadeia de custódia, seguro ou temperatura não implementados.

## Declaração e controles

O solicitante informa categoria, descrição, peso, dimensões, valor declarado, fragilidade e confirmação de que o conteúdo não é proibido. O executor vê apenas informação suficiente antes do aceite e confirma na coleta se embalagem e descrição são coerentes; não abre embalagem fechada como regra.

Controles iniciais:

- catálogo fechado de categorias, sem texto livre como única classificação;
- termos e checkbox explícitos, não pré-marcados;
- filtros de conteúdo e revisão manual por risco;
- limite de valor/peso server-side conforme `TransportSelection`;
- direito de recusar/cancelar sem Karma negativo diante de divergência ou risco;
- denúncia, preservação proporcional de evidência e kill switch de categoria;
- nenhuma recompensa, payout ou conclusão automática quando houver conteúdo divergente.

O Minimapa não promete detectar todo ilícito. A policy reduz exposição e fornece meios de bloqueio, denúncia e cooperação legal, sem exigir que o executor se coloque em risco ou investigue o conteúdo.

## Fontes regulatórias iniciais

- Resolução Contran 943/2022 e catálogo vigente: https://www.gov.br/transportes/pt-br/pt-br/assuntos/transito/conteudo-Senatran/resolucoes-contran
- orientação do DNIT sobre carga em motocicletas: https://www.gov.br/dnit/pt-br/fique-atento-as-normas-para-o-transporte-de-cargas-para-os-motociclistas
- ANTT/RNTRC: https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/requisitos

Essas fontes são ponto de partida. A ativação real depende da matriz jurídica por município e de validação profissional.
