# Referências externas de preço

## Objetivo

Fontes públicas ou profissionais podem reduzir o cold start de algumas categorias. Elas não substituem propostas reais nem o histórico do Minimapa e nunca são combinadas em uma média opaca. Cada valor aparece com origem, competência, território, unidade, metodologia e natureza jurídica.

## Tipos de referência

1. **Piso obrigatório:** limite legal aplicável somente quando a operação satisfaz seu escopo.
2. **Custo de referência:** composição de mão de obra, materiais e equipamentos; não equivale necessariamente ao preço final ao consumidor.
3. **Honorário indicativo:** orientação de entidade profissional, sem ser apresentado como preço obrigatório quando não tiver essa natureza.
4. **Tarifa pública regulada:** preço de um serviço público ou modalidade específica; serve apenas ao território e serviço correspondentes.
5. **Histórico do Minimapa:** faixa estatística de quests comparáveis concluídas, com amostra e confiança.

O produto deve mostrar essas camadas separadamente. Quando houver piso legal aplicável, ele atua como restrição de conformidade, não como mais uma observação estatística.

## Fontes candidatas verificadas

### SINAPI — CAIXA e IBGE

- Escopo: custos e índices da construção civil, incluindo composições, insumos, materiais, equipamentos e mão de obra.
- Atualização: relatórios mensais por unidade da federação.
- Uso potencial: encanamento, elétrica, pintura, alvenaria e outros serviços mapeáveis a composições canônicas.
- Limite: é referência de custo para obras/serviços de engenharia, não orçamento varejista pronto. Escopo, mobilização, margem, tributos e condições locais podem alterar o preço final.
- Integração inicial: importação manual e reprodutível dos XLSX/ZIP oficiais; automatizar somente após validar formato, termos e estabilidade.
- Fonte: https://www.caixa.gov.br/poder-publico/modernizacao-gestao/sinapi/Paginas/default.aspx
- Metodologia/estatísticas: https://www.ibge.gov.br/estatisticas/economicas/precos-e-custos/9270-sistema-nacional-de-pesquisa-de-custos-e-indices-da-construcao-civil.html

### Pisos mínimos de frete — ANTT

- Escopo: operações de transporte rodoviário de cargas alcançadas pela legislação e regulamentação da ANTT.
- Uso potencial: compliance e referência mínima quando a quest se enquadrar juridicamente na operação regulada.
- Limite: não se aplica automaticamente a passageiros, pequenas entregas urbanas ou qualquer quest que apenas mova um item. O enquadramento precisa ser validado.
- Integração inicial: cálculo isolado por versão dos coeficientes oficiais; não chamar ou reproduzir calculadora sem API/termos adequados.
- Fonte: https://www.gov.br/antt/pt-br/assuntos/cargas/politica-nacional-de-pisos-minimos-de-frete

### Tarifas municipais de táxi

- Escopo: bandeirada, quilômetro e hora parada publicados pelo município para táxis.
- Uso potencial: referência pública local de custo de deslocamento, sempre rotulada como tarifa de táxi — não como tarifa obrigatória do Minimapa.
- Limite: varia por município, modalidade, bandeira e vigência e pode não representar transporte por aplicativo, entrega ou frete.
- Integração inicial: cadastro manual da norma e seus componentes para a cidade piloto, com revisão de vigência.
- Exemplo oficial de São Paulo: https://prefeitura.sp.gov.br/documents/d/mobilidade/cmtt_ct_taxi_25-02-2025_p-pdf

### Tabelas de entidades profissionais

- Escopo: honorários indicativos para profissões e atividades regulamentadas.
- Uso potencial: referência em categorias exatamente mapeadas e executadas por profissional habilitado.
- Limite: verificar atualização, território, acesso, licenciamento, caráter indicativo e eventuais questões concorrenciais. Não raspar calculadoras fechadas ou que exijam cadastro.
- Exemplo: calculadora gratuita da Tabela de Honorários do CAU/BR para arquitetura e urbanismo.
- Fonte: https://transparencia.caubr.gov.br/cartadeservicos4-1/

## Modelo de dados

- `ExternalPriceSource`: publicador, tipo, URL canônica, termos/licença, método de obtenção e responsável pela revisão.
- `ExternalPriceSourceVersion`: competência, vigência, território, checksum do artefato, momento de importação e estado de aprovação.
- `ExternalPriceReference`: unidade, componentes, valor/faixa, moeda, tributos/encargos e fórmula.
- `ExternalPriceCategoryMapping`: vínculo revisado entre referência externa e versão canônica de serviço/quest.
- `PriceSuggestionSnapshot`: fotografia das referências internas e externas efetivamente exibidas ao usuário.

O dado importado é imutável por versão. Correções criam nova versão ou evento compensatório; não sobrescrevem a evidência usada por uma quest publicada.

## Pipeline seguro e sem custo

1. cadastrar a fonte e classificar sua natureza;
2. baixar artefato oficial manualmente no protótipo;
3. validar checksum, formato, competência, unidade e território;
4. mapear categorias com revisão humana;
5. executar testes de fixtures sem rede no CI;
6. publicar a versão somente após revisão;
7. monitorar expiração e retirar referência vencida;
8. automatizar apenas quando houver download/API estável e uso permitido.

Nenhuma importação autoriza billing. Scraping de concorrentes, marketplaces ou calculadoras protegidas não faz parte do plano.

## Apresentação

Exemplo para um reparo elegível:

> Histórico do Minimapa: R$ 120–160 · 48 serviços comparáveis
>
> Referência externa de custo: SINAPI/CAIXA · SP · 06/2026
>
> Materiais e deslocamento: calculados separadamente

O usuário pode abrir metodologia e origem. O sistema não soma ou mistura referências incompatíveis silenciosamente. Se houver divergência relevante, mostra ambas e reduz a confiança. Sem fonte aplicável e sem amostra interna, mantém orçamento/propostas sem sugestão.
