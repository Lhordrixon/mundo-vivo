# 0006 — Poucos reinos contíguos

**Problema.** Cada fundador fundava o próprio reino: 240 reinos de uma
criatura, espalhados pelo mapa. O vizinho mais próximo de alguém quase
nunca era do mesmo reino. Quando o combate corpo a corpo entrou, todo
encontro virou luta, e as seis sementes testadas se extinguiram.

**Decisão.** O mundo funda `CreatureConfig.initialFactions` reinos, hoje
4. `Simulation.placeFactionCentres` escolhe uma capital por reino: para
cada uma, sorteia 12 tiles de terra e fica com o mais distante das
capitais já escolhidas (heurística do melhor candidato, de Mitchell).
Cada fundador entra no reino da capital mais próxima em linha reta, o que
forma regiões contíguas (células de Voronoi).

**Alternativas.**
- Manter um reino por fundador e só enfraquecer o combate. As medições
  mostraram que o problema era a fundação, não a força do combate.
- Fatiar o mapa em faixas: cria viés de grade.
- Uma trégua durante o cortejo: ajudou (nascimentos de 277 para 384), mas
  não evitou as extinções. Continua valendo, junto com esta decisão.

**Consequências.**
- 95 a 98% das criaturas nascem com o vizinho mais próximo no mesmo reino
  (`foundingKingdomsAreSpatiallyCoherent`).
- Nenhum reino novo surge depois da fundação.
- Um fundador do outro lado de uma baía pode cair num reino que não
  alcança a pé. O território de verdade, que respeita o terreno, corrige
  isso depois.

<details>
<summary>As medições que escolheram 4 reinos</summary>

6 sementes, 20 minutos simulados. Sem combate, para saber se agrupar
custa população:

| Reinos | População média | Extinções | Nascimentos |
|---|---|---|---|
| 4 | 475 (229–622) | 0/6 | 15.342 |
| 5 | 369 (171–592) | 0/6 | 15.258 |
| 6 | 361 (165–523) | 0/6 | 14.779 |

Não custa: o mundo de 240 reinos espalhados dava 432 e 15.864.

Com combate:

| Reinos | População média | Extinções | Nascimentos | Mortes em combate |
|---|---|---|---|---|
| 3 | 166 (38–314) | 0/6 | 8.188 | 492 |
| **4** | **158 (33–238)** | **0/6** | **8.471** | **538** |
| 5 | 143 (23–321) | 0/6 | 6.716 | 620 |
| 6 | 164 (24–290) | 0/6 | 6.912 | 653 |
| 8 | 77 (4–177) | 0/6 | 5.065 | 724 |

Com os 240 reinos espalhados, eram 6 extinções em 6. Quatro ganha nas
duas medições: a maior população sem combate e a melhor pior-semente com
combate. A partir de 8, o mundo fica frágil.

Essas medições são de antes da genética. Os números atuais estão em
[verificação](../verificacao.md).

</details>

**Estado.** Em vigor desde o commit `589df6a`.
