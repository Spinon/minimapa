# Instruções locais do Minimapa

## Pedido de teste do usuário

Quando o usuário pedir para "rodar um teste", "abrir um teste", "testar o app" ou equivalente sem indicar outro alvo:

1. Execute `npm run test:open` na raiz do repositório.
2. Aguarde testes, lint, build, inicialização do emulador, instalação e abertura do app terminarem.
3. Deixe o emulador `medium_phone` e o Minimapa abertos para o teste manual do usuário.
4. Informe de forma curta se a validação passou e que o app está aberto. Se falhar, diagnostique e corrija quando estiver dentro do escopo.

Não inicie Supabase, Mapbox, PSP ou qualquer serviço potencialmente cobrado para esse pedido genérico. Só suba dependências adicionais quando o fluxo em teste realmente precisar delas.
