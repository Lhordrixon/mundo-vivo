# Simulação

Como o mundo nasce e como as criaturas vivem, comem, brigam e morrem.

**Em resumo:** o mundo é uma grade de 256×192 tiles gerada a partir de uma
semente. Nela vivem 240 criaturas fundadoras. Elas comem, se reproduzem,
envelhecem, brigam com outras facções e morrem. Tudo em `core/.../sim/`.

---

## 🌍 Como o mundo é gerado?

Em duas passadas sobre a grade:

1. Um ruído fractal preenche a elevação de cada tile.
2. A elevação é esticada para ocupar a faixa inteira de 0 a 1. Depois ganha
   cristas de montanha e afunda nas bordas.
3. Temperatura e umidade são calculadas. Junto com a altura, elas decidem o
   bioma de cada tile.

O passo 2 não é detalhe. O ruído fractal se concentra no meio da faixa.
Sem esticar, nenhum ponto chega a ser montanha, e o mundo sai plano.

Separar clima de bioma é o que põe deserto ao lado de selva na mesma
latitude. Sem isso, os biomas sairiam em faixas horizontais.

---

## 🐾 Como uma criatura vive?

Cada criatura tem fome, vida, idade e um **estado**: vagando, procurando
comida, comendo ou procurando parceiro.

- A fome sobe o tempo todo.
- Acima de um limite, a criatura larga tudo e vai atrás de comida.
- Com a fome no máximo, ela começa a perder vida.
- Saciada, adulta e fora do período de espera, ela procura parceiro.
- Reproduzir custa fome aos dois pais.

A cor na tela mostra o estado. Branco é vagando, amarelo é procurando
comida, verde é comendo, rosa é procurando parceiro.

> [!TIP]
> O custo de fome da reprodução é o que prende a população à comida. Sem
> ele, a população cresce até o limite do pool e para lá.

<details>
<summary>Três números que foram descobertos medindo, não escolhendo</summary>

**O raio de busca por parceiro é cinco vezes o da comida.** São 70 tiles
contra 14. Com os dois iguais, uma população pequena deixava de se
encontrar e se extinguia com comida sobrando. Um terço das sementes
testadas morria assim.

Ampliar o raio de parceiro é de graça: essa busca já percorre a lista de
criaturas. Ampliar o de comida custaria varrer mais tiles.

**O limite de "tile comestível" precisa ser baixo.** Com ele alto, a comida
acabava para todo mundo ao mesmo tempo, e a população despencava junto.
Baixo, a escassez chega devagar e a população oscila.

**A espera entre reproduções decide a estabilidade.** Com espera curta, a
população multiplicava antes de a comida responder, e depois morria de
fome inteira. O ciclo sumiu ao dobrar a espera.

</details>

### Por que as criaturas quase não morrem de fome?

A comida limita a população pela **natalidade**, não pela mortalidade.
Quem está com fome demais simplesmente não reproduz.

Vale saber antes de mexer nos números: baixar a rebrota de comida não mata
mais criaturas. Faz nascerem menos.

---

## 💔 Como uma criatura perde vida?

Existe uma porta só: `Creature.applyDamage(quanto, causa)`. Fome, terreno,
golpe do jogador e combate passam todos por ela.

O método tira vida, para em zero e anota a causa. Ele devolve `true` só se
**aquele** golpe foi o fatal. Assim, dois golpes na mesma criatura nunca
contam duas mortes.

`applyDamage` não mata ninguém. Quem trata a morte é
`Simulation.resolveDeathFromDamage`, sempre do mesmo jeito:

1. O corpo vira comida no tile.
2. A criatura sai da facção.
3. O espaço dela volta para o pool.

A causa só decide em qual contador a morte entra.

### Que causas de dano existem hoje?

- 🍂 **Fome** (`CAUSE_STARVATION`): quando a fome chega ao máximo.
- 🌊 **Terreno perigoso** (`CAUSE_HAZARDOUS_TERRAIN`): só o oceano profundo
  fere. Em jogo normal isso nunca acontece, porque ninguém entra andando na
  água. Serve para quando o chão mudar debaixo de alguém.
- 👆 **Golpe do jogador** (`CAUSE_PLAYER_STRIKE`): cada toque tira 0,34 de
  vida.
- ⚔️ **Combate** (`CAUSE_COMBAT`): veja a próxima seção.

<details>
<summary>Um afogado não deixa cadáver</summary>

Quem morre num tile de água não deixa comida. O `FoodMap` respeita a
capacidade do tile, e água tem capacidade zero. O corpo afunda.

É uma regra antiga do `FoodMap`. Ela só ficou alcançável quando o dano de
terreno apareceu.

</details>

---

## ⚔️ Como funciona o combate?

Criaturas de facções diferentes que ficam perto uma da outra se ferem.

- **Alcance:** 1,5 tile (`CreatureConfig.combatRangeTiles`).
- **Dano:** 0,18 de vida por segundo, **por inimigo** ao alcance
  (`combatDamagePerSecond`).
- **Quem não briga:** criatura sem facção, e quem está procurando parceiro.
- **Onde fica:** `Simulation.hostileNeighbours`, chamado a cada passo.

O dano se multiplica pelo número de inimigos. Assim, estar em menor número
mata mais rápido. Com dano fixo, uma criatura sozinha trocaria de igual para
igual com um reino inteiro.

Combate é reação a proximidade. Ninguém persegue inimigo, e não há exército.
Isso é de propósito: é o mínimo para facção e território terem consequência.

> [!IMPORTANT]
> O combate só funciona porque o mundo nasce com poucos reinos contíguos.
> Com uma facção por criatura, todo vizinho era inimigo e o mundo se
> extinguia. A história está em [facções](faccoes.md).

<details>
<summary>Por que quem procura parceiro não luta</summary>

A busca por parceiro filtra por espécie, não por facção. Então dois
inimigos da mesma espécie podem se aproximar para ter filho.

Sem essa trégua, toda tentativa de reprodução entre facções virava luta.
Medido, os nascimentos caíam de 15.864 para 277 em seis sementes.

A trégua sozinha não bastou. Procurar parceiro ocupa só 9,4% do tempo de
uma criatura. Nos outros 90,6% ela estava exposta. O que resolveu de
verdade foi a fundação de reinos contíguos.

</details>

### Quanto o combate custa à população?

Sem combate, a população de equilíbrio medida ficava perto de 430. Com
combate, as medições atuais ficam entre 195 e 262, conforme a taxa de
quadros. Nenhuma semente testada se extingue.

É a fronteira cobrando, não um defeito. Os números atuais estão em
[verificação](verificacao.md).
