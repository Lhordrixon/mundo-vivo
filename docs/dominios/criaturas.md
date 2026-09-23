# 🐾 Criaturas

- **O que é:** os seres que vivem no mundo, com fome, vida, idade, espécie,
  reino e genoma. Aqui também estão o combate e o poder do jogador, que
  ainda não têm pacote próprio.
- **Classes principais:** `sim/creature/Creature`, `CreatureState`,
  `CreaturePool`, `CreatureConfig`, `Species`; `sim/Simulation` (passo,
  combate e golpe do jogador).
- **Quem depende:** `faction`, `genetics`, `Simulation`, `render/` e
  `MundoVivoGame`. `creature` depende de `genetics` (ciclo conhecido).
- **Testes que cobrem:** `CreatureTest`, `CreaturePoolTest`,
  `SimulationTest` e a parte de criaturas do `tools/SimSelfTest`.

---

## 🐾 Como uma criatura vive?

Cada criatura tem fome, vida, idade e um **estado**: vagando, procurando
comida, comendo ou procurando parceiro.

- A fome sobe o tempo todo.
- Acima de um limite, a criatura larga tudo e vai atrás de comida.
- Com a fome no máximo, ela começa a perder vida.
- Saciada, a vida se recupera devagar.
- Saciada, adulta e fora do período de espera, ela procura parceiro.
- Reproduzir custa fome aos dois pais.
- Passando da idade máxima, ela morre de velhice.

A cor na tela mostra o estado: **branco**, vagando; **amarelo**,
procurando comida; **verde**, comendo; **rosa**, procurando parceiro
(`CreatureRenderer.colorFor`).

![De perto, cada quadrado é uma criatura](../img/criaturas.png)

> [!TIP]
> O custo de fome da reprodução é o que prende a população à comida. Sem
> ele, a população cresceria até o limite do pool.

<details>
<summary>Três números que foram descobertos medindo, não escolhendo</summary>

**O raio de busca por parceiro é cinco vezes o da comida.** São 70 tiles
contra 14 (`mateSearchRadiusTiles`, `searchRadiusTiles`). Com os dois
iguais, uma população pequena deixava de se encontrar e se extinguia com
comida sobrando. Um terço das sementes testadas morria assim.

Ampliar o raio de parceiro é de graça: essa busca já percorre a lista de
criaturas vivas, e o raio só filtra o resultado. Ampliar o de comida
custaria varrer mais tiles.

**O limite de "tile comestível" precisa ser baixo.** Veja
[ecologia](ecologia.md).

**A espera entre reproduções decide a estabilidade.** Com espera curta, a
população multiplicava antes de a comida responder, estourava o mapa e
morria de fome inteira. O ciclo de explosão e colapso sumiu ao dobrar a
espera.

</details>

### Onde as criaturas moram na memória?

No `CreaturePool`. Ele cria todas as criaturas (até 3.000) uma vez, no
começo, e recicla os espaços: morrer devolve o slot, nascer pega um livre.
Por isso a simulação não aloca memória durante o jogo.

### Como elas andam?

Em linha reta até o alvo. Se o próximo passo cairia na água, a criatura
escolhe outro destino. Não há busca de caminho, então contornar uma baía
pode demorar. Está na [dívida técnica](../divida-tecnica.md).

---

## 🐾 Com quem uma criatura pode ter filhos?

Com quem é da mesma **espécie**. Existem duas: `ALPHA` e `BETA`.

- Cada fundador sorteia a sua.
- Todo filho herda a espécie dos pais, sem sorteio, porque os pais têm
  sempre a mesma.
- Duas criaturas de espécies diferentes nunca formam par, por mais perto
  que estejam.

As duas espécies são iguais em tudo, menos em quem cruza com quem. Elas
não aparecem na tela. Veja
[nomes secos de espécie](../decisoes/0009-nomes-secos-de-especie.md) e
[240 fundadores](../decisoes/0007-240-fundadores.md).

O que o filho herda além da espécie está em [genética](genetica.md).

---

## 🐾 Como uma criatura perde vida e morre?

Existe uma porta só para perder vida: `Creature.applyDamage(quanto,
causa)`. O método tira vida, para em zero, anota a causa e devolve `true`
só se **aquele** golpe foi o fatal.

`applyDamage` não mata ninguém. Toda morte termina em `Simulation.die()`:

1. O corpo vira comida no tile.
2. A criatura sai do reino.
3. O slot volta ao pool.

A causa só decide em qual contador a morte entra. Veja
[porta única de dano](../decisoes/0008-porta-unica-de-dano.md).

| Causa | Constante | Quando |
|---|---|---|
| Fome | `CAUSE_STARVATION` | fome no máximo |
| Terreno perigoso | `CAUSE_HAZARDOUS_TERRAIN` | pisar no oceano profundo |
| Golpe do jogador | `CAUSE_PLAYER_STRIKE` | um toque na criatura |
| Combate | `CAUSE_COMBAT` | inimigo ao alcance |
| Velhice | — | idade máxima; vai direto para `die()` |

Num mesmo passo, o dano vem na ordem fome, terreno, combate. Quem morre
com mais de um acontecendo é creditado ao mais recente.

> [!NOTE]
> Em jogo normal, o terreno nunca fere: o movimento recusa água, então
> ninguém entra andando no oceano profundo. Esse dano existe para quando o
> chão mudar debaixo de alguém, com um poder ou um desastre.

---

## ⚔️ Como funciona o combate?

Criaturas de reinos diferentes que ficam perto uma da outra se ferem.

- **Alcance:** 1,5 tile (`CreatureConfig.combatRangeTiles`).
- **Dano:** 0,18 de vida por segundo, **por inimigo** ao alcance
  (`CreatureConfig.combatDamagePerSecond`).
- **Quem não luta:** criatura sem reino (−1) e quem está procurando
  parceiro.
- **Onde fica:** `Simulation.hostileNeighbours`, chamado a cada passo.

O dano se multiplica pelo número de inimigos. Assim, estar em menor número
mata mais rápido. Com dano fixo, uma criatura sozinha trocaria de igual
para igual com um reino inteiro.

Combate é reação a proximidade. Ninguém persegue inimigo, e não há
exército. É o mínimo para os reinos terem consequência.

> [!IMPORTANT]
> O combate só funciona porque o mundo nasce com poucos reinos contíguos.
> Com um reino por criatura, todo vizinho era inimigo, e o mundo se
> extinguia. Veja [poucos reinos contíguos](../decisoes/0006-poucos-reinos-contiguos.md).

<details>
<summary>Por que quem procura parceiro não luta</summary>

A busca por parceiro filtra por espécie, não por reino. Então dois
inimigos da mesma espécie podem se aproximar para ter filho.

Sem essa trégua, toda tentativa de reprodução entre reinos virava luta.
Medido, os nascimentos caíam de 15.864 para 277 em seis sementes.

A trégua sozinha não bastou: procurar parceiro ocupa só 9,4% do tempo de
uma criatura. O que resolveu de verdade foi fundar reinos contíguos.

</details>

### Quanto o combate custa à população?

Sem combate, a população média ficava perto de 430. Com combate, as
medições atuais ficam entre 195 e 262, conforme a taxa de quadros, sem
extinção. É a fronteira cobrando, não um defeito. Os números estão em
[verificação](../verificacao.md).

---

## ✋ O que o jogador pode fazer?

Um poder só: **tocar numa criatura a fere**. Cada toque tira 0,34 de vida
(`CreatureConfig.playerStrikeDamage`), então três toques matam.

- `Simulation.strikeAt(tileX, tileY)` fere a primeira criatura viva no
  tile tocado.
- O golpe usa a mesma `applyDamage`, com a causa `CAUSE_PLAYER_STRIKE`, e
  a morte sai pelo mesmo `die()`.
- Tocar na água, no vazio ou fora do mundo não faz nada. Não é erro:
  errar o alvo é parte de mirar.

O caminho do dedo até a criatura está em
[arquitetura](../arquitetura.md#como-um-toque-chega-até-a-criatura).

**Toque longo** descarta o mundo e gera outro. É um atalho de
desenvolvimento e vai sair quando houver interface.
