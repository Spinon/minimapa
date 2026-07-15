# Meios de transporte e organizações

Data-base: 2026-07-15

## Objetivo

Separar papéis e recursos que não são necessariamente a mesma coisa:

- **contratado:** pessoa ou organização que firma os termos;
- **executor:** jogador verificado que realiza fisicamente a quest;
- **meio de transporte:** recurso usado naquela execução;
- **despachante:** membro autorizado a enviar proposta e selecionar recursos em nome de uma organização.
- **beneficiário:** conta KYC indicada no acordo para receber o payout.

O `QuestCore` conhece apenas referências tipadas no acordo e na atribuição. Garagem, frota e vínculos empresariais vivem em módulos próprios e fornecem claims ao `PolicyGate`.

## Modo e garagem do jogador

Uma `TransportSelection` possui um `TransportMode` e, quando necessário, um `TransportAsset`. Caminhar não cria um veículo artificial; bicicleta, moto, carro e frota referenciam um asset cadastrado.

Modos iniciais:

- `ON_FOOT`;
- `BICYCLE`;
- `E_BIKE`;
- `MOTORCYCLE`;
- `CAR`;
- `VAN`;
- `TRUCK`;
- `OTHER_REVIEWED` para tipos aprovados posteriormente.

Cada jogador pode cadastrar zero ou vários `TransportAsset`. Um asset contém proprietário operacional, apelido, tipo, capacidades, dimensões/carga quando relevantes, energia/combustível, acessibilidade, status e documentos exigidos. Placa, Renavam, documento e localização não são públicos na descoberta. `ON_FOOT` funciona sem asset; outros modos podem tornar o asset obrigatório por policy.

O jogador pode:

- cadastrar, editar, arquivar e acompanhar pendências/validade;
- marcar um favorito global;
- definir defaults por modalidade ou categoria no futuro;
- escolher outro asset elegível ao aceitar ou contra-ofertar uma quest;
- trocar antes do início somente dentro das regras de substituição.

O favorito é uma preferência de UI sobre uma seleção (`mode + asset opcional`). Não ignora compatibilidade, documento, CNH, propriedade/autorização, disponibilidade, capacidade, território ou policy. Para impedir dois favoritos concorrentes, a preferência fica em `transport_preferences`, com unicidade por jogador e escopo, em vez de um booleano solto em cada asset.

## Elegibilidade e seleção por quest

Uma quest pode declarar requisitos como tipo aceito, capacidade mínima, volume, refrigeração, acessibilidade, emissão, categoria de CNH ou documento regulatório. O servidor calcula os assets elegíveis e explica por que os demais não podem ser usados.

Fluxo individual:

1. o jogador abre a quest;
2. o app sugere o favorito se ele for elegível;
3. o jogador escolhe um asset antes de `ACCEPT_AS_POSTED` ou `SUBMIT_COUNTEROFFER`;
4. o servidor revalida jogador, asset, autorização, documentos, disponibilidade e requisitos;
5. a proposta referencia a seleção e o aceite cria um snapshot imutável;
6. antes de iniciar, todos os claims são revalidados novamente.

`AssignmentResourceSnapshot` registra executor, contratado, asset, claims usados, documentos/validade sem conteúdo bruto e policy versionada. O solicitante vê, no momento apropriado, executor e características necessárias para reconhecer o veículo.

## Substituição e indisponibilidade

Trocar executor ou transporte após o acordo nunca sobrescreve a atribuição original:

- cria `AssignmentChangeRequest` com motivo, candidato e novo asset;
- reavalia integralmente elegibilidade e conflitos de agenda;
- notifica o solicitante com as informações de identificação necessárias;
- exige novo aceite do solicitante quando a modalidade, risco ou termos materiais mudarem;
- registra versões anterior e nova no audit log;
- após `active`, substituição é bloqueada por padrão e vira caso operacional.

Pane, acidente, documento vencido, desligamento da empresa ou perda de disponibilidade não transferem a quest silenciosamente.

## Organizações e vínculos

Uma empresa usa `Organization`; “empresa” é um tipo de organização, permitindo futuramente cooperativas e outras formas aprovadas. O cadastro empresarial possui identidade, responsáveis, status de verificação, documentos, unidades e conta de pagamento próprias.

`OrganizationMembership` vincula uma conta existente e verificada. A organização não cria funcionários-fantasma nem controla a conta pessoal. O fluxo é convite → visualização de termos/permissões → aceite pelo jogador → vínculo ativo. “Funcionário”, “prestador” ou outro tipo declarado descreve o vínculo informado, mas não determina por si só seu enquadramento jurídico.

Papéis iniciais:

- `OWNER`: responsável organizacional;
- `ADMIN`: membros, frota e configurações;
- `DISPATCHER`: propostas, agenda e seleção de executor/asset;
- `WORKER`: pode ser designado e executar quests;
- `FINANCE`: pagamentos e conciliação, sem acesso automático a localização/documentos.

Permissões seguem mínimo privilégio. Ações sensíveis exigem MFA/step-up, motivo e auditoria. O jogador vê organizações vinculadas, permissões, designações e pode sair quando não houver obrigação ativa; desligamento revoga novas designações imediatamente.

## Proposta e atribuição empresarial

A organização pode aceitar/contra-ofertar por meio de um membro autorizado. Dependendo da categoria, a proposta:

- já indica executor e asset; ou
- reserva a capacidade da organização e exige indicação até um deadline anterior ao início.

O solicitante vê claramente que está contratando uma organização e quem realizará a quest. A elegibilidade é uma interseção:

```text
organização habilitada
+ membro ativo e autorizado
+ executor pessoalmente elegível
+ asset autorizado e compatível
+ disponibilidade sem conflito
+ requisitos territoriais e da quest
= atribuição válida
```

Credencial empresarial não substitui CNH, identidade ou skill pessoal quando a execução as exige. Da mesma forma, credencial do funcionário não regulariza uma organização impedida.

## Frota e autorização de uso

Um asset pertence operacionalmente a uma pessoa ou organização, nunca a ambos ao mesmo tempo. `TransportAssetAuthorization` concede a jogadores específicos permissão de uso por período, modalidade e condições. Uma empresa pode manter frota e autorizar vários membros, mas apenas um asset pode estar reservado para quests conflitantes.

Não presumimos propriedade jurídica pelo cadastro. O sistema registra tipo de posse/autorização declarado e as comprovações exigidas pela policy. Alteração de proprietário operacional ou autorização invalida seleções futuras e dispara revisão das reservas.

## Agenda e concorrência

`ResourceReservation` evita dupla alocação de executor e asset. Reservas têm janela, estado, origem e expiração; aceitar uma proposta converte a reserva temporária em compromisso. Transações e constraints no banco impedem sobreposição proibida. Quests flexíveis podem permitir conflito potencial apenas até a escolha final, nunca durante atribuições confirmadas incompatíveis.

## Privacidade e segurança

- empresa vê apenas dados necessários de seus membros e atribuições;
- documentos brutos ficam em vault privado e não são replicados no snapshot;
- funcionário não herda acesso financeiro por participar de uma quest;
- despachante não acompanha localização fora da janela operacional;
- notificações não exibem endereço, placa ou documento na tela bloqueada;
- toda leitura administrativa/empresarial sensível é auditada;
- payout segue o beneficiário do acordo e onboarding do PSP, não necessariamente o executor físico.

## Persistência planejada

- `transport_assets`;
- `transport_modes` como catálogo versionado/configuração, sem permitir código arbitrário;
- `transport_asset_capabilities`;
- `transport_asset_documents`;
- `transport_preferences`;
- `transport_asset_authorizations`;
- `organizations`;
- `organization_verifications`;
- `organization_units`;
- `organization_memberships`;
- `organization_member_roles`;
- `resource_availability`;
- `resource_reservations`;
- `assignment_resource_snapshots`;
- `assignment_change_requests`.

Dados usados em autorização, agenda e matching são tipados/indexáveis. Metadados flexíveis servem apenas à apresentação e nunca concedem capacidade.

## Casos de teste essenciais

- jogador com vários assets e um favorito inelegível escolhe outro sem alterar o favorito;
- duas tentativas concorrentes não criam dois favoritos no mesmo escopo;
- documento vence depois da proposta e bloqueia aceite/início;
- empresa não designa jogador que não aceitou ou encerrou o vínculo;
- despachante sem role não propõe nem troca executor;
- CNH do executor é incompatível com o asset da frota;
- executor ou asset já reservado não aceita janela conflitante;
- troca após aceite exige revalidação, histórico e consentimento quando material;
- desligamento durante quest ativa abre caso, sem transferência silenciosa;
- payout vai ao beneficiário acordado e não muda quando o despachante troca.
