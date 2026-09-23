# 🌱 Ecologia

- **O que é:** a comida de cada tile, que é consumida e cresce de volta.
- **Classes principais:** `sim/ecology/FoodMap`.
- **Quem depende:** `Simulation`. `ecology` depende só de `world`.
- **Testes que cobrem:** a parte de comida do `tools/SimSelfTest` e, no
  JUnit, `SimulationTest` ("a comida cai quando há bocas e volta a
  crescer", "morte por terreno vira comida no tile"). O `FoodMap` não tem
  classe de teste JUnit própria.

---

## Como a comida funciona?

Cada tile tem uma quantidade de comida e um teto. O teto vem da
fertilidade do tipo de tile: selva sustenta muito, deserto quase nada,
água nada.

- A cada passo, `FoodMap.regrow` faz a comida crescer de volta, sem passar
  do teto.
- Uma criatura comendo tira comida do tile com `FoodMap.consume`. Ninguém
  come o que não existe.
- Quem morre vira comida no tile onde caiu, com `FoodMap.deposit`.

<details>
<summary>Um afogado não deixa cadáver</summary>

Quem morre num tile de água não deixa comida. O `FoodMap` respeita o teto
do tile, e água tem teto zero. O corpo afunda.

É uma regra antiga do `FoodMap`. Ela só ficou alcançável quando o dano de
terreno apareceu.

</details>

---

## O que limita a população?

A comida, mas pela **natalidade**, não pela mortalidade. Quem está com
fome acima do limite simplesmente não reproduz.

> [!TIP]
> Antes de mexer nos números: baixar a rebrota não mata mais criaturas.
> Faz nascerem menos.

<details>
<summary>O limite de "tile comestível" precisa ser baixo</summary>

Esse número foi descoberto medindo. Com o limite alto, a escassez virava
um precipício: um tile com comida logo abaixo do limite era invisível,
então a comida acabava para todo mundo ao mesmo tempo, e a população
despencava junto.

Com o limite baixo, a escassez chega devagar, e a população oscila em vez
de despencar.

</details>

---

## O que ainda não existe?

- A rebrota percorre o mapa inteiro a cada passo. Veja a
  [dívida técnica](../divida-tecnica.md).
- `FoodMap.regrowthPerSecond(float)` permite ajustar a rebrota, mas nada no
  jogo o chama.
- Não há vegetação como entidade, dieta nem predação. Veja o
  [roadmap](../roadmap.md).
