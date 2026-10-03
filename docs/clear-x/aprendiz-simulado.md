# Aprendiz simulado

Um agente faz o papel do Alan (Python e C++ iniciais, PEP 8, acha Java
complexo). Ele lê só as aulas, segue os passos, tenta cada lacuna sem
abrir as respostas e relata onde travou.

Limite (HIPÓTESE): é filtro de defeitos, não prova de que o Alan aprende.
Não substitui o Alan nem um aparelho real.

## Rodada 1 · 03/10/2026 · Fase A (17 aulas alteradas)

### Nível 0 (0.0 a 0.4)

Chegou a "✅ Aula concluída!" nas 5 aulas, quase sempre na 1ª tentativa.
Defeitos achados e o que foi feito:

- 0.2 aceitava `outra = c` (uma criatura só) → novo `assert outra is
  not c` e conferência de `health`.
- 0.1–0.4 mandavam "abrir o .py no Pydroid", mas a 0.0 ensina o Raw →
  o Rodar agora diz "copie o 📋 Raw e cole no Pydroid, ou abra do ZIP".
- 0.4 citava um `c` que não existe → `criaturas[0]`; explicado que o
  Python põe o `self` sozinho; objetivo reescrito.
- Dicas que enganavam: "fome não é negativa" com fome 1 → "Fome 1
  menos 3 dá 0 (mínimo)"; 0.0 mostrava `A` fixo → mostra a letra do
  aluno; 0.4 passou a perguntar pelo `self.hunger`.
- 0.3: o parâmetro não é cópia (diferente do C++) → dito na resposta.
- Glossário ganhou `None`, `pass`/`...`, parâmetro, `True`/`False`,
  `and`/`or`, `+=`, `append`/`len`/`range`, `min`/`max`.
- Menores: "chamada" em vez de "usada"; contagem do zero na dica da
  0.1; "sem apagar o agua = 0"; legenda do ⬜ ("só cor").

### Nível 1 (1.1 a 1.7)

Chegou ao fim das 7. Tentativas: 1 na maioria; 3 no MONTAR da 1.4 e da
1.6 (faltava `self.` no comentário). Defeitos e correções:

- 1.4 entregava a resposta duas vezes (o exemplo do cinema dava o mesmo
  29 do assert) → o tile pedido virou (7, 1).
- As Rosetas mostram a solução do MONTAR → o título agora diz "abra
  depois de terminar".
- Comentários sem `self.` (1.4, 1.6) e sem `c = creatures[i]` (1.5) →
  nomes reais no comentário, mais como quebrar linha longa (PEP 8).
- 1.6: o Prever não podia ser conferido → agora pergunta se o programa
  para no primeiro `assert`.
- 1.7: `==` com float → `abs(...) < 0.01`; a barra de comida sumia
  abaixo de 1 → o valor aparece ao lado; `TypeError` de
  `for c in self.creatures` entrou na armadilha.
- 1.5: Investigar pergunta por que a `c` não levou o golpe (o sintoma
  do bug).
- Rótulos numerados começavam em "2." → começam em "1.".
- 1.1: armadilha "de baixo para cima" reescrita; diferença 0.015 x 0.05
  explicada; 1.3: faixa certa do MUDAR (0.10 a 0.27).

Ficou de fora (limite de 40 linhas): rastreio da segunda criatura na
1.2 e do `for` da 1.5.

### Nível 2 (2.1 a 2.5)

Concluiu os 5 `.py`; os passos de navegador foram julgados pelo texto.
Defeitos e correções:

- O modelo de PR sugeria `Resolve #12`, que é a issue da trilha (o PR a
  fecharia), e o texto escrito dentro do `<!-- -->` some → exemplo sem
  número e "escreva numa linha nova, depois do `-->`".
- Faltava o caminho quando a CI fica vermelha → item no "⚠️ Não
  funcionou?" da 2.2 e da 2.3; a tarefa da 2.3 avisa que a CI "Aulas"
  confere o tamanho.
- 2.5 imprimia `6.6000000000000005f` → `round(nova, 2)`.
- Faltava o aviso do Chrome ao baixar `.apk` → "Baixar mesmo assim" na
  2.4 e na 2.5.
- 2.4: `TypeError` sem `str()` escapava da dica → o comentário da
  lacuna lembra o `str(cor[0])`; "troque só os três números".
- "Procure o texto" não funciona no editor do celular → "role até o
  número da linha na margem".
- 2.3: tela "Configure Git", Linux sem Desktop oficial e token no
  `git push`.
- 2.1: pergunta falsa ("toda linha tem `;`") reescrita; `.equals` virou o
  item 18 da Roseta; `in_bounds(8, 0)` pega o erro de um.
- 2.5: velocidade de hoje no Prever, base de comparação e Delete branch.
- Issue #8: apontava para uma seção que não existe → agora aponta para a
  aula 2.2.

Não testável daqui: o Gradle com o plugin Android (o `core:test` com 6.6
foi medido antes, 115 testes verdes) e qualquer botão real do GitHub.

## Rodada 2 · 03/10/2026 · Fase C (provas N0, N1, N2)

Acertou tudo de primeira. O problema era o contrário: respostas erradas
também passavam. Defeitos e correções:

- N0 item 3 aceitava `if` invertido e `+1.5` → confere depois de cada
  chamada (4, 6, 7) e mostra o valor.
- N1 item 2 aceitava estado fixo, limite errado e `dt` ignorado →
  passos de 2 s, confere bateria e estado a cada passo.
- N1 item 3 não tinha idade 3 (`>= 3` passava) → lista com 3 e 7; dica
  pede para mudar a própria lista.
- N1 item 4 tinha duas ordens válidas → o enunciado diz que a comida
  cresce primeiro.
- O recuo entregava a ordem dos Parsons → linhas sem recuo; N0 ganhou
  uma 4ª linha (`c = Creature()`).
- N2: "outra cara" igual ao Montar da 2.1 → `metade(int n)` com `//`;
  "a regra muda" era só lembrança → regra nova com `||`; Parsons novo;
  variações aceitas no item 4 (`double`, `public`, `static`).
- "Passou de primeira?" era vago → "os itens certos sem abrir as
  respostas".
- N0 perguntava onde a classe aparece no jogo (não ensinado) → pergunta
  pelo `self`.
- `verificar.py provas/N2` quebrava → avisa que o `.md` é conferido sem
  argumentos.

Teste adversarial (script): `if` invertido, `+1.5`, estado fixo, sem
`dt`, limite 0.5 e `>= 3` agora falham.

## Rodada 3 · 03/10/2026 · Fase D (3.0 a 4.4 e Prova N3)

Testes Java do "Alan" compilados e rodados contra o código real (javac
e JUnit fora do Gradle). 22 defeitos; os principais e o que foi feito:

- A #12 estava fechada: um commit da Fase A citava "Resolve #12" ao
  descrever o defeito do modelo de PR → reaberta; a trilha ganhou os
  Níveis 3 e 4.
- 3.0 pulava "abrir no Android Studio" (cria `local.properties`) e
  misturava `gradlew.bat` com Git Bash → um caminho só: Git Bash e
  `./gradlew`, com o passo do Android Studio; JDK "17, até 24".
- 3.0 citava uma issue que não existia → criada a #23.
- 3.2 dizia "1.0 de comida" (máximo real 0.95; `World` novo é todo
  oceano) → 0.65 de grama e `setTile` antes do `FoodMap`; import certo
  (`World`, `TileType`); renomear package e classe; os quatro testes que
  a #9 pede, mais o doc de ecologia e o MAPA.
- 3.2: "tire o `Math.min`" era ambíguo e não quebrava a rebrota → as
  duas trocas exatas (linha do `consume` e do `regrow`).
- 3.3: com um pai só em -1 o teste passava antes do conserto → os dois
  pais; como matar (três `strikeAt`); aviso de que os `if` empurram as
  linhas citadas pela 2.1 (job "Aulas").
- N3 aceitava um `assertEquals(1.0f, ...)` que sempre falha → 0.65; "o
  teste confere a regra" era falso → "o conserto usa a regra".
- 3.1: o Prever mostrava a resposta → `???`; `living()` explicado.
- 4.1: `colorOf(-1)` lança exceção para tile sem dono; `BitmapFont`
  avisada. 4.2: o toque duplo dispara antes um `tap`. 4.3: `-cp` com `;`
  no Windows (em `docs/verificacao.md`).
- Glossário ganhou JDK, SDK, gradlew, Git Bash, JUnit, teste,
  tolerância, exceção, package/import, vermelho e verde, invariante,
  reino/território, genoma/fenótipo, render, protótipo e roadmap.

Conferido sem erro: âncoras do COMECE-AQUI, `applyDamage`, `checkId`,
`TileMapping`, `World.setTile`, `FoodMap.retile`, `MIN_FATOR`/`MAX_FATOR`.
O conserto da 3.3 resolve a #10: 44 testes de `SimulationTest` verdes.
