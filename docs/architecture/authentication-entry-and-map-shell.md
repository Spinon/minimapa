# Entrada, autenticação e shell do mapa

## Decisão

O Minimapa começa como um jogo, mas autentica como uma aplicação de trabalho e pagamento. A sequência oficial é:

`splash → carregamento seguro → portal de entrada → autenticação/onboarding quando necessário → convite para criar avatar no primeiro acesso → toque para entrar → mapa-fantasia`

Supabase Auth é o provedor inicial. O Android aceita email e senha e **Entrar com Google**. A sessão pode permanecer ativa entre aberturas, mas ações sensíveis continuam sujeitas a sessão recente, MFA e reavaliação server-side. Nenhum login social substitui verificação de identidade civil, idade, CNH, credencial ou elegibilidade de quest.

Nenhuma configuração cloud, SMTP comercial, Google OAuth de produção ou plano pago será ativado antes dos gates de custo, privacidade e segurança. Email local usa Mailpit; Google começa com contrato/mock e depois ambiente de desenvolvimento explicitamente autorizado.

## Sequência de entrada

### 1. Splash

- logo/símbolo do Minimapa e versão discreta;
- curta, cancelável e sem espera artificial;
- respeita redução de movimento e não toca áudio sem consentimento;
- não exibe nome, avatar ou estado de verificação na tela bloqueada.

### 2. Carregamento — “Abrindo os portões”

Executa um bootstrap observável e com timeout:

1. carrega configuração assinada/cacheada e kill switches;
2. restaura sessão apenas do armazenamento seguro do aplicativo;
3. renova token quando necessário;
4. consulta manutenção, versão mínima e estado essencial da conta;
5. prepara dados mínimos do perfil e mapa sem iniciar navegação faturável;
6. escolhe uma rota explícita: portal, onboarding, convite de avatar, MFA, bloqueio, atualização ou mapa.

Falha de rede não vira loading infinito. A tela oferece tentar novamente, diagnóstico simples e saída/troca de conta. Conteúdo cacheado futuro pode ser consultado somente quando a policy permitir; publicar, aceitar ou concluir quests nunca funciona “offline por esperança”.

### 3. Portal de entrada

Quando não existe sessão válida:

- `Entrar com Google`;
- `Entrar com email`;
- `Criar conta`;
- `Esqueci minha senha` dentro do fluxo de email;
- links acessíveis para Termos, Privacidade, Ajuda e Segurança.

Quando existe sessão válida:

- cenário/arte leve, logo e **Toque para entrar**;
- nome de exibição e avatar somente depois de desbloquear o aparelho e restaurar a sessão;
- `Trocar conta` e acessibilidade em posição secundária;
- o toque possui função real: confirma presença, conclui o bootstrap e entra no mapa.

Não haverá botão falso que apenas atrasa o usuário. Em retomadas rápidas, uma preferência de acessibilidade pode reduzir animações, mas o gate continua capaz de mostrar manutenção, sessão expirada, consentimento atualizado ou bloqueio.

## Métodos de autenticação

### Email e senha

- email confirmado antes de ativar a conta;
- senha longa, medidor de força e bloqueio de senhas triviais conforme recursos aprovados;
- mensagens de login e recuperação não confirmam se um email está cadastrado;
- rate limit progressivo e proteção anti-bot adaptativa;
- senha nunca é armazenada, registrada ou manipulada pelo domínio do Minimapa;
- mudança de senha exige reautenticação quando aplicável e produz aviso de segurança.

### Google

- botão oficial e claro, sem imitar interface do Google;
- fluxo nativo Android ou OAuth com PKCE, nonce e redirects/deep links estritamente allowlisted;
- solicitar somente `openid`, email e perfil básico; não pedir acesso a contatos, Drive ou outros dados;
- client secret e `service_role` nunca entram no APK;
- branding e domínio de autenticação próprios são gates para produção contra phishing;
- contas com o mesmo email confirmado seguem a estratégia segura de identity linking do Supabase; linking manual fica desabilitado até uma revisão específica;
- desconectar Google não pode deixar a conta sem método de entrada recuperável.

Depois do Google, um novo usuário sempre passa pelo onboarding. Nome e foto vindos do provedor são sugestões editáveis, nunca dados legais nem autorização.

## Primeiro acesso e criação do avatar

Depois do onboarding obrigatório e antes do primeiro **Toque para entrar**, o Minimapa apresenta o **Ateliê do Personagem**. Criar o avatar é fortemente sugerido, mas pode ser pulado; trabalho, segurança e acesso básico não ficam presos a uma escolha cosmética.

Estados persistidos:

- `NOT_STARTED`: ainda não viu o convite;
- `SKIPPED`: escolheu entrar com avatar inicial gerado;
- `CREATED`: salvou uma personalização;
- `NEEDS_MIGRATION`: loadout usa uma versão antiga do catálogo e precisa de conversão segura.

Se o jogador pular, recebe um avatar inicial neutro e pode abrir o ateliê depois pelo medalhão/menu **Personagem**. A sugestão não reaparece em toda abertura; pode haver um lembrete discreto, com limite de frequência e opção de dispensar.

### Tela “Crie seu aventureiro”

- preview grande e animado no centro, com alternativa estática;
- presets de corpo inclusivos sem exigir gênero como categoria de conta;
- pele, rosto, olhos, cabelo, cores e conjunto inicial de roupa;
- ações `Aleatorizar`, `Desfazer`, `Restaurar`, `Salvar aventureiro` e `Agora não`;
- rotação/zoom acessíveis, nomes textuais para opções e contraste verificável;
- conjunto inicial inteiramente gratuito, sem loja, Gold, oferta, contagem regressiva ou item pago no onboarding;
- confirmação antes de perder alterações e salvamento idempotente.

O avatar é uma composição versionada de IDs do catálogo e cores permitidas, não uma imagem remota arbitrária. O cliente nunca decide propriedade de cosmético: inventário e loadout são validados no servidor. Assets removidos recebem fallback visual sem quebrar o perfil.

### Separação de identidade

- avatar e nome de exibição são identidade lúdica;
- foto/selfie de verificação é evidência privada e não vira avatar automaticamente;
- foto necessária para identificação pós-atribuição segue política própria e não pode ser substituída pelo personagem;
- aparência do avatar nunca altera Karma, elegibilidade, prioridade, remuneração, verificação ou resultado de disputa;
- origem Google pode sugerir nome/foto de perfil, mas a foto não é importada como asset do avatar sem consentimento e pipeline específico.

## Criação e ativação de conta

Auth e perfil são estados separados. O Supabase pode criar uma identidade técnica antes de o cadastro de produto terminar, mas o Minimapa mantém a conta como `ONBOARDING_REQUIRED` e não concede acesso operacional até a ativação transacional.

### Obrigatório para ativar a conta básica

| Dado/aceite | Regra |
| --- | --- |
| Método de acesso | email confirmado + senha, ou identidade Google confirmada |
| Nome de exibição | público, editável, sujeito a moderação e sem obrigação de ser nome civil |
| Data de nascimento | privada; precisa indicar 18+ no piloto e será confrontada na verificação |
| País e município-base | usados para política/jurisdição, sem exigir localização precisa permanente |
| Termos e Privacidade | versão, timestamp, locale e evidência do aceite |
| Declaração de maioridade | não substitui comprovação posterior |
| Preferências essenciais | idioma e acessibilidade; notificações promocionais sempre separadas e opcionais |

Não são obrigatórios nessa etapa: avatar, gênero, endereço residencial, localização contínua, meio de transporte, empresa, loja, skills ou dados de pagamento.

### Onboarding de identidade para o piloto

Antes de publicar ou executar uma quest real no piloto fechado, o jogador completa a etapa protegida de verificação: nome legal, CPF/dados exigidos pelo provedor, telefone confirmado quando a política aprovada exigir, documento/selfie/prova de vida e consentimentos específicos. Dados sensíveis não entram em `user_metadata`, logs, analytics ou fixtures.

Esse desenho satisfaz os requisitos do produto sem pedir CPF ou biometria de alguém que apenas criou credenciais e desistiu. Contas técnicas que nunca concluem onboarding terão retenção e limpeza definidas juridicamente.

Todos começam como `PLAYER`; solicitante, executor, comerciante e membro de empresa são capacidades posteriores. Escolher um “tipo de conta” no cadastro não concede role nem cria identidades duplicadas.

## Recuperação de senha e de conta

O fluxo **Esqueci minha senha** é tratado como superfície crítica:

1. recebe o email e sempre mostra resposta neutra;
2. aplica rate limit, cooldown de reenvio e CAPTCHA/Turnstile somente quando o risco justificar;
3. envia link de uso único para redirect/deep link exato e allowlisted;
4. troca o código por sessão de recuperação e abre uma tela que não aceita redirects arbitrários;
5. exige uma nova senha forte e impede reutilização quando o recurso contratado permitir;
6. revoga ou oferece revogação imediata das demais sessões, invalida operações sensíveis pendentes e registra o evento;
7. envia notificação de segurança sem link de login embutido além do necessário;
8. marca a conta para reverificação/step-up quando houver mudança de dispositivo, sinais de tomada de conta ou papel de alto risco.

O suporte nunca recebe, escolhe ou conhece senha. Recuperação assistida não ignora MFA ou identidade; usa caso auditável, espera proporcional e revisão humana. Códigos, tokens e URLs completas de recuperação não aparecem em logs ou analytics.

## Sessão persistente e segurança

- access token curto e refresh token com rotação;
- tokens em armazenamento privado protegido pelo Android Keystore, nunca em preferências simples, logs ou backup inseguro;
- auto-refresh controlado pelo lifecycle e tolerante a conectividade intermitente;
- logout do dispositivo e **Sair de todos os dispositivos**;
- lista futura de sessões/dispositivos com data aproximada e revogação;
- sessão restaurada não implica `aal2`; ações como payout, alteração de conta financeira, documentos, organização e administração exigem step-up/MFA;
- operações críticas consultam estado atual e, quando necessário, `session_id`; não confiam apenas em claims antigas;
- autorização usa tabelas server-controlled/RLS e `app_metadata` somente como cache não definitivo; `user_metadata` nunca concede permissões.

## Shell do mapa-fantasia

Depois de **Toque para entrar**, o usuário chega ao `ExploreMode`: o mapa é a home, e não uma tela escondida atrás de um dashboard convencional.

### Camadas persistentes do ExploreMode

- topo: busca contextual, município/zona e estado de conectividade;
- canto superior: medalhão do avatar com nível e acesso a personagem/perfil;
- lateral direita: trilho curto de ações — **Postar quest** como ação primária, mural/filtros e ações contextuais habilitadas;
- lateral esquerda ou canto oposto: comunicação, notificações e acesso a grupo quando existirem;
- mapa: regiões de quest, pontos públicos, lojas e conteúdo permitido, com densidade e clustering;
- rodapé/ficha: bottom sheet contextual ao selecionar quest, loja, jogador ou lugar.

Ícones têm rótulo acessível, estado selecionado, badge limitado e alternativa textual. A estética pode lembrar HUD de RPG, mas preço, identidade, endereço, risco e segurança usam linguagem convencional.

### Menus que substituem o mapa

Configurações, personagem, garagem, organização, loja, histórico, suporte e criação de quest abrem superfícies próprias em tela cheia. O mapa preserva câmera, filtros e seleção para restauração, mas deixa de renderizar/consultar quando oculto se isso reduzir bateria, privacidade e custo.

O menu **Ações** é extensível por capability e feature flag:

- postar quest;
- gerenciar quests;
- cadastrar meio de transporte;
- criar/gerenciar organização;
- iniciar loja, somente quando o módulo estiver aprovado;
- entrar em conteúdos RPG, somente em estado/localização seguros.

Uma ação indisponível não finge funcionar: aparece bloqueada com requisito explicável ou fica ausente quando ainda não foi lançada. Configurações críticas não são escondidas atrás de nomenclatura fantasy.

### Exceção: modo corrida

Em `ActiveQuestMode`, o trilho de ícones, lojas, RPG, anúncios e quests alheias é desmontado. Menus comuns não substituem livremente a navegação ativa. Permanecem apenas navegação, estado da quest, comunicação essencial, segurança e ações de pausa/saída confirmadas. Ao concluir ou cancelar com segurança, o app restaura o contexto do mapa-mundo.

## Estados de interface obrigatórios

- primeira instalação;
- primeiro acesso com avatar não iniciado, pulado, criado ou incompatível com nova versão;
- sessão válida, expirada, revogada e sem rede;
- email não confirmado;
- onboarding incompleto;
- MFA necessário/perdido;
- manutenção, atualização obrigatória e conta suspensa;
- recuperação solicitada, link inválido/expirado/usado e senha alterada;
- Google cancelado, sem conta disponível ou falha de redirect;
- mapa carregando, vazio, degradado e sem permissão de localização;
- retorno de menu preservando contexto;
- transição explícita entre `ExploreMode` e `ActiveQuestMode`.

## Critérios de aceite arquiteturais

- nenhum frame do mapa aparece antes de a rota de conta/policy ser decidida;
- login Google e email terminam na mesma identidade de domínio e no mesmo onboarding;
- reiniciar o app mantém uma sessão válida, mas nunca contorna MFA/step-up;
- recuperação não permite enumeração de usuários, open redirect, replay ou reutilização de token;
- cadastro incompleto não publica, aceita ou executa quests;
- pular o avatar nunca contorna onboarding nem bloqueia capabilities não cosméticas;
- loadout aceita somente itens gratuitos iniciais ou itens pertencentes ao inventário server-side;
- perfil/roles não podem ser promovidos por metadata editável pelo cliente;
- menus do ExploreMode preservam contexto; o modo corrida remove as camadas não essenciais da árvore de UI;
- todos os fluxos possuem teste local com Supabase/Mailpit, providers fake e nenhuma chamada faturável.

## Referências oficiais

- [Supabase — Login with Google](https://supabase.com/docs/guides/auth/social-login/auth-google)
- [Supabase — Password-based Auth](https://supabase.com/docs/guides/auth/passwords)
- [Supabase — Password security](https://supabase.com/docs/guides/auth/password-security)
- [Supabase — User sessions](https://supabase.com/docs/guides/auth/sessions)
- [Supabase — Multi-Factor Authentication](https://supabase.com/docs/guides/auth/auth-mfa)
- [Supabase — CAPTCHA protection](https://supabase.com/docs/guides/auth/auth-captcha)
- [Supabase — Identity linking](https://supabase.com/docs/guides/auth/auth-identity-linking)
