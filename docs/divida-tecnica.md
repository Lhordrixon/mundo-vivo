# Dívida técnica

Só o que está em aberto: o que já se sabe que está incompleto, lento ou
provisório. Está escrito aqui de propósito, para não virar surpresa.

**Em resumo:** o jogo funciona, com quatro tipos de pendência. Há um bug
que ainda não acontece em jogo normal. Há código lento que só pesa com
muitas criaturas. Há código pronto que ainda não aparece na tela. E há
propostas de mudança estrutural que esperam aprovação.

> [!TIP]
> Procurando algo para fazer? Os itens marcados como **bom para começar**
> são pequenos, isolados e fáceis de testar.

---

## Que bugs conhecidos existem?

### 🔴 problema conhecido — reproduzir com uma criatura sem reino quebra o jogo

Se um dos pais não tiver reino (`factionId == -1`), o filho pode herdar o
`-1`. Aí `FactionRegistry.join(-1)` lança `IndexOutOfBoundsException`.

- **Onde:** `Simulation.reproduce`, na linha `factions.join(childFaction)`.
- **Quando acontece:** ainda não, em jogo normal. Todo fundador recebe
  reino, e todo filho herda um válido.
- **Quando vai acontecer:** no dia em que algo criar uma criatura sem
  reino. Um poder de "criar criatura", por exemplo.

---

## O que fica lento com muita criatura?

- **A busca de inimigos compara cada criatura com todas as outras.** Com
  288 criaturas, um passo custa 0,165 ms. Com o pool cheio (2.999), custa
  **8,9 ms**, mais da metade de um quadro. O conserto é guardar as
  criaturas numa grade por região. O lugar é `Simulation.hostileNeighbours`.
- **A busca por parceiro também percorre todas as criaturas.** Hoje é
  barato, porque só quem procura parceiro faz a busca. A mesma grade
  resolveria. O lugar é `Simulation.resolveMate`.
- **A rebrota de comida percorre o mapa inteiro a cada passo.** São 49 mil
  tiles por passo. Daria para crescer uma fatia do mapa por quadro, com o
  `dt` multiplicado.
- **O território é sempre recalculado do mundo inteiro**, uma vez por
  segundo, mesmo sem ninguém ter se mexido. A mesma correção da rebrota
  serviria: recalcular uma fatia por vez.
- **Mudar um tile reenvia a textura inteira** para a placa de vídeo
  (196 KB), não só a parte que mudou. Vai pesar quando os poderes pintarem
  terreno. A correção é acumular a região suja e enviar só ela.

---

## O que está pronto mas ainda não aparece?

- 🔵 **Nome e cor dos reinos.** `FactionRegistry.nameOf` e `colorOf`
  funcionam e têm teste. Só os testes os chamam. O lugar natural é um mapa
  de territórios colorido. **Bom para começar.**
- 🔵 **O tamanho da criatura.** `Creature.size` sai do genoma, como os
  outros traços, mas nada o lê. O uso natural seria no combate ou na
  comida. **Bom para começar.**
- 🔵 **O território.** É calculado a cada segundo, mas ninguém evita terra
  de outro reino, e nada é desenhado. As criaturas brigam por proximidade,
  não por território.
- 🔵 **O dano de terreno.** Funciona e tem teste, mas nada muda o chão
  debaixo de uma criatura durante o jogo. Espera um poder ou um desastre.
  Enquanto isso, custa uma consulta de tile por criatura por passo.
- 🔵 **Edição de terreno.** `World.setTile`, `WorldRenderer.setTile` e
  `FoodMap.retile` existem, mas nenhum código de jogo os chama.

---

## O que é simplificação provisória?

- **Criaturas andam em linha reta.** Não há busca de caminho. Se o
  próximo passo cairia na água, a criatura escolhe outro destino. Contornar
  uma baía pode demorar. O território não resolve isso: ele só decide de
  quem é cada tile, não guia ninguém.
- **O limite do pool não segura a população de forma saudável.** Batendo
  no limite, param os nascimentos, mas não as mortes. Medido: com o limite
  em 250, a população de uma semente que normalmente vive foi a zero. O
  limite existe por memória; quem deve limitar é a comida.
- **O filho de pais de reinos diferentes tira o reino na moeda.**
  Funciona porque é raro: vizinhos tendem a ser parentes. Com guerra de
  verdade, talvez esse casal nem devesse existir.
- **Temperatura e umidade são descartadas depois de gerar o mundo.** Só a
  altura fica guardada. Crescimento de vegetação ou migração por estação
  vão precisar delas.

---

## E no app Android?

- **Não existe salvar e carregar.** `World`, `FoodMap` e `Rng` já expõem o
  estado bruto para isso, e a semente é guardada. Falta o formato do
  arquivo.
- **Toque longo apaga o mundo e gera outro.** É um atalho de
  desenvolvimento, e vai atrapalhar quando houver outros gestos. **Bom
  para começar.**
- **O `minifyEnabled` está desligado na versão final.** As regras do
  ProGuard existem, mas ligar sem testar quebra o app ao abrir, não ao
  compilar.
- **O ícone é provisório.** Um desenho vetorial simples, sem identidade.

---

## Que mudanças estruturais estão propostas?

Nenhuma destas está aprovada. Refatoração estrutural só entra com a
aprovação do dono do projeto, e só se deixar a arquitetura mais
verdadeira, não só mais bonita.

| Proposta | Benefício | Custo |
|---|---|---|
| **Dividir `Simulation.java`** (869 linhas) em partes por assunto | arquivos menores; cada domínio com um lugar óbvio | mexe no arquivo mais central; risco de quebrar a ordem do passo, que decide a quem se credita cada morte |
| **Criar os pacotes `combat` e `player`** | combate e jogador passam a ter pacote, como os outros domínios | exige expor partes internas de `Simulation` (pool, reinos, `die()`); hoje `hostileNeighbours` e `strikeAt` somam cerca de 45 linhas |
| **Quebrar o ciclo `creature` ⇄ `genetics`** | dependências em uma direção só; `genetics` testável sem `creature` | `Phenotype` teria de devolver valores em vez de escrever na criatura, ou mudar de pacote; o ciclo hoje não causa bug |
| **Aposentar `tools/SimSelfTest`** | uma suíte de testes só, sem duplicação com o JUnit | perde o único jeito de testar sem Gradle e sem internet; as 95 verificações teriam de ser conferidas contra o JUnit antes |

---

## O que vem depois?

1. **Olhar o jogo na tela com atenção.** O APK já abriu num celular, mas
   ninguém mediu FPS nem rodou `./gradlew desktop:run`.
2. **Salvar e carregar o mundo.** Conferir que um mundo recarregado
   recalcula o mesmo território, já que ele deriva da posição das
   criaturas em vez de ser salvo.
3. **Desenhar o território com o nome e a cor de cada reino.** Isso dá
   uso a três coisas prontas que hoje não aparecem.
