# Piloto de Rio Claro

Data-base: 2026-07-14

## Decisões confirmadas

- base operacional em Rio Claro, SP;
- área técnica máxima de 50 km a partir de um centro versionado;
- Android, maiores de 18 anos e identidade verificada nas duas pontas;
- mural aberto de quests: o executor escolhe o que deseja analisar, aceitar ou contra-ofertar;
- nenhuma punição por ignorar ou recusar uma quest;
- pagamento dentro do aplicativo por PSP de marketplace, sem custódia pelo Minimapa e sem percentual sobre a remuneração;
- sem garantia comercial ou seguro do Minimapa no piloto; um futuro Minimap Plus dependerá de seguradora/parceiro habilitado;
- itens e serviços obedecem à legislação federal, estadual e municipal aplicável a cada trecho, não somente às regras de Rio Claro.

O raio de 50 km cruza municípios. Por isso, `PilotServiceArea` é apenas uma fronteira geográfica preliminar. `JurisdictionPolicy` deve avaliar origem, destino, rota, categoria e vigência antes de publicar, atribuir e iniciar. Passageiros ficam fechados até confirmação direta das regras atuais com a Secretaria Municipal de Mobilidade de Rio Claro e cada município alcançado.

## O que significa modalidade

Modalidade é uma família operacional de quests. Ela determina papéis, dados, lifecycle específico, evidências, credenciais e policies, enquanto o `QuestCore` continua genérico.

| Modalidade | Particularidades | Exemplos de definição |
| --- | --- | --- |
| `LOCAL_DELIVERY` | custódia do item, coleta, entrega, conteúdo proibido, veículo e prova de entrega | pacote pequeno, compra em comércio local |
| `PASSENGER_TRANSPORT` | passageiro, CNH/EAR, veículo, cadastro municipal, segurança pessoal e seguro | deslocamento urbano de passageiro |
| `ON_SITE_SERVICE` | escopo, agenda, materiais, entrada em imóvel e credencial | encanamento, montagem, manutenção |
| `REMOTE_SERVICE` | entregáveis digitais, marcos e aceite sem navegação | design, revisão, consultoria |
| `MARKETPLACE_FULFILLMENT` | pedido de loja, estoque, fiscal, pós-venda e entrega vinculada | compra e entrega de uma loja do mapa |

“Transporte” não deve ser uma única modalidade: transportar uma pessoa e entregar um item têm riscos e obrigações diferentes. A recomendação permanece iniciar com `LOCAL_DELIVERY`, em uma única definição de item pequeno, permitido e de baixo valor. A decisão final da modalidade continua bloqueada até o checklist jurídico/operacional local; `PASSENGER_TRANSPORT` não entra por consequência automática do raio escolhido.

## Área, zonas e liquidez

Executor é o papel neutro de quem realiza uma quest. Na interface ele pode aparecer como aventureiro, entregador, motorista ou profissional conforme a modalidade.

Liquidez é a chance de uma quest válida encontrar, no lugar e horário certos, um executor elegível que aceite os termos ou faça uma contraproposta em tempo útil. Ter muitos cadastros não basta: eles podem estar longe, offline, sem o veículo ou credencial necessária, ou não interessados naquele preço.

O piloto terá duas escalas:

1. **Área permitida:** círculo máximo de 50 km, sujeito a recortes por município e policy.
2. **Zonas de lançamento:** células menores ativadas gradualmente, começando pela área central com oferta recrutada. Uma quest fora de zona ativa pode ser salva, mas não publicada.

Métricas mínimas por zona, janela e modalidade:

- executores elegíveis e disponíveis;
- quests publicadas, visualizadas e sem proposta;
- tempo até primeira proposta e até acordo;
- taxa de preenchimento e conclusão;
- diferença entre oferta inicial e acordo;
- quilômetros vazios e tempo total do executor;
- cancelamentos e disputas por motivo.

Metas numéricas serão definidas após um dry run. Como hipótese inicial, não abrir uma zona/janela sem pelo menos três executores elegíveis confirmados e suporte disponível. Isso não vira promessa ao usuário nem prioridade algorítmica.

## Descoberta sem endereço exato

No mapa público, cada quest usa `QuestDiscoveryArea`:

- círculo ou célula aproximada com um ícone de exclamação fantasy;
- centro visual estável e deslocado do endereço real;
- raio calibrado por densidade e risco, sem revelar a posição ao ampliar o mapa;
- distância, ETA e trajeto apenas em faixas antes do acordo;
- endereço exato liberado somente depois de atribuição, pagamento/condições válidas e finalidade ativa.

O backend nunca envia coordenada exata para o cliente de descoberta. Consultas repetidas, filtros e mudanças de zoom retornam a mesma área, impedindo triangulação. O raio de privacidade da quest não se confunde com os 50 km da área do piloto.

## Marketplace por escolha, não despacho

O Minimapa adota estratégia `PULL_BOARD` no piloto:

- quests elegíveis aparecem no mural e no mapa aproximado;
- notificações podem avisar grupos que escolheram filtros, mas não constituem designação individual;
- não existe taxa de aceite, fila oculta, penalidade por recusa ou obrigação de justificar desinteresse;
- só há registro quando o executor envia proposta, aceita ou abre a quest para fins legítimos de segurança/analytics minimizado;
- sem interessados, o solicitante pode revisar valor, janela, escopo ou retirar a quest, criando nova versão auditável.

Isso preserva autonomia real. O Minimapa ainda pode suspender alguém por fraude ou segurança comprovada, com processo e recurso, mas nunca por não querer uma oportunidade.

## CNH, identidade e veículo

Para uma modalidade veicular, a identificação equivalente à experiência de apps de transporte inclui:

- identidade e selfie verificadas no onboarding e reverificação baseada em risco;
- CNH válida, categoria compatível com o veículo e indicador EAR quando legalmente exigido;
- veículo aprovado, com marca/modelo, cor e placa exibidos no momento apropriado;
- foto atual do executor e sinais claros de divergência/denúncia;
- documentos com expiração e bloqueio server-side quando vencidos.

O usuário vê somente os atributos necessários à segurança. Número da CNH, documento bruto e biometria não são públicos. O domínio guarda claims verificados, validade e referência do provedor; evidências brutas têm acesso e retenção restritos. O Datavalid é candidato futuro porque valida dados cadastrais, categoria e validade da CNH e biometria, mas permanece apenas em adapter/mock até autorização de custo.

Cada executor pode manter vários meios cadastrados, marcar uma seleção favorita e escolher o asset adequado para cada quest. Empresas podem manter frota e indicar um membro executor, desde que ele possua conta própria, tenha aceitado o vínculo no app e permaneça pessoalmente elegível. Contratado, despachante, executor, asset e beneficiário do pagamento aparecem como papéis distintos no acordo e na auditoria. O desenho está em `docs/architecture/transport-and-organizations.md`.

## Pendências bloqueantes

- confirmar modalidade inicial e limite de valor/tamanho;
- obter validação jurídica por município/rota, inclusive transporte, fiscal, consumidor e seguro;
- definir centro geográfico, polígonos excluídos e primeiras zonas ativas;
- definir horário do piloto, escala de suporte e coorte recrutada;
- fechar quem paga a tarifa do PSP e como ela é mostrada;
- aprovar termos que descrevam ausência de seguro/garantia adicional sem tentar afastar direitos legais.

## Fontes oficiais consultadas

- Prefeitura de Rio Claro, Mobilidade Urbana: https://rioclaro.sp.gov.br/secretaria/secretaria-de-mobilidade-urbana-e-sistema-viario/
- histórico municipal de regulamentação de transporte individual: https://rioclaro.sp.gov.br/secretarias/gabinete-do-prefeito/prefeito-sanciona-lei-que-autoriza-o-uber-em-rio-claro/
- Lei 13.640/2018: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13640.htm
- ANTT, RNTRC: https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/requisitos
- ANTT, CIOT: https://www.gov.br/antt/pt-br/assuntos/cargas/ciot-para-todos-1
- Serpro Datavalid V4/CNH: https://centraldeajuda.serpro.gov.br/duvidas/pt/avisos/avisodatavalidv4/
