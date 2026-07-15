# Confiança e verificação de usuários

## Princípio

Verificação reduz anonimato e fraude, mas não certifica caráter, competência ou segurança absoluta. A interface usa afirmações precisas, como **Identidade verificada** e **Credencial verificada**; nunca rotula uma pessoa como “segura” ou “insegura”.

Somente jogadores com identidade verificada podem ser atribuídos a uma quest, executá-la ou concluí-la. Usuários não verificados podem explorar e publicar quests de baixo risco com alcance limitado, mas precisam concluir a verificação antes de a quest avançar para atribuição e execução.

```mermaid
stateDiagram-v2
    [*] --> Unverified
    Unverified --> Pending: inicia verificação
    Pending --> Verified: controles aprovados
    Pending --> NeedsReview: divergência ou baixa confiança
    NeedsReview --> Verified: revisão aprovada
    NeedsReview --> Rejected: revisão rejeitada
    Verified --> ReverificationRequired: validade ou risco
    ReverificationRequired --> Verified: nova aprovação
    Verified --> Suspended: fraude ou medida de segurança
    Suspended --> Verified: recurso aprovado
```

## Camadas independentes

1. **Conta autenticada:** login por email, telefone ou provedor social.
2. **Contato confirmado:** posse de email e telefone confirmada.
3. **Identidade verificada:** documento, dados cadastrais, idade e prova de vida/selfie avaliados por provedor e, quando necessário, revisão humana.
4. **Papel verificado:** requisitos adicionais para motorista, comerciante ou outra função regulada.
5. **Skill/credencial verificada:** comprovação profissional específica; não substitui identidade.
6. **Sessão de alta confiança:** MFA recente para ações sensíveis; não substitui verificação documental.

Um badge sempre declara qual camada foi validada. “Identidade verificada” não implica “encanador verificado”; “curso verificado” não implica boa reputação.

## Capacidades por estado

| Capacidade | Não verificado | Identidade pendente | Identidade verificada |
| --- | --- | --- | --- |
| Explorar conteúdo público | sim | sim | sim |
| Criar rascunho de quest | sim | sim | sim |
| Publicar quest de baixo risco | sim, alcance limitado | sim, alcance limitado | sim |
| Exibir endereço exato publicamente | nunca | nunca | nunca |
| Candidatar-se ou aceitar quest | não | não | sim |
| Ser atribuído/executar/concluir | não | não | sim |
| Ativar quest publicada por solicitante não verificado | não | não | sim |
| Publicar serviço regulado/alto risco | não | não | conforme requisitos extras |
| Votar no Conselho do Reino | não | não | conforme regras do ciclo |

Uma quest de solicitante não verificado mostra **Identidade do solicitante não verificada** e `verification_required_before_assignment`. Prestadores podem demonstrar interesse sem receber endereço exato, contato direto ou atribuição. Ao verificar-se, o solicitante libera o matching normal sem precisar republicar a quest.

## Estados e reavaliação

Estados mínimos:

- `UNVERIFIED`;
- `PENDING`;
- `NEEDS_REVIEW`;
- `VERIFIED`;
- `REJECTED`;
- `REVERIFICATION_REQUIRED`;
- `EXPIRED`;
- `SUSPENDED`.

O status possui motivo interno, datas, método, nível de confiança, versão da política e validade. Expiração de documento, alteração relevante, fraude provável ou recuperação sensível de conta pode exigir nova verificação. Usuários têm um fluxo de recurso; detalhes antifraude não são expostos.

## Autorização no Supabase

- A fonte de verdade fica em tabelas controladas pelo servidor, nunca em `user_metadata` editável pelo usuário.
- Claims em `app_metadata` podem acelerar a interface, mas operações críticas consultam estado atual porque JWTs podem estar desatualizados.
- RLS bloqueia candidaturas, atribuições e mudanças de estado para identidades não verificadas.
- Uma função privada e uma operação transacional validam simultaneamente identidade do executor, identidade do solicitante, requisitos da quest e estado permitido.
- Ações sensíveis podem exigir sessão `aal2`/MFA além da identidade documental.
- `service_role` permanece somente no backend; aplicativos móveis recebem apenas chave publicável.

Verificação de identidade e MFA são controles diferentes. O nível `aal2` confirma um segundo fator da sessão, não um documento civil.

## Dados e integração com provedor

O domínio usa um adaptador `IdentityVerificationProvider` para evitar dependência permanente de uma empresa. O backend inicia a sessão de verificação, recebe webhooks assinados e traduz resultados para estados internos idempotentes.

Armazenar o mínimo possível:

- identificador da verificação no provedor;
- resultado, nível de confiança e códigos internos necessários;
- nome normalizado, data de nascimento e identificadores estritamente necessários, preferencialmente protegidos/tokenizados;
- referências de evidência e trilha de auditoria;
- datas de criação, decisão, validade e exclusão programada.

Documentos e biometria brutos não ficam em tabelas públicas. Se precisarem transitar pelo Storage, usam bucket privado, caminhos por usuário, acesso temporário, retenção curta e auditoria. A política final de retenção e base legal deve ser validada antes do piloto.

## Opções de verificação no Brasil

Uma consulta de situação cadastral do CPF confirma dados do cadastro, mas não prova que quem segura o celular é o titular. O gate `IDENTITY_VERIFIED` exige a combinação de dados biográficos, documento quando necessário, comparação facial e prova de vida.

### Candidatos para spike

| Opção | Capacidades relevantes | Papel possível |
| --- | --- | --- |
| Serpro Datavalid V4 + BioConnect | CPF/dados biográficos em bases oficiais, facial, vivacidade, QR Code de CNH e SDK de captura | Candidato direto para identidade e futuros motoristas |
| idwall | CPF, documento, selfie versus documento, prova de vida, sinais de dispositivo e jornadas orquestradas | Candidato all-in-one com fluxo e gestão de risco |
| Unico IDCloud | selfie com liveness, verificação de identidade, captura/OCR de documentos, CPF match, SDK web/nativo e API | Candidato all-in-one com revalidação e sinais de fraude |

O spike compara cobertura real da população piloto, falso aceite/rejeição, acessibilidade, fallback manual, tempo, SDK Android, webhooks, disponibilidade, portabilidade, retenção, contrato e custo por verificação. A arquitetura não assume um vencedor antes desse teste.

Fontes oficiais:

- Datavalid V4: https://centraldeajuda.serpro.gov.br/duvidas/pt/avisos/avisodatavalidv4/
- Datavalid/BioConnect: https://campanhas.serpro.gov.br/datavalid/novidades/
- idwall: https://idwall.co/pt-BR/casos-de-uso/validacao-de-identidade/
- Unico IDCloud: https://developer.unico.io/index.html

## Assinatura eletrônica

Autenticação de identidade e assinatura têm propósitos diferentes. O KYC cria confiança sobre a conta; a assinatura vincula uma pessoa identificada a um documento específico e preserva evidências de integridade e manifestação de vontade.

Para quests comuns, aceite explícito, versão dos termos, timestamp e eventos auditáveis podem ser suficientes após análise jurídica. Assinatura integrada fica reservada inicialmente para:

- termos de motorista, comerciante ou prestador regulado;
- quests de maior valor ou risco definidas pela matriz;
- orçamento/escopo formal de serviços complexos;
- acordos empresariais e documentos que exijam evidência reforçada.

Candidatos brasileiros para spike incluem Clicksign API 3.0 e Autentique, ambos com API e métodos adicionais de autenticação; a Clicksign documenta documentoscopia e biometria facial com verificação governamental, e a Autentique oferece biometria e validação documental. O nível de assinatura necessário — simples, avançada ou qualificada ICP-Brasil — depende do ato e da legislação aplicável, não apenas do nome comercial do produto.

Fontes oficiais:

- Clicksign API 3.0: https://developers.clicksign.com/docs/migracao-da-api-19-para-30
- Autentique: https://www.autentique.com.br/
- Lei 14.063/2020: https://www.planalto.gov.br/ccivil_03/_ato2019-2022/2020/lei/l14063.htm
- LGPD: https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm

## Controles adicionais

- rate limits para tentativas e reenvios;
- detecção privada de contas duplicadas e dispositivos abusivos;
- assinatura e replay protection em webhooks;
- revisão manual para resultados inconclusivos;
- revogação e bloqueio emergencial;
- notificação de mudanças de estado;
- logs sem documento, biometria ou endereço completo;
- testes tentando contornar RLS, usar JWT antigo e repetir webhooks.
