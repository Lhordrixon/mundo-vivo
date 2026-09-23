# 👑 Facções

- **O que é:** o reino de cada criatura e o território de cada reino.
- **Classes principais:** `sim/faction/FactionRegistry` (reinos, nome e
  cor), `sim/faction/Territory` (de quem é cada tile);
  `Simulation.placeFactionCentres` (fundação).
- **Quem depende:** `Simulation`. `faction` depende de `creature`,
  `world` e `util`.
- **Testes que cobrem:** `FactionRegistryTest`, `TerritoryTest`,
  `SimulationTest` (fundação e território) e a parte de facções do
  `tools/SimSelfTest`.

**Em resumo:** o mundo funda 4 reinos vizinhos entre si. Cada criatura
pertence a um deles e passa o reino para os filhos. Criaturas de reinos
diferentes brigam quando se encontram (veja [combate](criaturas.md)).
O território de cada reino é recalculado uma vez por segundo.

---

## 👑 Como os reinos nascem?

O mundo funda 4 reinos (`CreatureConfig.initialFactions`). Cada reino ocupa
uma região contínua do mapa.

1. `Simulation.placeFactionCentres` escolhe uma capital para cada reino,
   bem longe das outras.
2. Cada criatura fundadora entra no reino da capital mais próxima.

O resultado: 95 a 98% das criaturas nascem com o vizinho mais próximo no
mesmo reino. O teste `foundingKingdomsAreSpatiallyCoherent` garante isso.

> [!IMPORTANT]
> Antes, cada fundador fundava o próprio reino: eram 240 reinos de uma
> criatura, espalhados. Quase ninguém tinha um vizinho do mesmo reino.
> Quando o combate entrou, todo encontro virava luta, e as seis sementes
> testadas se extinguiram. Veja
> [poucos reinos contíguos](../decisoes/0006-poucos-reinos-contiguos.md).

<details>
<summary>Como as capitais são escolhidas</summary>

**Capitais bem espaçadas.** Para cada capital, o jogo sorteia 12 tiles de
terra. Fica o mais distante das capitais já escolhidas. É a "heurística do
melhor candidato", de Mitchell: barata, determinística e muito melhor que
sortear uma vez só.

**Fronteiras de graça.** Cada fundador no reino da capital mais próxima
forma regiões contínuas por construção. Na geometria, isso se chama
**células de Voronoi**.

A distância usada aqui é em linha reta. Um fundador do outro lado de uma
baía pode cair num reino que não alcança a pé. Tudo bem: é só o ponto de
partida. O território de verdade respeita o terreno (veja abaixo).

</details>

<details>
<summary>Por que 4 reinos: as medições</summary>

Medido em 6 sementes, 20 minutos simulados.

**Sem combate**, para saber se agrupar custa população:

| reinos | população média | extinções |
|---|---|---|
| 4 | 475 | 0 de 6 |
| 5 | 369 | 0 de 6 |
| 6 | 361 | 0 de 6 |

Não custa. O mundo antigo, com 240 reinos espalhados, dava 432.

**Com combate:**

| reinos | população média | pior semente | extinções |
|---|---|---|---|
| 3 | 166 | 38 | 0 de 6 |
| **4** | **158** | **33** | **0 de 6** |
| 5 | 143 | 23 | 0 de 6 |
| 6 | 164 | 24 | 0 de 6 |
| 8 | 77 | 4 | 0 de 6 |

Com os 240 reinos espalhados, eram 6 extinções em 6.

Quatro ganha nas duas medições: a maior população sem combate e a melhor
pior-semente com combate. A partir de 8, o mundo fica frágil.

Essas medições são de antes da genética. Os números atuais estão em
[verificação](../verificacao.md).

</details>

---

## 👑 Como um filho escolhe o reino?

Ele herda o reino de um dos pais, por sorteio meio a meio.

Na prática, os pais quase sempre são do mesmo reino. Os vizinhos são
parentes, e agora nascem no mesmo reino. Mas nada impede um casal de reinos
diferentes.

Nenhum reino novo surge depois da fundação. O número de reinos fica
constante pelo resto do jogo. O teste `noNewFactionsAfterInitialFounding`
garante isso.

---

## 👑 De quem é cada tile?

Cada tile pertence ao reino da criatura viva que chega nele primeiro,
**andando**. Não vale linha reta.

Andar importa. Duas criaturas nos dois lados de uma baía não dividem a água
ao meio. O território de cada lado cresce contornando a baía, como uma
criatura andaria de verdade. A água nunca tem dono.

O cálculo é uma **busca em largura**: todas as criaturas começam ao mesmo
tempo, e cada tile fica com quem chega primeiro. O custo depende do número
de tiles, não do número de criaturas.

`Simulation` recalcula o território uma vez por segundo. Uma fronteira não
precisa reagir em 16 ms a alguém que andou meio tile.

<details>
<summary>Detalhes da busca em largura</summary>

Varrer o mundo uma vez custa cerca de 49 mil operações. Varrer uma vez por
criatura custaria dezenas de milhões.

Os arrays de apoio são criados uma vez, no tamanho do mundo, e
reaproveitados. Recalcular não aloca memória.

Num empate exato de distância, o tile fica com quem aparece primeiro na
lista de criaturas. É arbitrário, mas determinístico, e é isso que importa.

Um recálculo custa cerca de 0,3 ms (medido no `SimSelfTest` com pouco
mais de 350 criaturas), com folga no segundo de intervalo. Os números
atuais estão em [verificação](../verificacao.md).

</details>

### O território muda o comportamento de alguém?

Ainda não. Ninguém evita terra de outro reino, e ninguém disputa fronteira.

As criaturas brigam por **proximidade**, não por território. Um inimigo
perto é atacado esteja onde estiver. O território também não é desenhado na
tela.

---

## 👑 Os reinos têm nome e cor?

Têm, mas o jogador ainda não vê nenhum dos dois.

- **Nome:** `FactionRegistry.nameOf` devolve nomes inventados, montados
  com duas tabelas de sílabas. O mundo da semente 12345 tem Dundor, Aelrok,
  Dunholm e Breholm.
- **Cor:** `FactionRegistry.colorOf` devolve uma cor por reino.

Os dois vêm da semente do mundo. A mesma semente dá sempre os mesmos nomes
e as mesmas cores.

Hoje, só os testes chamam `nameOf` e `colorOf`. A cor de cada criatura na
tela indica o que ela está fazendo, não o reino. Está anotado em
[dívida técnica](../divida-tecnica.md).

<details>
<summary>Como as cores ficam diferentes entre si</summary>

A cor não é sorteada: é calculada a partir do número do reino. Cada reino
gira cerca de 137,5° no círculo de cores em relação ao anterior: o número
do reino multiplicado pelo inverso da razão áurea (0,618…). É o giro que
espalha as cores o máximo possível, para qualquer quantidade de reinos.

Sortear seria pior: sorteios agrupam, e dois reinos vizinhos poderiam ficar
com o mesmo tom. O sorteio decide só a saturação e o brilho, entre quatro
opções claras. O fundo do jogo é quase preto, e cores escuras sumiriam.

A cor é um `int` no formato RGBA, não um `Color` do libGDX. Nada em `sim/`
pode depender do libGDX. O formato é o mesmo de `TileType.colorRgba8888()`,
então a camada de desenho vai consumir os dois do mesmo jeito.

**O que o esquema não garante.** A separação é entre números de reino, não
no mapa. Dois reinos vizinhos no terreno podem ter números distantes e,
com azar, cores parecidas. Escolher a cor olhando o território resolveria,
mas o território muda a cada segundo, e a cor mudaria junto, o que é pior.

</details>

<details>
<summary>Como os nomes são sorteados</summary>

Os nomes saem de duas tabelas de sílabas inventadas: 24 inícios × 16
finais, 384 combinações. Com 4 reinos a repetição é rara, e mesmo quando
acontece incomoda menos que "Reino 37".

Nome e cor saem de um único `nextLong()` do `Rng` por reino, lido em
faixas de bits diferentes. Um sorteio em vez de três mexe o mínimo
possível na sequência que o resto da simulação usa.

A semente 2026 funda Garvik, Tyrdor, Ashrok e Ornnen.

</details>
